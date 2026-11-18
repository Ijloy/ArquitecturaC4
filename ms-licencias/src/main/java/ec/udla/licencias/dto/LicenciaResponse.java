package ec.udla.licencias.dto;

/**
 * Resultado de consulta que se devuelve al API Gateway.
 *
 * @param fuente          ANT (en vivo), CACHE (copia fresca) o CACHE_RESPALDO (ANT caida)
 * @param estadoCircuito  estado del Circuit Breaker al responder
 * @param advertencia     aviso cuando el dato podria estar desactualizado
 */
public record LicenciaResponse(
        LicenciaInfo licencia,
        Fuente fuente,
        String estadoCircuito,
        String advertencia) {

    public enum Fuente { ANT, CACHE, CACHE_RESPALDO }
}
