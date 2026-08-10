package com.programmers.kdt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient performanceRestClient(
            @Value("${performance-service.url}") String performanceServiceUrl
    ){
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        return RestClient.builder()
                .baseUrl(performanceServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
