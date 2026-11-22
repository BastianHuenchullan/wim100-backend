package cl.duoc.wim100.repository;

import cl.duoc.wim100.model.PuntajeCriterio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PuntajeCriterioRepository extends JpaRepository<PuntajeCriterio, Long> {
    List<PuntajeCriterio> findByEvaluacionId(Long evaluacionId);
}