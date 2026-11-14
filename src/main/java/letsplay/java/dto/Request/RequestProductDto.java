package letsplay.java.dto.Request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RequestProductDto(
                @NotBlank(message = "Title is required") @Size(min = 3, max = 50, message = "Title must be between 3 and 50 characters") String title,
                @NotBlank(message = "Description is required") @Size(min = 3, max = 255, message = "Description must be between 3 and 255 characters") String description,
                @NotNull(message = "Price is required") @DecimalMin(value = "0.0", message = "Price cannot be negative") @DecimalMax(value = "1000000000.0", message = "Price cannot be over 1000000000") Double price) {
}