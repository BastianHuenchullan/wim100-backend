package cl.duoc.wim100.repository;

import cl.duoc.wim100.model.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {
    Optional<Evaluacion> findByPostulanteIdAndEvaluadoraClave(Long postulanteId, String evaluadoraClave);
}