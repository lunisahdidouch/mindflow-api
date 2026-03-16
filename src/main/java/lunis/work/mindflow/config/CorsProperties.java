package lunis.work.mindflow.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mindflow.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
