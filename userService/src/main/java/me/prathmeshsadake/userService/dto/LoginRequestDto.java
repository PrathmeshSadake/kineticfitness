package me.prathmeshsadake.userService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank(message = "Email is required.")
    @Email(message = "Must be a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(message = "Password must have atleast 8 characters", min = 8)
    private String password;
}
