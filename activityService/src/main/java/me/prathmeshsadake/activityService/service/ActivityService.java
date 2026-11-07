package me.prathmeshsadake.activityService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.activityService.dto.ActivityRequestDto;
import me.prathmeshsadake.activityService.dto.ActivityResponseDto;
import me.prathmeshsadake.activityService.model.Activity;
import me.prathmeshsadake.activityService.repository.ActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityResponseDto trackActivity(ActivityRequestDto activityRequestDto) {
        Activity activity = Activity.builder()
                .userId(activityRequestDto.getUserId())
                .activityType(activityRequestDto.getType())
                .additionalMetrics(activityRequestDto.getAdditionalMetrics())
                .caloriesBurned(activityRequestDto.getCaloriesBurned())
                .startTime(activityRequestDto.getStartTime())
                .duration(activityRequestDto.getDuration())
                .build();

        Activity savedActivity = activityRepository.save(activity);
        return mapToResponse(savedActivity);
    }

    public List<ActivityResponseDto> getUserActivities(String userId) {
        return activityRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ActivityResponseDto getActivityById(String activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found with id: " + activityId));
        return mapToResponse(activity);
    }

    private ActivityResponseDto mapToResponse(Activity activity) {
        ActivityResponseDto response = new ActivityResponseDto();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setActivityType(activity.getActivityType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }
}
