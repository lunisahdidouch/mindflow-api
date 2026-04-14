package lunis.work.mindflow.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import lunis.work.mindflow.config.AiProviderProperties;
import lunis.work.mindflow.config.SambanovaProperties;
import lunis.work.mindflow.config.XaiProperties;
import org.junit.jupiter.api.Test;

class SelectedAiChatClientTests {

    @Test
    void selectsSambanovaWhenConfigured() {
        AiChatClient sambanova = (messages, options) -> "sambanova";
        AiChatClient xai = (messages, options) -> "xai";

        SelectedAiChatClient client = new SelectedAiChatClient(
                new AiProviderProperties("sambanova"),
                new SambanovaProperties("https://api.example.com/v1", "MiniMax-M2.5", "key"),
                new XaiProperties("https://api.x.ai", "grok-4-1-fast-reasoning", "key"),
                sambanova,
                xai);

        assertThat(client.chat(List.of(new AiChatMessage("user", "hi")), AiChatOptions.standard(0.4)))
                .isEqualTo("sambanova");
    }

    @Test
    void selectsXaiWhenConfigured() {
        AiChatClient sambanova = (messages, options) -> "sambanova";
        AiChatClient xai = (messages, options) -> "xai";

        SelectedAiChatClient client = new SelectedAiChatClient(
                new AiProviderProperties("xai"),
                new SambanovaProperties("https://api.example.com/v1", "MiniMax-M2.5", "key"),
                new XaiProperties("https://api.x.ai", "grok-4-1-fast-reasoning", "key"),
                sambanova,
                xai);

        assertThat(client.chat(List.of(new AiChatMessage("user", "hi")), AiChatOptions.standard(0.4)))
                .isEqualTo("xai");
    }

    @Test
    void rejectsUnknownProvider() {
        AiChatClient sambanova = (messages, options) -> "sambanova";
        AiChatClient xai = (messages, options) -> "xai";

        assertThatThrownBy(() -> new SelectedAiChatClient(
                new AiProviderProperties("unknown"),
                new SambanovaProperties("https://api.example.com/v1", "MiniMax-M2.5", "key"),
                new XaiProperties("https://api.x.ai", "grok-4-1-fast-reasoning", "key"),
                sambanova,
                xai)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Onbekende mindflow.ai.provider");
    }
}
