package tn.reservely.backend.features.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateStaffRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 30) String phone,
        List<String> specialties
) {}
