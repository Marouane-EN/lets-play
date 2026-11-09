package letsplay.java.dto.Response;

public record UserDto(
                String id,
                String username,
                String email,
                String role) {
}