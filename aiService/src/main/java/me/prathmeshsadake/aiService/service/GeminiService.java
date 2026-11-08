package me.prathmeshsadake.aiService.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.aiService.dto.ActivityResponseDto;
import me.prathmeshsadake.aiService.model.Recommendation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.model}")
    private String geminiModel;

    public Recommendation generateRecommendation(ActivityResponseDto activity) {
        String prompt = buildPrompt(activity);
        String rawText = callGemini(prompt);
        return parseRecommendation(activity, rawText);
    }

    private String buildPrompt(ActivityResponseDto activity) {
        return """
                You are a fitness coach AI. Analyze this workout activity and respond with ONLY valid JSON (no markdown).
                Use this exact schema:
                {
                  "recommendations": ["string"],
                  "suggestions": ["string"],
                  "safety": ["string"]
                }

                Activity details:
                - type: %s
                - durationMinutes: %s
                - caloriesBurned: %s
                - startTime: %s
                - additionalMetrics: %s

                Keep each list to 3-5 concise, actionable items.
                """.formatted(
                activity.getActivityType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getStartTime(),
                activity.getAdditionalMetrics()
        );
    }

    private String callGemini(String prompt) {
        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        try {
            String response = geminiWebClient.post()
                    .uri("/v1beta/models/{model}:generateContent", geminiModel)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty response from Gemini");
            }
            return textNode.asText();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to generate recommendation from Gemini");
        }
    }

    private Recommendation parseRecommendation(ActivityResponseDto activity, String rawText) {
        try {
            String json = extractJson(rawText);
            Map<String, List<String>> parsed = objectMapper.readValue(json, new TypeReference<>() {});

            return Recommendation.builder()
                    .userId(activity.getUserId())
                    .activityId(activity.getId())
                    .activityType(activity.getActivityType())
                    .recommendations(parsed.getOrDefault("recommendations", Collections.emptyList()))
                    .suggestions(parsed.getOrDefault("suggestions", Collections.emptyList()))
                    .safety(parsed.getOrDefault("safety", Collections.emptyList()))
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", rawText, e);
            Map<String, List<String>> fallback = new HashMap<>();
            fallback.put("recommendations", List.of(rawText));
            return Recommendation.builder()
                    .userId(activity.getUserId())
                    .activityId(activity.getId())
                    .activityType(activity.getActivityType())
                    .recommendations(fallback.get("recommendations"))
                    .suggestions(Collections.emptyList())
                    .safety(Collections.emptyList())
                    .build();
        }
    }

    private String extractJson(String rawText) {
        String trimmed = rawText.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }
}
