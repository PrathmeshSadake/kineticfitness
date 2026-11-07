package me.prathmeshsadake.activityService.dto;

import lombok.Data;
import me.prathmeshsadake.activityService.enums.ActivityType;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ActivityResponseDto {
    private String id;
    private String userId;
    private ActivityType activityType;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
