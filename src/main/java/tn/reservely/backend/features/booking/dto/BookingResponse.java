package tn.reservely.backend.features.booking.dto;

public record BookingResponse(
        String id,
        String referenceCode,
        String businessId,
        String businessName,
        String serviceId,
        String serviceName,
        String staffId,
        String staffName,
        String startAt,
        String endAt,
        String status,
        String notes,
        String createdAt
) {}
