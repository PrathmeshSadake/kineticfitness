package me.prathmeshsadake.aiService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.aiService.dto.ActivityResponseDto;
import me.prathmeshsadake.aiService.dto.RecommendationResponseDto;
import me.prathmeshsadake.aiService.model.Recommendation;
import me.prathmeshsadake.aiService.repository.RecommendationRepository;
import org.springframework.stereotype.Service;

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
        return recommendationRepository.findByActivityId(activityId)
                .map(this::mapToResponse)
                .orElseGet(() -> generateAndSave(activityId));
    }

    private RecommendationResponseDto generateAndSave(String activityId) {
        log.info("Generating AI recommendation for activity {}", activityId);
        ActivityResponseDto activity = activityClientService.getActivityById(activityId);
        Recommendation generated = geminiService.generateRecommendation(activity);
        Recommendation saved = recommendationRepository.save(generated);
        return mapToResponse(saved);
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
