package ec.udla.vehiculos.service;

import com.fasterxml.jackson.databind.JsonNode;
import ec.udla.vehiculos.client.SriVehiculoClient;
import ec.udla.vehiculos.registro.ConsultaVehiculo;
import ec.udla.vehiculos.registro.ConsultaVehiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VehiculoService {

    private static final Logger log = LoggerFactory.getLogger(VehiculoService.class);

    private final SriVehiculoClient sri;
    private final ConsultaVehiculoRepository registro;

    public VehiculoService(SriVehiculoClient sri, ConsultaVehiculoRepository registro) {
        this.sri = sri;
        this.registro = registro;
    }

    public Optional<JsonNode> consultar(String placa) {
        String normalizada = placa == null ? "" : placa.toUpperCase().replace("-", "").trim();
        if (!normalizada.matches("[A-Z0-9]{5,8}")) {
            throw new IllegalArgumentException("Placa no valida");
        }
        Optional<JsonNode> vehiculo = sri.obtenerPorPlaca(normalizada);
        try {
            registro.save(new ConsultaVehiculo(normalizada, vehiculo.isPresent()));
        } catch (Exception e) {
            log.warn("No se pudo registrar la consulta: {}", e.getMessage());
        }
        return vehiculo;
    }
}
