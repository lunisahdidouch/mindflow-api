package lunis.work.mindflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mindflow.ai")
public record AiProviderProperties(String provider) {
}
