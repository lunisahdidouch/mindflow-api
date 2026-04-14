package lunis.work.mindflow.ai;

import java.util.List;
import lunis.work.mindflow.common.ApiException;
import lunis.work.mindflow.config.XaiProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component("xaiClient")
public class XaiClient implements AiChatClient {
    private final RestClient restClient;
    private final XaiProperties properties;

    public XaiClient(RestClient xaiRestClient, XaiProperties properties) {
        this.restClient = xaiRestClient;
        this.properties = properties;
    }

    @Override
    public String chat(List<AiChatMessage> messages, AiChatOptions options) {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "XAI_API_KEY ontbreekt in de backend configuratie.");
        }

        ChatCompletionResponse response;
        try {
            response = restClient.post()
                    .uri("/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .body(new ChatCompletionRequest(
                            properties.model(),
                            messages,
                            options.temperature() == null ? 0.4 : options.temperature()))
                    .retrieve()
                    .body(ChatCompletionResponse.class);
        } catch (RestClientResponseException exception) {
            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "xAI request mislukte: " + compactMessage(exception.getResponseBodyAsString(), exception.getStatusText()));
        } catch (RestClientException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Kon xAI niet bereiken vanaf de backend.");
        }

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "xAI gaf geen antwoord terug.");
        }

        return response.choices().get(0).message().content();
    }

    private String compactMessage(String responseBody, String fallback) {
        if (StringUtils.hasText(responseBody)) {
            String compact = responseBody.replaceAll("\\s+", " ").trim();
            return compact.length() > 220 ? compact.substring(0, 220) + "..." : compact;
        }
        return fallback;
    }

    record ChatCompletionRequest(String model, List<AiChatMessage> messages, Double temperature) {
    }

    record ChatCompletionResponse(List<Choice> choices) {
    }

    record Choice(AiChatMessage message) {
    }
}
