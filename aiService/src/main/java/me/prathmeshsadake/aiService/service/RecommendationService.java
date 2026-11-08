package me.prathmeshsadake.aiService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.aiService.dto.ActivityResponseDto;
import me.prathmeshsadake.aiService.dto.RecommendationResponseDto;
import me.prathmeshsadake.aiService.messaging.ActivityMessage;
import me.prathmeshsadake.aiService.model.Recommendation;
import me.prathmeshsadake.aiService.repository.RecommendationRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {

    private final ActivityClientService activityClientService;
    private final GeminiService geminiService;
    private final RecommendationRepository recommendationRepository;

    public List<ActivityResponseDto> getUserActivities(String userId) {
        return activityClientService.getUserActivities(userId);
    }

    public RecommendationResponseDto getRecommendationForActivity(String activityId) {
        return recommendationRepository.findFirstByActivityIdOrderByCreatedAtDesc(activityId)
                .map(this::mapToResponse)
                .orElseGet(() -> generateAndSaveFromActivityService(activityId));
    }

    public List<RecommendationResponseDto> getRecommendationsForUser(String userId) {
        return recommendationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RecommendationResponseDto getRecommendationById(String recommendationId) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Recommendation not found with id: " + recommendationId));
        return mapToResponse(recommendation);
    }

    public void processActivityMessage(ActivityMessage message) {
        if (message == null || message.getId() == null) {
            log.warn("Ignoring empty activity message");
            return;
        }

        if (recommendationRepository.existsByActivityId(message.getId())) {
            log.info("Recommendation already exists for activityId={}, skipping", message.getId());
            return;
        }

        ActivityResponseDto activity = toActivityDto(message);
        Recommendation generated = geminiService.generateRecommendation(activity);
        saveIgnoringDuplicate(generated);
    }

    private RecommendationResponseDto generateAndSaveFromActivityService(String activityId) {
        log.info("No cached recommendation found. Generating for activity {}", activityId);
        ActivityResponseDto activity = activityClientService.getActivityById(activityId);
        Recommendation generated = geminiService.generateRecommendation(activity);
        Recommendation saved = saveIgnoringDuplicate(generated);
        return mapToResponse(saved);
    }

    /**
     * RabbitMQ listener and HTTP on-demand path can race; unique activityId + this helper
     * makes the second writer reuse the winner's document instead of failing the API.
     */
    private Recommendation saveIgnoringDuplicate(Recommendation generated) {
        try {
            return recommendationRepository.save(generated);
        } catch (DuplicateKeyException ex) {
            log.info("Duplicate recommendation for activityId={}, loading existing", generated.getActivityId());
            return recommendationRepository.findFirstByActivityIdOrderByCreatedAtDesc(generated.getActivityId())
                    .orElseThrow(() -> ex);
        }
    }

    private ActivityResponseDto toActivityDto(ActivityMessage message) {
        ActivityResponseDto activity = new ActivityResponseDto();
        activity.setId(message.getId());
        activity.setUserId(message.getUserId());
        activity.setActivityType(message.getActivityType());
        activity.setDuration(message.getDuration());
        activity.setCaloriesBurned(message.getCaloriesBurned());
        activity.setStartTime(message.getStartTime());
        activity.setAdditionalMetrics(message.getAdditionalMetrics());
        activity.setCreatedAt(message.getCreatedAt());
        return activity;
    }

    private RecommendationResponseDto mapToResponse(Recommendation recommendation) {
        return RecommendationResponseDto.builder()
                .id(recommendation.getId())
                .userId(recommendation.getUserId())
                .activityId(recommendation.getActivityId())
                .activityType(recommendation.getActivityType())
                .recommendations(recommendation.getRecommendations())
                .suggestions(recommendation.getSuggestions())
                .safety(recommendation.getSafety())
                .createdAt(recommendation.getCreatedAt())
                .build();
    }
}
