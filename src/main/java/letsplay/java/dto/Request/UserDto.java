package letsplay.java.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserDto(
                @NotBlank(message = "Username is required") @Size(min = 3, max = 20, message = "Name must be between 3 and 20 characters") String username,
                @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
                @NotBlank(message = "Role is required") @Pattern(regexp = "USER|ADMIN", message = "Role must be either USER or ADMIN") String role) {
}