package tn.reservely.backend.features.business.dto;

public record StaffUnavailabilityDto(
        String id,
        String staffId,
        String staffName,
        String from,
        String to
) {}
