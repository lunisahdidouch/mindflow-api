package lunis.work.mindflow.study;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByStudySetIdAndStudySetUserIdOrderByIdAsc(Long studySetId, Long userId);

    long countByStudySetId(Long studySetId);
}
