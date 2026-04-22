package tn.reservely.backend.features.business.dto;

import java.util.List;
import java.util.Map;

public record BusinessResponse(
        String id,
        String name,
        String address,
        String city,
        double lat,
        double lng,
        String genderTarget,
        String phone,
        String imageUrl,
        double ratingAvg,
        int reviewCount,
        List<String> tags,
        boolean isOpen,
        String openingHours,
        List<ServiceItemDto> services,
        List<StaffDto> staff,
        Map<String, WorkingHoursDto> workingHours,
        String instagramHandle,
        String facebookHandle,
        String tiktokHandle,
        String whatsappNumber
) {}
