package lunis.work.mindflow.ai;

import java.util.List;
import lunis.work.mindflow.config.AiProviderProperties;
import lunis.work.mindflow.config.SambanovaProperties;
import lunis.work.mindflow.config.XaiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Primary
public class SelectedAiChatClient implements AiChatClient {
    private final AiChatClient delegate;

    public SelectedAiChatClient(
            AiProviderProperties providerProperties,
            SambanovaProperties sambanovaProperties,
            XaiProperties xaiProperties,
            @Qualifier("sambanovaClient") AiChatClient sambanovaClient,
            @Qualifier("xaiClient") AiChatClient xaiClient) {
        String provider = normalize(providerProperties.provider());
        this.delegate = switch (provider) {
            case "sambanova" -> {
                requireConfigured(sambanovaProperties.baseUrl(), "SAMBANOVA_BASE_URL ontbreekt voor provider sambanova.");
                requireConfigured(sambanovaProperties.model(), "SAMBANOVA_MODEL ontbreekt voor provider sambanova.");
                yield sambanovaClient;
            }
            case "xai" -> {
                requireConfigured(xaiProperties.baseUrl(), "XAI_BASE_URL ontbreekt voor provider xai.");
                requireConfigured(xaiProperties.model(), "XAI_MODEL ontbreekt voor provider xai.");
                yield xaiClient;
            }
            default -> throw new IllegalStateException(
                    "Onbekende mindflow.ai.provider: " + provider + ". Gebruik sambanova of xai.");
        };
    }

    @Override
    public String chat(List<AiChatMessage> messages, AiChatOptions options) {
        return delegate.chat(messages, options);
    }

    private String normalize(String provider) {
        return provider == null ? "" : provider.trim().toLowerCase();
    }

    private void requireConfigured(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(message);
        }
    }
}
