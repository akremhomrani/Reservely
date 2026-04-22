package tn.reservely.backend.features.business.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateWorkingHoursRequest(@NotNull List<HourEntry> hours) {

    public record HourEntry(
            String dayOfWeek,
            String openTime,
            String closeTime,
            boolean closed
    ) {}
}
