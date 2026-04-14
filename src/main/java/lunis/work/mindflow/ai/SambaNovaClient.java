package lunis.work.mindflow.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lunis.work.mindflow.common.ApiException;
import lunis.work.mindflow.config.SambanovaProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component("sambanovaClient")
public class SambaNovaClient implements AiChatClient {
    private final RestClient restClient;
    private final SambanovaProperties properties;

    public SambaNovaClient(RestClient sambanovaRestClient, SambanovaProperties properties) {
        this.restClient = sambanovaRestClient;
        this.properties = properties;
    }

    @Override
    public String chat(List<AiChatMessage> messages, AiChatOptions options) {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "SAMBANOVA_API_KEY ontbreekt in de backend configuratie.");
        }
        if (!StringUtils.hasText(properties.baseUrl())) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "SAMBANOVA_BASE_URL ontbreekt in de backend configuratie.");
        }
        if (!StringUtils.hasText(properties.model())) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "SAMBANOVA_MODEL ontbreekt in de backend configuratie.");
        }

        ChatCompletionResponse response;
        try {
            response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .body(new ChatCompletionRequest(
                            properties.model(),
                            messages,
                            clampTemperature(options.temperature()),
                            options.jsonMode() ? new ResponseFormat("json_object") : null))
                    .retrieve()
                    .body(ChatCompletionResponse.class);
        } catch (RestClientResponseException exception) {
            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "SambaNova request mislukte: "
                            + compactMessage(exception.getResponseBodyAsString(), exception.getStatusText()));
        } catch (RestClientException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Kon SambaNova niet bereiken vanaf de backend.");
        }

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "SambaNova gaf geen antwoord terug.");
        }

        ChatMessage message = response.choices().get(0).message();
        if (message == null || !StringUtils.hasText(message.content())) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "SambaNova gaf geen berichtinhoud terug.");
        }

        return message.content();
    }

    private Double clampTemperature(Double temperature) {
        if (temperature == null) {
            return 0.4D;
        }
        return Math.max(0D, Math.min(1D, temperature));
    }

    private String compactMessage(String responseBody, String fallback) {
        if (StringUtils.hasText(responseBody)) {
            String compact = responseBody.replaceAll("\\s+", " ").trim();
            return compact.length() > 220 ? compact.substring(0, 220) + "..." : compact;
        }
        return fallback;
    }

    record ChatCompletionRequest(
            String model,
            List<AiChatMessage> messages,
            Double temperature,
            @JsonProperty("response_format") ResponseFormat responseFormat) {
    }

    record ResponseFormat(String type) {
    }

    record ChatCompletionResponse(List<Choice> choices) {
    }

    record Choice(ChatMessage message) {
    }

    record ChatMessage(String role, String content) {
    }
}
