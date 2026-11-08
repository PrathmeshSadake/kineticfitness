package me.prathmeshsadake.aiService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RecommendationResponseDto {
    private String id;
    private String userId;
    private String activityId;
    private String activityType;
    private List<String> recommendations;
    private List<String> suggestions;
    private List<String> safety;
    private LocalDateTime createdAt;
}
