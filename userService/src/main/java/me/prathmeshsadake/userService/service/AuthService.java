package me.prathmeshsadake.userService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.userService.dto.LoginRequestDto;
import me.prathmeshsadake.userService.dto.RegisterRequestDto;
import me.prathmeshsadake.userService.dto.UserDto;
import me.prathmeshsadake.userService.model.User;
import me.prathmeshsadake.userService.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public UserDto createNewUser(RegisterRequestDto registerRequestDto) {

        if(userRepository.existsByEmail(registerRequestDto.getEmail())){
            throw new RuntimeException("Email already exists");
        }

        User registeringUser = new User();
        registeringUser.setEmail(registerRequestDto.getEmail());
        registeringUser.setFirstName(registerRequestDto.getFirstName());
        registeringUser.setLastName(registerRequestDto.getLastName());
        registeringUser.setPassword(registerRequestDto.getPassword());
        User user = userRepository.save(registeringUser);
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        return userDto;
    }

    public String loginUser(LoginRequestDto loginRequestDto) {
        return UUID.randomUUID().toString();
    }
}
