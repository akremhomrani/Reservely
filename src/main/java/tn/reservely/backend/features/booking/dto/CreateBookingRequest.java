package tn.reservely.backend.features.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID businessId,
        @NotNull UUID serviceId,
        UUID staffId,
        @NotBlank String startAt,
        String notes
) {}
