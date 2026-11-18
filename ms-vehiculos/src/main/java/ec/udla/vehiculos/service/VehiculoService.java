package ec.udla.vehiculos.service;

import com.fasterxml.jackson.databind.JsonNode;
import ec.udla.vehiculos.client.SriVehiculoClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VehiculoService {

    private final SriVehiculoClient sri;

    public VehiculoService(SriVehiculoClient sri) {
        this.sri = sri;
    }

    public Optional<JsonNode> consultar(String placa) {
        String normalizada = placa == null ? "" : placa.toUpperCase().replace("-", "").trim();
        if (!normalizada.matches("[A-Z0-9]{5,8}")) {
            throw new IllegalArgumentException("Placa no valida");
        }
        return sri.obtenerPorPlaca(normalizada);
    }
}
