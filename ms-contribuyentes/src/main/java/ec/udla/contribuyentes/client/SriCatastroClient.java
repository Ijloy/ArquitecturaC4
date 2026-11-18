package ec.udla.contribuyentes.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/** Cliente de la API REST SRI (catastro). "Valida RUC y consulta contribuyente - REST HTTPS". */
@Component
public class SriCatastroClient {

    private final RestClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public SriCatastroClient(RestClient restClient, ObjectMapper mapper,
                             @Value("${sri.catastro.base-url}") String baseUrl) {
        this.http = restClient;
        this.mapper = mapper;
        this.baseUrl = baseUrl;
    }

    /** existePorNumeroRuc devuelve true / false */
    public boolean existeContribuyente(String ruc) {
        String body = get(baseUrl + "/existePorNumeroRuc?numeroRuc=" + ruc);
        return body != null && body.trim().equalsIgnoreCase("true");
    }

    /** obtenerPorNumerosRuc devuelve un arreglo JSON; se toma el primer elemento */
    public Optional<JsonNode> obtenerContribuyente(String ruc) {
        String body = get(baseUrl + "/obtenerPorNumerosRuc?&ruc=" + ruc);
        if (body == null || body.isBlank()) return Optional.empty();
        try {
            JsonNode json = mapper.readTree(body);
            if (json.isArray()) return json.isEmpty() ? Optional.empty() : Optional.of(json.get(0));
            return Optional.of(json);
        } catch (Exception e) {
            throw new SriNoDisponibleException("Respuesta del SRI no valida", e);
        }
    }

    private String get(String url) {
        try {
            return http.get().uri(url).retrieve().body(String.class);
        } catch (Exception e) {
            throw new SriNoDisponibleException("No se pudo consultar el SRI: " + e.getMessage(), e);
        }
    }
}
