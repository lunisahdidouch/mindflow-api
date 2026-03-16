package lunis.work.mindflow.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mindflow.xai")
public record XaiProperties(@NotBlank String baseUrl, @NotBlank String model, String apiKey) {
}
