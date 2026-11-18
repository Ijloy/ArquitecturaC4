package ec.udla.contribuyentes.service;

import com.fasterxml.jackson.databind.JsonNode;
import ec.udla.contribuyentes.client.SriCatastroClient;
import ec.udla.contribuyentes.dto.ContribuyenteResponse;
import ec.udla.contribuyentes.registro.ConsultaContribuyente;
import ec.udla.contribuyentes.registro.ConsultaContribuyenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Valida el RUC, verifica que sea contribuyente y persona natural, y registra la consulta. */
@Service
public class ContribuyenteService {

    private static final Logger log = LoggerFactory.getLogger(ContribuyenteService.class);

    private final SriCatastroClient sri;
    private final ConsultaContribuyenteRepository registro;

    public ContribuyenteService(SriCatastroClient sri, ConsultaContribuyenteRepository registro) {
        this.sri = sri;
        this.registro = registro;
    }

    public ContribuyenteResponse verificar(String ruc, String correo) {
        if (ruc == null || !ruc.matches("\\d{13}")) {
            throw new IllegalArgumentException("El RUC debe tener 13 digitos numericos");
        }
        if (correo == null || !correo.matches("\\S+@\\S+\\.\\S+")) {
            throw new IllegalArgumentException("Correo electronico no valido");
        }
        String cedula = ruc.substring(0, 10);

        ContribuyenteResponse respuesta;
        if (!sri.existeContribuyente(ruc)) {
            respuesta = new ContribuyenteResponse(ruc, cedula, false, false,
                    "El RUC no esta registrado como contribuyente en el SRI", null);
        } else {
            Optional<JsonNode> datos = sri.obtenerContribuyente(ruc);
            boolean natural = datos.map(ContribuyenteService::esPersonaNatural).orElse(false);
            respuesta = new ContribuyenteResponse(ruc, cedula, true, natural,
                    natural ? "Contribuyente verificado: persona natural"
                            : "El contribuyente no es persona natural",
                    datos.orElse(null));
        }

        registrar(respuesta, correo);
        return respuesta;
    }

    /** El SRI indica el tipo en "tipoContribuyente" (ej. "PERSONA NATURAL"). */
    static boolean esPersonaNatural(JsonNode datos) {
        JsonNode tipo = datos.get("tipoContribuyente");
        return tipo != null && tipo.asText().toUpperCase().contains("NATURAL");
    }

    /** Registrar es secundario: si la BD falla, la consulta igual responde. */
    private void registrar(ContribuyenteResponse r, String correo) {
        try {
            registro.save(new ConsultaContribuyente(r.ruc(), correo, r.esContribuyente(), r.esPersonaNatural()));
        } catch (Exception e) {
            log.warn("No se pudo registrar la consulta: {}", e.getMessage());
        }
    }
}
