package cl.duoc.wim100.controller;

import cl.duoc.wim100.model.Evaluacion;
import cl.duoc.wim100.model.PuntajeCriterio;
import cl.duoc.wim100.repository.EvaluacionRepository;
import cl.duoc.wim100.repository.PuntajeCriterioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/evaluaciones")
@CrossOrigin(origins = "*")
public class EvaluacionController {

    private final EvaluacionRepository evaluacionRepo;
    private final PuntajeCriterioRepository puntajeRepo;

    public EvaluacionController(EvaluacionRepository evaluacionRepo, PuntajeCriterioRepository puntajeRepo) {
        this.evaluacionRepo = evaluacionRepo;
        this.puntajeRepo = puntajeRepo;
    }

    public static class PuntajeDto {
        public Long criterioId;
        public Integer puntaje;
        public String justificacion;
    }

    public static class EvaluacionDto {
        public Long id;
        public Long postulanteId;
        public String evaluadora;
        public String evaluadoraClave;
        public Double puntajeTotal;
        public String observacionGeneral;
        public Long fecha;
        public List<PuntajeDto> puntajes = new ArrayList<>();
    }

    @GetMapping
    public List<EvaluacionDto> listarTodas() {
        List<EvaluacionDto> resultado = new ArrayList<>();
        for (Evaluacion ev : evaluacionRepo.findAll()) {
            EvaluacionDto dto = new EvaluacionDto();
            dto.id = ev.getId();
            dto.postulanteId = ev.getPostulanteId();
            dto.evaluadora = ev.getEvaluadora();
            dto.evaluadoraClave = ev.getEvaluadoraClave();
            dto.puntajeTotal = ev.getPuntajeTotal();
            dto.observacionGeneral = ev.getObservacionGeneral();
            dto.fecha = ev.getFecha();

            for (PuntajeCriterio p : puntajeRepo.findByEvaluacionId(ev.getId())) {
                PuntajeDto pDto = new PuntajeDto();
                pDto.criterioId = p.getCriterioId();
                pDto.puntaje = p.getPuntaje();
                pDto.justificacion = p.getJustificacion();
                dto.puntajes.add(pDto);
            }
            resultado.add(dto);
        }
        return resultado;
    }

    @PostMapping
    public ResponseEntity<EvaluacionDto> guardar(@RequestBody EvaluacionDto body) {
        // Si la misma evaluadora edita su evaluación anterior, se actualiza en vez de duplicarla
        Evaluacion ev = evaluacionRepo
                .findByPostulanteIdAndEvaluadoraClave(body.postulanteId, body.evaluadoraClave)
                .orElse(new Evaluacion());

        ev.setPostulanteId(body.postulanteId);
        ev.setEvaluadora(body.evaluadora);
        ev.setEvaluadoraClave(body.evaluadoraClave);
        ev.setPuntajeTotal(body.puntajeTotal);
        ev.setObservacionGeneral(body.observacionGeneral);
        ev.setFecha(body.fecha != null ? body.fecha : System.currentTimeMillis());

        Evaluacion guardada = evaluacionRepo.save(ev);

        // Reemplaza los puntajes anteriores de esa evaluación
        List<PuntajeCriterio> anteriores = puntajeRepo.findByEvaluacionId(guardada.getId());
        puntajeRepo.deleteAll(anteriores);

        if (body.puntajes != null) {
            for (PuntajeDto pDto : body.puntajes) {
                PuntajeCriterio pc = new PuntajeCriterio();
                pc.setEvaluacionId(guardada.getId());
                pc.setCriterioId(pDto.criterioId);
                pc.setPuntaje(pDto.puntaje);
                pc.setJustificacion(pDto.justificacion);
                puntajeRepo.save(pc);
            }
        }

        body.id = guardada.getId();
        return ResponseEntity.ok(body);
    }
}