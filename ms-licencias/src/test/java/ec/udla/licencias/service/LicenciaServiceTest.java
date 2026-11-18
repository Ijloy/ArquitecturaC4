package ec.udla.licencias.service;

import ec.udla.licencias.cache.CacheManager;
import ec.udla.licencias.client.AntClient;
import ec.udla.licencias.dto.LicenciaInfo;
import ec.udla.licencias.dto.LicenciaResponse.Fuente;
import ec.udla.licencias.exception.AntNoDisponibleException;
import ec.udla.licencias.exception.CircuitoAbiertoException;
import ec.udla.licencias.resilience.CircuitBreaker;
import ec.udla.licencias.validator.ResponseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Prueba el flujo del diagrama C3 sin Redis ni ANT reales. */
@ExtendWith(MockitoExtension.class)
class LicenciaServiceTest {

    static final String CEDULA = "1712345678";

    @Mock CacheManager cacheManager;
    @Mock AntClient antClient;
    @Mock ResponseValidator responseValidator;

    LicenciaService service;

    @BeforeEach
    void setUp() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(3, Duration.ofSeconds(60), Clock.systemUTC());
        service = new LicenciaService(cacheManager, circuitBreaker, antClient, responseValidator);
    }

    private LicenciaInfo info(int puntos) {
        return new LicenciaInfo(CEDULA, puntos, List.of(), Instant.now());
    }

    @Test
    void copiaFrescaSeDevuelveSinConsultarLaAnt() {
        LicenciaInfo enCache = info(30);
        when(cacheManager.buscar(CEDULA)).thenReturn(Optional.of(enCache));
        when(cacheManager.esFresca(enCache)).thenReturn(true);

        var r = service.consultarPuntos(CEDULA, "");

        assertEquals(Fuente.CACHE, r.fuente());
        verifyNoInteractions(antClient);
    }

    @Test
    void sinCacheConsultaLaAntYValida() {
        when(cacheManager.buscar(CEDULA)).thenReturn(Optional.empty());
        when(antClient.consultarPuntos(CEDULA, "PBA1234")).thenReturn("<html/>");
        when(responseValidator.validarYAlmacenar(CEDULA, "<html/>")).thenReturn(info(28));

        var r = service.consultarPuntos(CEDULA, "PBA1234");

        assertEquals(Fuente.ANT, r.fuente());
        assertEquals(28, r.licencia().puntos());
    }

    @Test
    void antCaidaDevuelveCopiaDeRespaldo() {
        LicenciaInfo antigua = new LicenciaInfo(CEDULA, 25, List.of(), Instant.now().minus(Duration.ofDays(3)));
        when(cacheManager.buscar(CEDULA)).thenReturn(Optional.of(antigua));
        when(cacheManager.esFresca(antigua)).thenReturn(false);
        when(antClient.consultarPuntos(any(), any())).thenThrow(new AntNoDisponibleException("timeout"));

        var r = service.consultarPuntos(CEDULA, "");

        assertEquals(Fuente.CACHE_RESPALDO, r.fuente());
        assertEquals(25, r.licencia().puntos());
        assertNotNull(r.advertencia());
    }

    @Test
    void antCaidaSinCacheLanzaError() {
        when(cacheManager.buscar(CEDULA)).thenReturn(Optional.empty());
        when(antClient.consultarPuntos(any(), any())).thenThrow(new AntNoDisponibleException("timeout"));

        assertThrows(AntNoDisponibleException.class, () -> service.consultarPuntos(CEDULA, ""));
    }

    @Test
    void trasTresFallosElCircuitoDejaDeLlamarALaAnt() {
        when(cacheManager.buscar(CEDULA)).thenReturn(Optional.empty());
        when(antClient.consultarPuntos(any(), any())).thenThrow(new AntNoDisponibleException("timeout"));

        for (int i = 0; i < 3; i++) {
            assertThrows(AntNoDisponibleException.class, () -> service.consultarPuntos(CEDULA, ""));
        }
        assertThrows(CircuitoAbiertoException.class, () -> service.consultarPuntos(CEDULA, ""));
        verify(antClient, times(3)).consultarPuntos(any(), any());
    }

    @Test
    void cedulaInvalidaEsRechazada() {
        assertThrows(IllegalArgumentException.class, () -> service.consultarPuntos("123", ""));
    }
}
