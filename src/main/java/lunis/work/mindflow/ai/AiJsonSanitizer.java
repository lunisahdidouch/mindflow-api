package lunis.work.mindflow.ai;

import lunis.work.mindflow.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AiJsonSanitizer {

    public String sanitize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI-service gaf geen bruikbare inhoud terug.");
        }

        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "");
            cleaned = cleaned.replaceFirst("\\s*```$", "");
        }

        int objectStart = cleaned.indexOf('{');
        int objectEnd = cleaned.lastIndexOf('}');
        int arrayStart = cleaned.indexOf('[');
        int arrayEnd = cleaned.lastIndexOf(']');

        if (objectStart >= 0 && objectEnd > objectStart) {
            return cleaned.substring(objectStart, objectEnd + 1).trim();
        }
        if (arrayStart >= 0 && arrayEnd > arrayStart) {
            return cleaned.substring(arrayStart, arrayEnd + 1).trim();
        }

        throw new ApiException(HttpStatus.BAD_GATEWAY, "AI-service leverde geen geldige JSON terug.");
    }
}
