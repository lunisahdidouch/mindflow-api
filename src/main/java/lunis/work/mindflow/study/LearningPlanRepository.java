package lunis.work.mindflow.study;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningPlanRepository extends JpaRepository<LearningPlan, Long> {
    Optional<LearningPlan> findFirstByStudySetIdAndStudySetUserIdOrderByCreatedAtDesc(Long studySetId, Long userId);
}
