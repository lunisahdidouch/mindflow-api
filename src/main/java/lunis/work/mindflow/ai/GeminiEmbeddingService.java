package lunis.work.mindflow.ai;

import java.util.List;
import lunis.work.mindflow.common.ApiException;
import lunis.work.mindflow.config.GeminiProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class GeminiEmbeddingService {
    private final RestClient geminiRestClient;
    private final GeminiProperties properties;

    public GeminiEmbeddingService(RestClient geminiRestClient, GeminiProperties properties) {
        this.geminiRestClient = geminiRestClient;
        this.properties = properties;
    }

    public float[] embed(String text) {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "GEMINI_API_KEY ontbreekt in de backend configuratie.");
        }

        GeminiEmbedResponse response;
        try {
            response = geminiRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/{model}:embedContent")
                            .queryParam("key", properties.apiKey())
                            .build(properties.model()))
                    .body(new GeminiEmbedRequest(new Content(List.of(new Part(text))), "RETRIEVAL_DOCUMENT", 768))
                    .retrieve()
                    .body(GeminiEmbedResponse.class);
        } catch (RestClientResponseException exception) {
            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Gemini embeddings request mislukte: "
                            + compactMessage(exception.getResponseBodyAsString(), exception.getStatusText()));
        } catch (RestClientException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Kon Gemini embeddings niet bereiken vanaf de backend.");
        }

        if (response == null || response.embedding() == null || response.embedding().values() == null) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Gemini embeddings service gaf geen vector terug.");
        }

        List<Double> values = response.embedding().values();
        float[] vector = new float[values.size()];
        double norm = 0D;
        for (int i = 0; i < values.size(); i++) {
            vector[i] = values.get(i).floatValue();
            norm += vector[i] * vector[i];
        }

        float divisor = (float) Math.sqrt(norm == 0 ? 1 : norm);
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / divisor;
        }
        return vector;
    }

    private String compactMessage(String responseBody, String fallback) {
        if (StringUtils.hasText(responseBody)) {
            String compact = responseBody.replaceAll("\\s+", " ").trim();
            return compact.length() > 220 ? compact.substring(0, 220) + "..." : compact;
        }
        return fallback;
    }

    record GeminiEmbedRequest(Content content, String taskType, Integer outputDimensionality) {
    }

    record Content(List<Part> parts) {
    }

    record Part(String text) {
    }

    record GeminiEmbedResponse(Embedding embedding) {
    }

    record Embedding(List<Double> values) {
    }
}
