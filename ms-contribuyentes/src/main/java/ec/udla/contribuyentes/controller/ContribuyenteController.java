package ec.udla.contribuyentes.controller;

import ec.udla.contribuyentes.dto.ContribuyenteResponse;
import ec.udla.contribuyentes.service.ContribuyenteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contribuyentes")
public class ContribuyenteController {

    private final ContribuyenteService service;

    public ContribuyenteController(ContribuyenteService service) {
        this.service = service;
    }

    /** GET /api/contribuyentes/{ruc}?correo=usuario@mail.com */
    @GetMapping("/{ruc}")
    public ContribuyenteResponse verificar(@PathVariable String ruc, @RequestParam String correo) {
        return service.verificar(ruc, correo);
    }
}
