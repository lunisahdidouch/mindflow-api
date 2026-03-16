package lunis.work.mindflow.study;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PracticeTestRepository extends JpaRepository<PracticeTest, Long> {
    Optional<PracticeTest> findFirstByStudySetIdAndStudySetUserIdOrderByCreatedAtDesc(Long studySetId, Long userId);
}
