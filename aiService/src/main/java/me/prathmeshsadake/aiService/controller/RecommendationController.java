package me.prathmeshsadake.aiService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.aiService.dto.ActivityResponseDto;
import me.prathmeshsadake.aiService.dto.RecommendationResponseDto;
import me.prathmeshsadake.aiService.service.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/activities/user/{userId}")
    public ResponseEntity<List<ActivityResponseDto>> getUserActivities(
            @PathVariable String userId,
            Authentication authentication) {
        assertSelf(authentication, userId);
        log.info("Get activities for user {}", userId);
        return ResponseEntity.ok(recommendationService.getUserActivities(userId));
    }

    @GetMapping("/recommendations/activity/{activityId}")
    public ResponseEntity<RecommendationResponseDto> getRecommendationByActivity(
            @PathVariable String activityId,
            Authentication authentication) {
        log.info("Get recommendation for activity {}", activityId);
        RecommendationResponseDto recommendation =
                recommendationService.getRecommendationForActivity(activityId);
        assertSelf(authentication, recommendation.getUserId());
        return ResponseEntity.ok(recommendation);
    }

    @GetMapping("/recommendations/user/{userId}")
    public ResponseEntity<List<RecommendationResponseDto>> getRecommendationsByUser(
            @PathVariable String userId,
            Authentication authentication) {
        assertSelf(authentication, userId);
        log.info("Get recommendations for user {}", userId);
        return ResponseEntity.ok(recommendationService.getRecommendationsForUser(userId));
    }

    @GetMapping("/recommendations/{recommendationId}")
    public ResponseEntity<RecommendationResponseDto> getRecommendationById(
            @PathVariable String recommendationId,
            Authentication authentication) {
        RecommendationResponseDto recommendation =
                recommendationService.getRecommendationById(recommendationId);
        assertSelf(authentication, recommendation.getUserId());
        return ResponseEntity.ok(recommendation);
    }

    private void assertSelf(Authentication authentication, String userId) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication");
        }
        if (!authentication.getName().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }
    }
}
