package lunis.work.mindflow.study;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByStudySetIdAndStudySetUserIdOrderByIdAsc(Long studySetId, Long userId);

    long countByStudySetId(Long studySetId);
}
