package lunis.work.mindflow.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import lunis.work.mindflow.config.SambanovaProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SambaNovaClientTests {

    @Test
    void sendsJsonModeRequestForStructuredOutputs() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.example.com/v1");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        SambaNovaClient client = new SambaNovaClient(
                restClient,
                new SambanovaProperties("https://api.example.com/v1", "MiniMax-M2.5", "secret"));

        server.expect(requestTo("https://api.example.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer secret"))
                .andExpect(content().string(containsString("\"model\":\"MiniMax-M2.5\"")))
                .andExpect(content().string(containsString("\"response_format\":{\"type\":\"json_object\"}")))
                .andRespond(withSuccess("""
                        {
                          "choices": [
                            {
                              "message": {
                                "role": "assistant",
                                "content": "{\\"topic\\":\\"Biologie\\"}"
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        String response = client.chat(
                List.of(
                        new AiChatMessage("system", "Geef JSON"),
                        new AiChatMessage("user", "Maak iets")),
                AiChatOptions.jsonMode(0.4));

        server.verify();
        assertThat(response).isEqualTo("{\"topic\":\"Biologie\"}");
    }
}
