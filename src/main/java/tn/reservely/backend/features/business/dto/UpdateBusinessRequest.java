package tn.reservely.backend.features.business.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateBusinessRequest(
        @Size(max = 200) String name,
        @Size(max = 300) String address,
        @Size(max = 100) String city,
        Double lat,
        Double lng,
        String genderTarget,
        @Size(max = 20) String phone,
        List<String> tags,
        @Size(max = 100) String instagramHandle,
        @Size(max = 100) String facebookHandle,
        @Size(max = 100) String tiktokHandle,
        @Size(max = 30) String whatsappNumber
) {}
