package ec.udla.contribuyentes.service;

import com.fasterxml.jackson.databind.JsonNode;
import ec.udla.contribuyentes.client.SriCatastroClient;
import ec.udla.contribuyentes.dto.ContribuyenteResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Valida el RUC y verifica que sea contribuyente y persona natural. */
@Service
public class ContribuyenteService {

    private final SriCatastroClient sri;

    public ContribuyenteService(SriCatastroClient sri) {
        this.sri = sri;
    }

    public ContribuyenteResponse verificar(String ruc, String correo) {
        if (ruc == null || !ruc.matches("\\d{13}")) {
            throw new IllegalArgumentException("El RUC debe tener 13 digitos numericos");
        }
        if (correo == null || !correo.matches("\\S+@\\S+\\.\\S+")) {
            throw new IllegalArgumentException("Correo electronico no valido");
        }
        String cedula = ruc.substring(0, 10);

        if (!sri.existeContribuyente(ruc)) {
            return new ContribuyenteResponse(ruc, cedula, false, false,
                    "El RUC no esta registrado como contribuyente en el SRI", null);
        }

        Optional<JsonNode> datos = sri.obtenerContribuyente(ruc);
        boolean natural = datos.map(ContribuyenteService::esPersonaNatural).orElse(false);
        return new ContribuyenteResponse(ruc, cedula, true, natural,
                natural ? "Contribuyente verificado: persona natural"
                        : "El contribuyente no es persona natural",
                datos.orElse(null));
    }

    /** El SRI indica el tipo en "tipoContribuyente" (ej. "PERSONA NATURAL"). */
    static boolean esPersonaNatural(JsonNode datos) {
        JsonNode tipo = datos.get("tipoContribuyente");
        return tipo != null && tipo.asText().toUpperCase().contains("NATURAL");
    }

}
