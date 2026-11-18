package ec.udla.vehiculos.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/** Cliente de la API REST SRI (matriculacion). "Consulta informacion por placa - REST HTTPS". */
@Component
public class SriVehiculoClient {

    private final RestClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public SriVehiculoClient(RestClient restClient, ObjectMapper mapper,
                             @Value("${sri.vehiculo.base-url}") String baseUrl) {
        this.http = restClient;
        this.mapper = mapper;
        this.baseUrl = baseUrl;
    }

    public Optional<JsonNode> obtenerPorPlaca(String placa) {
        try {
            String body = http.get()
                    .uri(baseUrl + "/obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn?numeroPlacaCampvCpn=" + placa)
                    .retrieve()
                    .body(String.class);
            if (body == null || body.isBlank()) return Optional.empty();
            JsonNode json = mapper.readTree(body);
            return (json.isNull() || json.isEmpty()) ? Optional.empty() : Optional.of(json);
        } catch (Exception e) {
            throw new SriNoDisponibleException("No se pudo consultar el SRI: " + e.getMessage(), e);
        }
    }
}
