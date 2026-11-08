package me.prathmeshsadake.aiService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.aiService.dto.ActivityResponseDto;
import me.prathmeshsadake.aiService.dto.RecommendationResponseDto;
import me.prathmeshsadake.aiService.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/activities/user/{userId}")
    public ResponseEntity<List<ActivityResponseDto>> getUserActivities(@PathVariable String userId) {
        log.info("Get activities for user {}", userId);
        return ResponseEntity.ok(recommendationService.getUserActivities(userId));
    }

    @GetMapping("/recommendations/activity/{activityId}")
    public ResponseEntity<RecommendationResponseDto> getRecommendationByActivity(
            @PathVariable String activityId) {
        log.info("Get recommendation for activity {}", activityId);
        return ResponseEntity.ok(recommendationService.getRecommendationForActivity(activityId));
    }

    @GetMapping("/recommendations/user/{userId}")
    public ResponseEntity<List<RecommendationResponseDto>> getRecommendationsByUser(
            @PathVariable String userId) {
        log.info("Get recommendations for user {}", userId);
        return ResponseEntity.ok(recommendationService.getRecommendationsForUser(userId));
    }

    @GetMapping("/recommendations/{recommendationId}")
    public ResponseEntity<RecommendationResponseDto> getRecommendationById(
            @PathVariable String recommendationId) {
        log.info("Get recommendation {}", recommendationId);
        return ResponseEntity.ok(recommendationService.getRecommendationById(recommendationId));
    }
}
