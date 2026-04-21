package tn.reservely.backend.features.business.dto;

public record ServiceItemDto(
        String id,
        String name,
        int durationMinutes,
        double price,
        String description
) {}
