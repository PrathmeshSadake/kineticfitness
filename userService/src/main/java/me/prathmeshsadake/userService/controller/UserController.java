package me.prathmeshsadake.userService.controller;

import lombok.RequiredArgsConstructor;
import me.prathmeshsadake.userService.dto.UserDto;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class UserController {

//    @GetMapping("/{userId}")
//    public RequestEntity<UserDto> getUser(@PathVariable String userId) {
//        return
//    }
}
