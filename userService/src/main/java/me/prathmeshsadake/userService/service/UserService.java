package me.prathmeshsadake.userService.service;

import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.userService.dto.UserDto;
import me.prathmeshsadake.userService.model.User;
import me.prathmeshsadake.userService.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDto getUserById(String userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        return userDto;
    }

}
