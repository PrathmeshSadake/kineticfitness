package me.prathmeshsadake.userService.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.prathmeshsadake.userService.dto.LoginRequestDto;
import me.prathmeshsadake.userService.dto.RegisterRequestDto;
import me.prathmeshsadake.userService.dto.UserDto;
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
    public ResponseEntity<UserDto> signup(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        UserDto userDto = authService.createNewUser(registerRequestDto);
        return new ResponseEntity<UserDto>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequestDto loginDto){
        return authService.loginUser(loginDto);
    }

}
