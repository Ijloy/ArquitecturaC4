package ec.udla.licencias.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Datos validados de la licencia. Es el objeto que se guarda en Redis.
 *
 * @param puntos     puntos de la licencia (null si la pagina no los muestra)
 * @param citaciones filas de la tabla de citaciones (columna -> valor)
 * @param obtenidoEn momento en que se obtuvo de la ANT
 */
public record LicenciaInfo(
        String cedula,
        Integer puntos,
        List<Map<String, String>> citaciones,
        Instant obtenidoEn) {
}
