package com.example.alumni.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AlumniSearchRequest(
        @NotBlank(message = "university is required") String university,
        @NotBlank(message = "designation is required") String designation,
        @Min(value = 1950, message = "passoutYear must be >= 1950")
        @Max(value = 2100, message = "passoutYear must be <= 2100") Integer passoutYear) {
}
