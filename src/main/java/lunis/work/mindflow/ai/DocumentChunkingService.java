package lunis.work.mindflow.ai;

import java.util.ArrayList;
import java.util.List;
import lunis.work.mindflow.config.ChunkingProperties;
import org.springframework.stereotype.Service;

@Service
public class DocumentChunkingService {
    private final ChunkingProperties properties;

    public DocumentChunkingService(ChunkingProperties properties) {
        this.properties = properties;
    }

    public List<String> chunk(String input) {
        String normalized = input.replaceAll("\\s+", " ").trim();
        if (normalized.isBlank()) {
            return List.of();
        }

        List<String> chunks = new ArrayList<>();
        int chunkSize = properties.chunkSize();
        int overlap = Math.min(properties.overlap(), chunkSize / 2);
        int start = 0;

        while (start < normalized.length()) {
            int end = Math.min(normalized.length(), start + chunkSize);
            if (end < normalized.length()) {
                int candidate = normalized.lastIndexOf(' ', end);
                if (candidate > start + (chunkSize / 2)) {
                    end = candidate;
                }
            }

            String chunk = normalized.substring(start, end).trim();
            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            if (end >= normalized.length()) {
                break;
            }

            start = Math.max(0, end - overlap);
        }

        if (chunks.size() > 1) {
            String last = chunks.get(chunks.size() - 1);
            if (last.length() < Math.max(120, chunkSize / 4)) {
                String merged = chunks.get(chunks.size() - 2) + " " + last;
                chunks.set(chunks.size() - 2, merged.trim());
                chunks.remove(chunks.size() - 1);
            }
        }

        return chunks;
    }
}
