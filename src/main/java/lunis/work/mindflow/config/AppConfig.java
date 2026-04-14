package lunis.work.mindflow.config;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Bean
    RestClient xaiRestClient(RestClient.Builder builder, XaiProperties properties) {
        return builder.baseUrl(properties.baseUrl()).build();
    }

    @Bean
    RestClient sambanovaRestClient(RestClient.Builder builder, SambanovaProperties properties) {
        return StringUtils.hasText(properties.baseUrl())
                ? builder.baseUrl(properties.baseUrl()).build()
                : builder.build();
    }

    @Bean
    RestClient geminiRestClient(RestClient.Builder builder, GeminiProperties properties) {
        return builder.baseUrl(properties.baseUrl()).build();
    }

    @Bean
    RestClientCustomizer restClientCustomizer(HttpMessageConverters converters) {
        return builder -> builder.messageConverters(list -> {
            list.clear();
            list.addAll(converters.getConverters());
            list.add(new MappingJackson2HttpMessageConverter());
        });
    }
}
