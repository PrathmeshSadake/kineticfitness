package me.prathmeshsadake.aiService.service;

import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.aiService.dto.ActivityResponseDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Slf4j
public class ActivityClientService {

    private final WebClient activityServiceWebClient;

    public ActivityClientService(
            @Qualifier("activityServiceWebClient") WebClient activityServiceWebClient) {
        this.activityServiceWebClient = activityServiceWebClient;
    }

    public List<ActivityResponseDto> getUserActivities(String userId) {
        try {
            log.info("Fetching activities for user {}", userId);
            return activityServiceWebClient.get()
                    .uri("/")
                    .headers(this::propagateAuth)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ActivityResponseDto>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch activities for user {}: {}", userId, e.getStatusCode());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to fetch user activities");
        } catch (Exception e) {
            log.error("Activity service unreachable for user {}", userId, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Activity service unavailable");
        }
    }

    public ActivityResponseDto getActivityById(String activityId) {
        try {
            log.info("Fetching activity {}", activityId);
            return activityServiceWebClient.get()
                    .uri("/{activityId}", activityId)
                    .headers(this::propagateAuth)
                    .retrieve()
                    .bodyToMono(ActivityResponseDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found with id: " + activityId);
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch activity {}: {}", activityId, e.getStatusCode());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to fetch activity");
        } catch (Exception e) {
            log.error("Activity service unreachable for activity {}", activityId, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Activity service unavailable");
        }
    }

    private void propagateAuth(HttpHeaders headers) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String token
                && !token.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        if (authentication != null && authentication.getName() != null) {
            headers.set("X-User-Id", authentication.getName());
        }
    }
}
