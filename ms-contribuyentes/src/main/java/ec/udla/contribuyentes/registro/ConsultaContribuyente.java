package ec.udla.contribuyentes.registro;

import jakarta.persistence.*;

import java.time.Instant;

/** Fila de la tabla consulta_contribuyente (Base de datos PostgreSQL - Registro de consultas). */
@Entity
@Table(name = "consulta_contribuyente")
public class ConsultaContribuyente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ruc;
    private String correo;
    private boolean esContribuyente;
    private boolean esPersonaNatural;
    private Instant fechaConsulta;

    protected ConsultaContribuyente() {
    }

    public ConsultaContribuyente(String ruc, String correo, boolean esContribuyente, boolean esPersonaNatural) {
        this.ruc = ruc;
        this.correo = correo;
        this.esContribuyente = esContribuyente;
        this.esPersonaNatural = esPersonaNatural;
        this.fechaConsulta = Instant.now();
    }

    public Long getId() { return id; }
    public String getRuc() { return ruc; }
    public String getCorreo() { return correo; }
    public boolean isEsContribuyente() { return esContribuyente; }
    public boolean isEsPersonaNatural() { return esPersonaNatural; }
    public Instant getFechaConsulta() { return fechaConsulta; }
}
