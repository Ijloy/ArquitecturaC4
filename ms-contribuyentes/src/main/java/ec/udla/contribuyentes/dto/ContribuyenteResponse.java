package ec.udla.contribuyentes.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @param cedula 10 primeros digitos del RUC; la usa el frontend para consultar la ANT
 */
public record ContribuyenteResponse(
        String ruc,
        String cedula,
        boolean esContribuyente,
        boolean esPersonaNatural,
        String mensaje,
        JsonNode datos) {
}
