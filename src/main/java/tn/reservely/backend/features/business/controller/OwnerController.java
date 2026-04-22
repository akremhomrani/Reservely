package tn.reservely.backend.features.business.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.reservely.backend.features.booking.dto.BookingResponse;
import tn.reservely.backend.features.business.dto.*;
import tn.reservely.backend.features.business.service.OwnerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    // ── business ─────────────────────────────────────────────────────────────

    @GetMapping("/business")
    public ResponseEntity<BusinessResponse> getMyBusiness(Authentication auth) {
        return ownerService.getMyBusiness(ownerId(auth))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/business")
    public ResponseEntity<BusinessResponse> createBusiness(
            Authentication auth,
            @Valid @RequestBody CreateBusinessRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerService.createBusiness(ownerId(auth), req));
    }

    @PutMapping("/business")
    public ResponseEntity<BusinessResponse> updateBusiness(
            Authentication auth,
            @Valid @RequestBody UpdateBusinessRequest req) {
        return ResponseEntity.ok(ownerService.updateBusiness(ownerId(auth), req));
    }

    // ── working hours ─────────────────────────────────────────────────────────

    @PutMapping("/business/hours")
    public ResponseEntity<List<WorkingHoursDto>> updateHours(
            Authentication auth,
            @Valid @RequestBody UpdateWorkingHoursRequest req) {
        return ResponseEntity.ok(ownerService.updateWorkingHours(ownerId(auth), req));
    }

    // ── services ──────────────────────────────────────────────────────────────

    @PostMapping("/business/services")
    public ResponseEntity<ServiceItemDto> addService(
            Authentication auth,
            @Valid @RequestBody CreateServiceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerService.addService(ownerId(auth), req));
    }

    @PutMapping("/business/services/{serviceId}")
    public ResponseEntity<ServiceItemDto> updateService(
            Authentication auth,
            @PathVariable UUID serviceId,
            @Valid @RequestBody UpdateServiceRequest req) {
        return ResponseEntity.ok(ownerService.updateService(ownerId(auth), serviceId, req));
    }

    @DeleteMapping("/business/services/{serviceId}")
    public ResponseEntity<Void> deleteService(
            Authentication auth,
            @PathVariable UUID serviceId) {
        ownerService.deleteService(ownerId(auth), serviceId);
        return ResponseEntity.noContent().build();
    }

    // ── staff ─────────────────────────────────────────────────────────────────

    @PostMapping("/business/staff")
    public ResponseEntity<StaffDto> addStaff(
            Authentication auth,
            @Valid @RequestBody CreateStaffRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerService.addStaff(ownerId(auth), req));
    }

    @PutMapping("/business/staff/{staffId}")
    public ResponseEntity<StaffDto> updateStaff(
            Authentication auth,
            @PathVariable UUID staffId,
            @Valid @RequestBody UpdateStaffRequest req) {
        return ResponseEntity.ok(ownerService.updateStaff(ownerId(auth), staffId, req));
    }

    @DeleteMapping("/business/staff/{staffId}")
    public ResponseEntity<Void> deleteStaff(
            Authentication auth,
            @PathVariable UUID staffId) {
        ownerService.deleteStaff(ownerId(auth), staffId);
        return ResponseEntity.noContent().build();
    }

    // ── bookings ─────────────────────────────────────────────────────────────

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication auth) {
        return ResponseEntity.ok(ownerService.getMyBookings(ownerId(auth)));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private UUID ownerId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
