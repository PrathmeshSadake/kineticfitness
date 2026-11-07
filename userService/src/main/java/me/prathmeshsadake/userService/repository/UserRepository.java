package me.prathmeshsadake.userService.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import me.prathmeshsadake.userService.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,String> {

    boolean existsByEmail(@NotBlank(message = "Email is required.") @Email(message = "Must be a valid email") String email);
}
