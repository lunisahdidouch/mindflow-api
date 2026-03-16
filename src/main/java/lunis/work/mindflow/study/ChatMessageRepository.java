package lunis.work.mindflow.study;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByStudySetIdAndStudySetUserIdOrderByCreatedAtAsc(Long studySetId, Long userId);

    List<ChatMessage> findTop10ByStudySetIdAndStudySetUserIdOrderByCreatedAtDesc(Long studySetId, Long userId);
}
