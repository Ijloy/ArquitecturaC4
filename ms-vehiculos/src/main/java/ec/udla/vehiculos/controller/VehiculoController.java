package ec.udla.vehiculos.controller;

import com.fasterxml.jackson.databind.JsonNode;
import ec.udla.vehiculos.service.VehiculoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService service;

    public VehiculoController(VehiculoService service) {
        this.service = service;
    }

    /** GET /api/vehiculos/{placa} */
    @GetMapping("/{placa}")
    public ResponseEntity<JsonNode> consultar(@PathVariable String placa) {
        return service.consultar(placa)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
