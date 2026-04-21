package tn.reservely.backend.features.business.dto;

import java.util.List;

public record StaffDto(
        String id,
        String name,
        String avatarUrl,
        double rating,
        List<String> specialties
) {}
