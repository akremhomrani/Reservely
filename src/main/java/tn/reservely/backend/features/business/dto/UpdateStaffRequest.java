package tn.reservely.backend.features.business.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateStaffRequest(
        @Size(max = 100) String name,
        List<String> specialties
) {}
