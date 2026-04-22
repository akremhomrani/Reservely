package tn.reservely.backend.features.business.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateServiceRequest(
        @Size(max = 200) String name,
        @Positive Integer durationMinutes,
        @Positive Double price,
        @Size(max = 500) String description
) {}
