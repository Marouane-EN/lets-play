package letsplay.java.dto.Response;

public record ResponseProductDto(
                String id,
                String name,
                String description,
                double price,
                String userId) {
}