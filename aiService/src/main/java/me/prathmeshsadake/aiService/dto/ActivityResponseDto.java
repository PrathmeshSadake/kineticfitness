package me.prathmeshsadake.aiService.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ActivityResponseDto {
    private String id;
    private String userId;
    private String activityType;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
