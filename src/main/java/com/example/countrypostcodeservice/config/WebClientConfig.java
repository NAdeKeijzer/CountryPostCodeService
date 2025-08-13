package com.example.countrypostcodeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient restCountriesWebClient() {
        return WebClient.builder()
                .baseUrl("https://restcountries.com")
                .build();
    }
}
