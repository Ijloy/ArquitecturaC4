package ec.udla.licencias.exception;

/** La ANT no respondio, respondio con error o devolvio una pagina sin datos. */
public class AntNoDisponibleException extends RuntimeException {
    public AntNoDisponibleException(String message) {
        super(message);
    }

    public AntNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
