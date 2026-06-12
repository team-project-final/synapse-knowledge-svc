package com.synapse.knowledge.note.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(EngagementProperties.class)
public class EngagementClientConfig {

    @Bean
    RestClient engagementRestClient(RestClient.Builder builder, EngagementProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.timeout());
        requestFactory.setReadTimeout(properties.timeout());

        return builder
            .baseUrl(properties.baseUrl())
            .requestFactory(requestFactory)
            .build();
    }
}
