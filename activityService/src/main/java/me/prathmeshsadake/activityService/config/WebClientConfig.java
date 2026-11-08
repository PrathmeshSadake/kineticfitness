package me.prathmeshsadake.activityService.config;

import org.springframework.beans.factory.annotation.Qualifier;
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
    public WebClient userServiceWebClient(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder
                .clone()
                .baseUrl("http://userService/api/v1/users")
                .build();
    }
}
