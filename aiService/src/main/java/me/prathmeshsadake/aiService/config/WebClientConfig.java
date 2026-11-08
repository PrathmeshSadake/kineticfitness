package me.prathmeshsadake.aiService.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient activityServiceWebClient(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder
                .clone()
                .baseUrl("http://activityService/api/v1/activities")
                .build();
    }

    @Bean
    public WebClient geminiWebClient(
            @Value("${gemini.base-url}") String geminiBaseUrl,
            @Value("${gemini.api-key}") String geminiApiKey) {
        return WebClient.builder()
                .baseUrl(geminiBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("x-goog-api-key", geminiApiKey)
                .build();
    }
}
