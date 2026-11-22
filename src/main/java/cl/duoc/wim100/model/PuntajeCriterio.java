package cl.duoc.wim100.model;

import jakarta.persistence.*;

@Entity
@Table(name = "puntajes_criterio")
public class PuntajeCriterio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long evaluacionId;
    private Long criterioId;
    private Integer puntaje;

    @Column(length = 1000)
    private String justificacion;

    public PuntajeCriterio() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEvaluacionId() { return evaluacionId; }
    public void setEvaluacionId(Long evaluacionId) { this.evaluacionId = evaluacionId; }
    public Long getCriterioId() { return criterioId; }
    public void setCriterioId(Long criterioId) { this.criterioId = criterioId; }
    public Integer getPuntaje() { return puntaje; }
    public void setPuntaje(Integer puntaje) { this.puntaje = puntaje; }
    public String getJustificacion() { return justificacion; }
    public void setJustificacion(String justificacion) { this.justificacion = justificacion; }
}