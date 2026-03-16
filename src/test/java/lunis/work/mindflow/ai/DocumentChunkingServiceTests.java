package lunis.work.mindflow.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import lunis.work.mindflow.config.ChunkingProperties;
import org.junit.jupiter.api.Test;

class DocumentChunkingServiceTests {

    @Test
    void createsOverlappingChunksAndMergesTinyTail() {
        DocumentChunkingService service = new DocumentChunkingService(new ChunkingProperties(40, 8, 5));
        String content = "Alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu nu xi omicron pi rho sigma tau";

        List<String> chunks = service.chunk(content);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks.get(0)).contains("Alpha beta");
        assertThat(chunks.get(chunks.size() - 1).length()).isGreaterThanOrEqualTo(10);
    }
}
