package tn.reservely.backend.features.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.reservely.backend.features.booking.domain.Booking;
import tn.reservely.backend.features.booking.dto.BookingResponse;
import tn.reservely.backend.features.booking.dto.CreateBookingRequest;
import tn.reservely.backend.features.booking.repository.BookingRepository;
import tn.reservely.backend.features.business.domain.Business;
import tn.reservely.backend.features.business.domain.ServiceItem;
import tn.reservely.backend.features.business.domain.Staff;
import tn.reservely.backend.features.business.repository.BusinessRepository;
import tn.reservely.backend.features.business.repository.ServiceItemRepository;
import tn.reservely.backend.features.business.repository.StaffRepository;
import tn.reservely.backend.shared.exception.NotFoundException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository     bookingRepo;
    private final BusinessRepository    businessRepo;
    private final ServiceItemRepository serviceRepo;
    private final StaffRepository       staffRepo;

    @Transactional
    public BookingResponse create(UUID customerId, CreateBookingRequest req) {
        Business b = businessRepo.findById(req.businessId())
                .orElseThrow(() -> new NotFoundException("Business not found"));
        ServiceItem svc = serviceRepo.findById(req.serviceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        Instant startAt = Instant.parse(req.startAt());
        Instant endAt   = startAt.plusSeconds((long) svc.getDurationMinutes() * 60);

        String ref = generateUniqueReference();

        String staffName = null;
        if (req.staffId() != null) {
            staffName = staffRepo.findById(req.staffId()).map(Staff::getName).orElse(null);
        }

        Booking booking = new Booking();
        booking.setReferenceCode(ref);
        booking.setCustomerId(customerId);
        booking.setBusinessId(req.businessId());
        booking.setServiceId(req.serviceId());
        booking.setStaffId(req.staffId());
        booking.setStartAt(startAt);
        booking.setEndAt(endAt);
        booking.setStatus("CONFIRMED");
        booking.setNotes(req.notes());
        bookingRepo.save(booking);

        return toResponse(booking, b.getName(), svc.getName(), staffName);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(UUID customerId) {
        List<Booking> bookings = bookingRepo.findByCustomerIdOrderByStartAtDesc(customerId);
        if (bookings.isEmpty()) return List.of();

        Set<UUID> bIds = bookings.stream().map(Booking::getBusinessId).collect(Collectors.toSet());
        Set<UUID> sIds = bookings.stream().map(Booking::getServiceId).collect(Collectors.toSet());
        Set<UUID> stIds = bookings.stream().filter(bk -> bk.getStaffId() != null).map(Booking::getStaffId).collect(Collectors.toSet());

        Map<UUID, String> bNames  = businessRepo.findAllById(bIds).stream().collect(Collectors.toMap(Business::getId, Business::getName));
        Map<UUID, String> sNames  = serviceRepo.findAllById(sIds).stream().collect(Collectors.toMap(ServiceItem::getId, ServiceItem::getName));
        Map<UUID, String> stNames = staffRepo.findAllById(stIds).stream().collect(Collectors.toMap(Staff::getId, Staff::getName));

        return bookings.stream()
                .map(bk -> toResponse(bk,
                        bNames.getOrDefault(bk.getBusinessId(), "Unknown"),
                        sNames.getOrDefault(bk.getServiceId(), "Unknown"),
                        bk.getStaffId() != null ? stNames.get(bk.getStaffId()) : null))
                .toList();
    }

    @Transactional
    public void cancel(UUID bookingId, UUID customerId) {
        Booking bk = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        if (!bk.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
        }
        bk.setStatus("CANCELLED");
        bookingRepo.save(bk);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private BookingResponse toResponse(Booking bk, String bName, String sName, String stName) {
        return new BookingResponse(
                bk.getId().toString(),
                bk.getReferenceCode(),
                bk.getBusinessId().toString(),
                bName,
                bk.getServiceId().toString(),
                sName,
                bk.getStaffId() != null ? bk.getStaffId().toString() : null,
                stName,
                bk.getStartAt().toString(),
                bk.getEndAt().toString(),
                bk.getStatus(),
                bk.getNotes(),
                bk.getCreatedAt().toString()
        );
    }

    private String generateUniqueReference() {
        String ref;
        int year = LocalDate.now().getYear();
        do {
            long n = ThreadLocalRandom.current().nextLong(10000, 99999);
            ref = "RES-" + year + "-" + n;
        } while (bookingRepo.existsByReferenceCode(ref));
        return ref;
    }
}
