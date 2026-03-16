package lunis.work.mindflow.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mindflow.chunking")
public record ChunkingProperties(@Min(256) int chunkSize, @Min(0) int overlap, @Min(1) int retrievalTopK) {
}
