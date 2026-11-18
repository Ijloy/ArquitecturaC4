package ec.udla.vehiculos.registro;

import jakarta.persistence.*;

import java.time.Instant;

/** Fila de la tabla consulta_vehiculo (Registro de consultas vehiculares). */
@Entity
@Table(name = "consulta_vehiculo")
public class ConsultaVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String placa;
    private boolean encontrado;
    private Instant fechaConsulta;

    protected ConsultaVehiculo() {
    }

    public ConsultaVehiculo(String placa, boolean encontrado) {
        this.placa = placa;
        this.encontrado = encontrado;
        this.fechaConsulta = Instant.now();
    }

    public Long getId() { return id; }
    public String getPlaca() { return placa; }
    public boolean isEncontrado() { return encontrado; }
    public Instant getFechaConsulta() { return fechaConsulta; }
}
