package me.prathmeshsadake.activityService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.activityService.dto.ActivityRequestDto;
import me.prathmeshsadake.activityService.dto.ActivityResponseDto;
import me.prathmeshsadake.activityService.service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponseDto> trackActivity(
            @RequestBody ActivityRequestDto activityRequestDto,
            Authentication authentication) {
        String userId = requireUserId(authentication);
        activityRequestDto.setUserId(userId);
        log.info("Tracking activity for user {}", userId);
        return ResponseEntity.ok(activityService.trackActivity(activityRequestDto));
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponseDto>> getUserActivities(Authentication authentication) {
        String userId = requireUserId(authentication);
        return ResponseEntity.ok(activityService.getUserActivities(userId));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponseDto> getActivity(
            @PathVariable String activityId,
            Authentication authentication) {
        String userId = requireUserId(authentication);
        ActivityResponseDto activity = activityService.getActivityById(activityId);
        if (!userId.equals(activity.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this activity");
        }
        return ResponseEntity.ok(activity);
    }

    private String requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication");
        }
        return authentication.getName();
    }
}
