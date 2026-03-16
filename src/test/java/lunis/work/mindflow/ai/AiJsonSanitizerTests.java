package lunis.work.mindflow.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AiJsonSanitizerTests {
    private final AiJsonSanitizer sanitizer = new AiJsonSanitizer();

    @Test
    void stripsMarkdownFenceBeforeReturningJson() {
        String raw = """
                ```json
                {
                  "topic": "Biologie"
                }
                ```
                """;

        assertThat(sanitizer.sanitize(raw)).isEqualTo("{\n  \"topic\": \"Biologie\"\n}");
    }
}
