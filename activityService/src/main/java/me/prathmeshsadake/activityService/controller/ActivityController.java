package me.prathmeshsadake.activityService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.activityService.dto.ActivityRequestDto;
import me.prathmeshsadake.activityService.dto.ActivityResponseDto;
import me.prathmeshsadake.activityService.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponseDto> trackActivity(@RequestBody ActivityRequestDto activityRequestDto) {
        log.info("Tracking activity {}", activityRequestDto);
        return ResponseEntity.ok(activityService.trackActivity(activityRequestDto));
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponseDto>> getUserActivities(@RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(activityService.getUserActivities(userId));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponseDto> getActivity(@PathVariable String activityId) {
        return ResponseEntity.ok(activityService.getActivityById(activityId));
    }
}
