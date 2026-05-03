package com.example.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Qualifier("fastApiWebClient")
    public WebClient fastApiWebClient(WebClient.Builder builder, FastApiProperties fastApiProperties) {
        return builder
                .baseUrl(fastApiProperties.getBaseUrl())
                .build();
    }
}
