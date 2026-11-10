package letsplay.java.dto.Response;

public record ProductDto(
                String id,
                String name,
                String description,
                double price,
                String userId) {
}