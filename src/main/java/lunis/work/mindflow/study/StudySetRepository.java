package lunis.work.mindflow.study;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudySetRepository extends JpaRepository<StudySet, Long> {
    List<StudySet> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<StudySet> findByIdAndUserId(Long id, Long userId);
}
