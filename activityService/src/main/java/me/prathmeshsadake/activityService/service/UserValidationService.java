package me.prathmeshsadake.activityService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserValidationService {

    private final WebClient userServiceWebClient;

    public boolean validateUser(String userId) {
        try {
            log.info("Validating user id {}", userId);
            Boolean isValid = userServiceWebClient.get()
                    .uri("/{userId}/validate", userId)
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
