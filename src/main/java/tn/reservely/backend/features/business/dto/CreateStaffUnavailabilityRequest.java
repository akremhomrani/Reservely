package tn.reservely.backend.features.business.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateStaffUnavailabilityRequest(
        @NotNull LocalDate from,
        @NotNull LocalDate to
) {}
