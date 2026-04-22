package tn.reservely.backend.features.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateBusinessRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 300) String address,
        @NotBlank @Size(max = 100) String city,
        double lat,
        double lng,
        String category,
        String genderTarget,
        @Size(max = 20) String phone,
        List<String> tags,
        @Size(max = 100) String instagramHandle,
        @Size(max = 100) String facebookHandle,
        @Size(max = 100) String tiktokHandle,
        @Size(max = 30) String whatsappNumber
) {}
