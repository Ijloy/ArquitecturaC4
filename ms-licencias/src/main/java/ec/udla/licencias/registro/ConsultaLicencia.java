package ec.udla.licencias.registro;

import jakarta.persistence.*;

import java.time.Instant;

/** Fila de la tabla consulta_licencia ("Registra consultas de licencias"). */
@Entity
@Table(name = "consulta_licencia")
public class ConsultaLicencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cedula;
    private String placa;
    private String fuente;
    private Integer puntos;
    private Instant fechaConsulta;

    protected ConsultaLicencia() {
    }

    public ConsultaLicencia(String cedula, String placa, String fuente, Integer puntos) {
        this.cedula = cedula;
        this.placa = placa;
        this.fuente = fuente;
        this.puntos = puntos;
        this.fechaConsulta = Instant.now();
    }

    public Long getId() { return id; }
    public String getCedula() { return cedula; }
    public String getPlaca() { return placa; }
    public String getFuente() { return fuente; }
    public Integer getPuntos() { return puntos; }
    public Instant getFechaConsulta() { return fechaConsulta; }
}
