package ec.udla.licencias.service;

import ec.udla.licencias.cache.CacheManager;
import ec.udla.licencias.client.AntClient;
import ec.udla.licencias.dto.LicenciaInfo;
import ec.udla.licencias.dto.LicenciaResponse;
import ec.udla.licencias.dto.LicenciaResponse.Fuente;
import ec.udla.licencias.exception.AntNoDisponibleException;
import ec.udla.licencias.resilience.CircuitBreaker;
import ec.udla.licencias.validator.ResponseValidator;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * LicenciaService: orquesta el patron Cache-Aside + Circuit Breaker.
 *
 *  1. CacheManager.buscar -> copia fresca? devolverla (fuente CACHE).
 *  2. CircuitBreaker autoriza -> ANTClient consulta -> ResponseValidator valida y almacena
 *     en cache -> devolver (fuente ANT).
 *  3. ANT caida o circuito abierto -> devolver la ultima copia del cache (CACHE_RESPALDO).
 *  4. Sin copia -> error 503.
 */
@Service
public class LicenciaService {

    private final CacheManager cacheManager;
    private final CircuitBreaker circuitBreaker;
    private final AntClient antClient;
    private final ResponseValidator responseValidator;

    public LicenciaService(CacheManager cacheManager, CircuitBreaker circuitBreaker, AntClient antClient,
                           ResponseValidator responseValidator) {
        this.cacheManager = cacheManager;
        this.circuitBreaker = circuitBreaker;
        this.antClient = antClient;
        this.responseValidator = responseValidator;
    }

    public LicenciaResponse consultarPuntos(String cedula, String placa) {
        if (cedula == null || !cedula.matches("\\d{10}")) {
            throw new IllegalArgumentException("La cedula debe tener 10 digitos");
        }

        Optional<LicenciaInfo> copia = cacheManager.buscar(cedula);
        if (copia.isPresent() && cacheManager.esFresca(copia.get())) {
            return respuesta(copia.get(), Fuente.CACHE, null);
        }

        try {
            LicenciaInfo info = circuitBreaker.ejecutar(() ->
                    responseValidator.validarYAlmacenar(cedula, antClient.consultarPuntos(cedula, placa)));
            return respuesta(info, Fuente.ANT, null);
        } catch (AntNoDisponibleException e) {
            if (copia.isEmpty()) throw e;
            return respuesta(copia.get(), Fuente.CACHE_RESPALDO,
                    "La ANT no esta disponible (" + e.getMessage() + "). Datos del " + copia.get().obtenidoEn());
        }
    }

    private LicenciaResponse respuesta(LicenciaInfo info, Fuente fuente, String advertencia) {
        return new LicenciaResponse(info, fuente, circuitBreaker.getEstado().name(), advertencia);
    }

}
