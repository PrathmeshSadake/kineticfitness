package me.prathmeshsadake.aiService.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.aiService.config.RabbitMQConfig;
import me.prathmeshsadake.aiService.service.RecommendationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final RecommendationService recommendationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void onActivityCreated(ActivityMessage message) {
        log.info("Received activity.created message for activityId={}", message.getId());
        recommendationService.processActivityMessage(message);
    }
}
