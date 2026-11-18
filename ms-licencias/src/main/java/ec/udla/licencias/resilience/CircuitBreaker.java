package ec.udla.licencias.resilience;

import ec.udla.licencias.exception.AntNoDisponibleException;
import ec.udla.licencias.exception.CircuitoAbiertoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * Circuit Breaker: "Autoriza consulta externa" hacia la ANT.
 *
 *  CERRADO      -> las llamadas pasan. Tras N fallos seguidos pasa a ABIERTO.
 *  ABIERTO      -> no se llama a la ANT (se responde desde cache) durante X segundos.
 *  SEMI_ABIERTO -> se deja pasar una llamada de prueba: exito = CERRADO, fallo = ABIERTO.
 *
 * Evita esperar el timeout de la ANT en cada solicitud cuando ya sabemos que esta caida.
 */
@Component
public class CircuitBreaker {

    public enum Estado { CERRADO, ABIERTO, SEMI_ABIERTO }

    private static final Logger log = LoggerFactory.getLogger(CircuitBreaker.class);

    private final int umbralFallos;
    private final Duration tiempoAbierto;
    private final Clock reloj;

    private Estado estado = Estado.CERRADO;
    private int fallosSeguidos = 0;
    private Instant abiertoDesde;

    @Autowired
    public CircuitBreaker(@Value("${ant.circuit-breaker.umbral-fallos}") int umbralFallos,
                          @Value("${ant.circuit-breaker.segundos-abierto}") long segundosAbierto) {
        this(umbralFallos, Duration.ofSeconds(segundosAbierto), Clock.systemUTC());
    }

    /** Constructor para pruebas (permite controlar el reloj). */
    public CircuitBreaker(int umbralFallos, Duration tiempoAbierto, Clock reloj) {
        this.umbralFallos = umbralFallos;
        this.tiempoAbierto = tiempoAbierto;
        this.reloj = reloj;
    }

    public <T> T ejecutar(Supplier<T> llamada) {
        if (!autorizar()) {
            throw new CircuitoAbiertoException("Circuito ABIERTO: la ANT fallo recientemente, no se consulta");
        }
        try {
            T resultado = llamada.get();
            registrarExito();
            return resultado;
        } catch (AntNoDisponibleException e) {
            registrarFallo();
            throw e;
        }
    }

    public synchronized Estado getEstado() {
        return estado;
    }

    private synchronized boolean autorizar() {
        if (estado == Estado.ABIERTO) {
            if (Instant.now(reloj).isBefore(abiertoDesde.plus(tiempoAbierto))) {
                return false;
            }
            estado = Estado.SEMI_ABIERTO;
            log.info("Circuit Breaker SEMI_ABIERTO: llamada de prueba a la ANT");
        }
        return true;
    }

    private synchronized void registrarExito() {
        fallosSeguidos = 0;
        estado = Estado.CERRADO;
    }

    private synchronized void registrarFallo() {
        fallosSeguidos++;
        if (estado == Estado.SEMI_ABIERTO || fallosSeguidos >= umbralFallos) {
            estado = Estado.ABIERTO;
            abiertoDesde = Instant.now(reloj);
            log.warn("Circuit Breaker ABIERTO tras {} fallos", fallosSeguidos);
        }
    }
}
