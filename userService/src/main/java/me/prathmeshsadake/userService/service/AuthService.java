package me.prathmeshsadake.userService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.userService.dto.AuthResponseDto;
import me.prathmeshsadake.userService.dto.LoginRequestDto;
import me.prathmeshsadake.userService.dto.RegisterRequestDto;
import me.prathmeshsadake.userService.dto.UserDto;
import me.prathmeshsadake.userService.model.User;
import me.prathmeshsadake.userService.repository.UserRepository;
import me.prathmeshsadake.userService.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponseDto createNewUser(RegisterRequestDto registerRequestDto) {
        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User registeringUser = new User();
        registeringUser.setEmail(registerRequestDto.getEmail().trim().toLowerCase());
        registeringUser.setFirstName(registerRequestDto.getFirstName());
        registeringUser.setLastName(registerRequestDto.getLastName());
        registeringUser.setPassword(passwordEncoder.encode(registerRequestDto.getPassword()));
        User user = userRepository.save(registeringUser);
        return buildAuthResponse(user);
    }

    public AuthResponseDto loginUser(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return buildAuthResponse(user);
    }

    private AuthResponseDto buildAuthResponse(User user) {
        String role = user.getRole() != null ? user.getRole().name() : "USER";
        String token = jwtService.generateToken(user.getId(), user.getEmail(), role);
        return AuthResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .user(toUserDto(user))
                .build();
    }

    private UserDto toUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        return userDto;
    }
}
