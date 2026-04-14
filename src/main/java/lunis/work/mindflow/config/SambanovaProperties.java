package lunis.work.mindflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mindflow.sambanova")
public record SambanovaProperties(String baseUrl, String model, String apiKey) {
}
