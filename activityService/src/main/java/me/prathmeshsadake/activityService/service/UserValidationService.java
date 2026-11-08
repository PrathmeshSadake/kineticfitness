package me.prathmeshsadake.activityService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class UserValidationService {

    private final WebClient userServiceWebClient;

    public UserValidationService(@Qualifier("userServiceWebClient") WebClient userServiceWebClient) {
        this.userServiceWebClient = userServiceWebClient;
    }

    public boolean validateUser(String userId) {
        try {
            log.info("Validating user id {}", userId);
            Boolean isValid = userServiceWebClient.get()
                    .uri("/{userId}/validate", userId)
                    .headers(headers -> {
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        if (authentication != null && authentication.getCredentials() instanceof String token
                                && !token.isBlank()) {
                            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                        }
                        if (authentication != null && authentication.getName() != null) {
                            headers.set("X-User-Id", authentication.getName());
                        }
                    })
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(isValid);
        } catch (WebClientResponseException e) {
            log.error("Error validating user {}: {}", userId, e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "User service unavailable");
        } catch (Exception e) {
            log.error("Failed to reach user service for user {}", userId, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "User service unavailable");
        }
    }
}
