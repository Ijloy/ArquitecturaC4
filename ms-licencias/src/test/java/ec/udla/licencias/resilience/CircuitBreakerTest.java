package ec.udla.licencias.resilience;

import ec.udla.licencias.exception.AntNoDisponibleException;
import ec.udla.licencias.exception.CircuitoAbiertoException;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class CircuitBreakerTest {

    /** Reloj que se puede adelantar a mano. */
    static class RelojManual extends Clock {
        Instant ahora = Instant.parse("2026-11-17T12:00:00Z");
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return ahora; }
    }

    final RelojManual reloj = new RelojManual();
    final CircuitBreaker cb = new CircuitBreaker(3, Duration.ofSeconds(60), reloj);

    private void fallar() {
        assertThrows(AntNoDisponibleException.class,
                () -> cb.ejecutar(() -> { throw new AntNoDisponibleException("timeout"); }));
    }

    @Test
    void seAbreTrasTresFallosSeguidos() {
        fallar(); fallar();
        assertEquals(CircuitBreaker.Estado.CERRADO, cb.getEstado());
        fallar();
        assertEquals(CircuitBreaker.Estado.ABIERTO, cb.getEstado());
        assertThrows(CircuitoAbiertoException.class, () -> cb.ejecutar(() -> "no deberia llamarse"));
    }

    @Test
    void trasElTiempoAbiertoPruebaYSeCierraSiFunciona() {
        fallar(); fallar(); fallar();
        reloj.ahora = reloj.ahora.plusSeconds(61);
        assertEquals("ok", cb.ejecutar(() -> "ok"));
        assertEquals(CircuitBreaker.Estado.CERRADO, cb.getEstado());
    }

    @Test
    void siLaPruebaFallaVuelveAAbrirse() {
        fallar(); fallar(); fallar();
        reloj.ahora = reloj.ahora.plusSeconds(61);
        fallar();
        assertEquals(CircuitBreaker.Estado.ABIERTO, cb.getEstado());
    }
}
