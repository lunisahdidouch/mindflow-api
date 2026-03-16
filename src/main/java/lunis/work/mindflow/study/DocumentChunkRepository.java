package lunis.work.mindflow.study;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    @Query(value = """
            SELECT dc.*
            FROM document_chunks dc
            JOIN study_sets ss ON ss.id = dc.study_set_id
            WHERE dc.study_set_id = :studySetId
              AND ss.user_id = :userId
            ORDER BY dc.embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<DocumentChunk> findNearestChunks(
            @Param("studySetId") Long studySetId,
            @Param("userId") Long userId,
            @Param("embedding") String embedding,
            @Param("limit") int limit);
}
