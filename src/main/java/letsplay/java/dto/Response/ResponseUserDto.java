package letsplay.java.dto.Response;

public record ResponseUserDto(
                String id,
                String username,
                String email,
                String role) {
}