package me.prathmeshsadake.userService.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.prathmeshsadake.userService.dto.AuthResponseDto;
import me.prathmeshsadake.userService.dto.LoginRequestDto;
import me.prathmeshsadake.userService.dto.RegisterRequestDto;
import me.prathmeshsadake.userService.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDto> signup(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        return new ResponseEntity<>(authService.createNewUser(registerRequestDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginDto) {
        return ResponseEntity.ok(authService.loginUser(loginDto));
    }
}
