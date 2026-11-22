package cl.duoc.wim100.model;

import jakarta.persistence.*;

@Entity
@Table(name = "evaluaciones")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postulanteId;
    private String evaluadora;
    private String evaluadoraClave;
    private Double puntajeTotal;

    @Column(length = 1000)
    private String observacionGeneral;

    private Long fecha;

    public Evaluacion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPostulanteId() { return postulanteId; }
    public void setPostulanteId(Long postulanteId) { this.postulanteId = postulanteId; }
    public String getEvaluadora() { return evaluadora; }
    public void setEvaluadora(String evaluadora) { this.evaluadora = evaluadora; }
    public String getEvaluadoraClave() { return evaluadoraClave; }
    public void setEvaluadoraClave(String evaluadoraClave) { this.evaluadoraClave = evaluadoraClave; }
    public Double getPuntajeTotal() { return puntajeTotal; }
    public void setPuntajeTotal(Double puntajeTotal) { this.puntajeTotal = puntajeTotal; }
    public String getObservacionGeneral() { return observacionGeneral; }
    public void setObservacionGeneral(String observacionGeneral) { this.observacionGeneral = observacionGeneral; }
    public Long getFecha() { return fecha; }
    public void setFecha(Long fecha) { this.fecha = fecha; }
}