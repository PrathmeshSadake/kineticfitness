package me.prathmeshsadake.activityService.controller;

import lombok.RequiredArgsConstructor;
import me.prathmeshsadake.activityService.dto.ActivityRequestDto;
import me.prathmeshsadake.activityService.dto.ActivityResponseDto;
import me.prathmeshsadake.activityService.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponseDto> trackActivity(@RequestBody ActivityRequestDto activityRequestDto) {
        return ResponseEntity.ok(activityService.trackActivity(activityRequestDto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ActivityResponseDto>> getUserActivities(@PathVariable String userId) {
        return ResponseEntity.ok(activityService.getUserActivities(userId));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponseDto> getActivity(@PathVariable String activityId) {
        return ResponseEntity.ok(activityService.getActivityById(activityId));
    }
}
