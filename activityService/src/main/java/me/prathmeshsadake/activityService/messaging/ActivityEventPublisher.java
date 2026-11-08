package me.prathmeshsadake.activityService.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.activityService.config.RabbitMQConfig;
import me.prathmeshsadake.activityService.dto.ActivityResponseDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishActivityCreated(ActivityResponseDto activity) {
        ActivityMessage message = ActivityMessage.builder()
                .id(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getActivityType() != null ? activity.getActivityType().name() : null)
                .duration(activity.getDuration())
                .caloriesBurned(activity.getCaloriesBurned())
                .startTime(activity.getStartTime())
                .additionalMetrics(activity.getAdditionalMetrics())
                .createdAt(activity.getCreatedAt())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                message
        );
        log.info("Published activity.created event for activityId={}", activity.getId());
    }
}
