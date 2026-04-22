package tn.reservely.backend.features.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateServiceRequest(
        @NotBlank @Size(max = 200) String name,
        @Positive int durationMinutes,
        @Positive double price,
        @Size(max = 500) String description
) {}
