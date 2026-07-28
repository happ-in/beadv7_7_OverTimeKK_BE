package com.programmers.kdt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient performanceRestClient(
            @Value("${performance-service.url}") String performanceServiceUrl
    ){
        return RestClient.builder()
                .baseUrl(performanceServiceUrl)
                .build();
    }
}
