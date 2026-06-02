package com.synapse.knowledge.search.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SearchProperties.class)
public class SearchClientConfig {

    @Bean
    @ConditionalOnMissingBean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    RestClient learningAiRestClient(RestClient.Builder builder, SearchProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.ai().timeout());
        requestFactory.setReadTimeout(properties.ai().timeout());

        return builder
            .baseUrl(properties.ai().baseUrl())
            .requestFactory(requestFactory)
            .build();
    }
}
