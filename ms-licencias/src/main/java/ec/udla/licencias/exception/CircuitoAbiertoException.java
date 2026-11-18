package ec.udla.licencias.exception;

/** El Circuit Breaker no autorizo la consulta externa (circuito ABIERTO). */
public class CircuitoAbiertoException extends AntNoDisponibleException {
    public CircuitoAbiertoException(String message) {
        super(message);
    }
}
