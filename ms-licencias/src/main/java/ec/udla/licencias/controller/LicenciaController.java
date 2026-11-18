package ec.udla.licencias.controller;

import ec.udla.licencias.dto.LicenciaResponse;
import ec.udla.licencias.service.LicenciaService;
import org.springframework.web.bind.annotation.*;

/** LicenciaController: recibe del API Gateway la solicitud de consulta de puntos. */
@RestController
@RequestMapping("/api/licencias")
public class LicenciaController {

    private final LicenciaService licenciaService;

    public LicenciaController(LicenciaService licenciaService) {
        this.licenciaService = licenciaService;
    }

    /** GET /api/licencias/{cedula}?placa=PBA1234 */
    @GetMapping("/{cedula}")
    public LicenciaResponse consultarPuntos(@PathVariable String cedula,
                                            @RequestParam(defaultValue = "") String placa) {
        return licenciaService.consultarPuntos(cedula, placa);
    }
}
