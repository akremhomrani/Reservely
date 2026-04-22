package tn.reservely.backend.features.business.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.reservely.backend.features.booking.domain.Booking;
import tn.reservely.backend.features.booking.dto.BookingResponse;
import tn.reservely.backend.features.booking.repository.BookingRepository;
import tn.reservely.backend.features.business.domain.*;
import tn.reservely.backend.features.business.dto.*;
import tn.reservely.backend.features.business.repository.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final BusinessRepository     businessRepo;
    private final ServiceItemRepository  serviceRepo;
    private final StaffRepository        staffRepo;
    private final WorkingHoursRepository hoursRepo;
    private final BookingRepository      bookingRepo;
    private final BusinessService        businessService;

    // ── business ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Optional<BusinessResponse> getMyBusiness(UUID ownerId) {
        return businessRepo.findByOwnerId(ownerId)
                .map(b -> businessService.getById(b.getId()));
    }

    @Transactional
    public BusinessResponse createBusiness(UUID ownerId, CreateBusinessRequest req) {
        if (businessRepo.findByOwnerId(ownerId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have a business");
        }
        Business b = new Business();
        b.setOwnerId(ownerId);
        b.setName(req.name());
        b.setAddress(req.address());
        b.setCity(req.city());
        b.setLat(req.lat() != 0 ? req.lat() : 36.8190);
        b.setLng(req.lng() != 0 ? req.lng() : 10.1658);
        b.setCategory(req.category() != null ? req.category() : "BARBER");
        b.setGenderTarget(req.genderTarget() != null ? req.genderTarget() : "MEN");
        b.setStatus("ACTIVE");
        b.setPhone(req.phone());
        if (req.tags() != null) b.getTags().addAll(req.tags());
        businessRepo.save(b);
        return businessService.getById(b.getId());
    }

    @Transactional
    public BusinessResponse updateBusiness(UUID ownerId, UpdateBusinessRequest req) {
        Business b = requireOwnedBusiness(ownerId);
        if (req.name()         != null) b.setName(req.name());
        if (req.address()      != null) b.setAddress(req.address());
        if (req.city()         != null) b.setCity(req.city());
        if (req.lat()          != null) b.setLat(req.lat());
        if (req.lng()          != null) b.setLng(req.lng());
        if (req.genderTarget() != null) b.setGenderTarget(req.genderTarget());
        if (req.phone()        != null) b.setPhone(req.phone());
        if (req.tags()         != null) { b.getTags().clear(); b.getTags().addAll(req.tags()); }
        businessRepo.save(b);
        return businessService.getById(b.getId());
    }

    // ── working hours ─────────────────────────────────────────────────────────

    @Transactional
    public List<WorkingHoursDto> updateWorkingHours(UUID ownerId, UpdateWorkingHoursRequest req) {
        Business b = requireOwnedBusiness(ownerId);
        for (UpdateWorkingHoursRequest.HourEntry entry : req.hours()) {
            WorkingHoursEntry wh = hoursRepo
                    .findByBusinessIdAndDayOfWeek(b.getId(), entry.dayOfWeek())
                    .orElseGet(() -> {
                        WorkingHoursEntry e = new WorkingHoursEntry();
                        e.setBusinessId(b.getId());
                        e.setDayOfWeek(entry.dayOfWeek());
                        return e;
                    });
            wh.setClosed(entry.closed());
            wh.setOpenTime(entry.closed() ? null : entry.openTime());
            wh.setCloseTime(entry.closed() ? null : entry.closeTime());
            hoursRepo.save(wh);
        }
        return hoursRepo.findByBusinessId(b.getId()).stream()
                .map(h -> new WorkingHoursDto(
                        h.isClosed() ? "" : h.getOpenTime(),
                        h.isClosed() ? "" : h.getCloseTime(),
                        h.isClosed()))
                .toList();
    }

    // ── services ──────────────────────────────────────────────────────────────

    @Transactional
    public ServiceItemDto addService(UUID ownerId, CreateServiceRequest req) {
        Business b = requireOwnedBusiness(ownerId);
        ServiceItem s = new ServiceItem();
        s.setBusinessId(b.getId());
        s.setName(req.name());
        s.setDurationMinutes(req.durationMinutes());
        s.setPrice(req.price());
        s.setDescription(req.description());
        serviceRepo.save(s);
        return toServiceDto(s);
    }

    @Transactional
    public ServiceItemDto updateService(UUID ownerId, UUID serviceId, UpdateServiceRequest req) {
        Business b = requireOwnedBusiness(ownerId);
        ServiceItem s = serviceRepo.findById(serviceId)
                .filter(si -> si.getBusinessId().equals(b.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
        if (req.name()            != null) s.setName(req.name());
        if (req.durationMinutes() != null) s.setDurationMinutes(req.durationMinutes());
        if (req.price()           != null) s.setPrice(req.price());
        if (req.description()     != null) s.setDescription(req.description());
        serviceRepo.save(s);
        return toServiceDto(s);
    }

    @Transactional
    public void deleteService(UUID ownerId, UUID serviceId) {
        Business b = requireOwnedBusiness(ownerId);
        ServiceItem s = serviceRepo.findById(serviceId)
                .filter(si -> si.getBusinessId().equals(b.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
        s.setActive(false);
        serviceRepo.save(s);
    }

    // ── staff ─────────────────────────────────────────────────────────────────

    @Transactional
    public StaffDto addStaff(UUID ownerId, CreateStaffRequest req) {
        Business b = requireOwnedBusiness(ownerId);
        Staff st = new Staff();
        st.setBusinessId(b.getId());
        st.setName(req.name());
        st.setRating(5.0);
        if (req.specialties() != null) st.getSpecialties().addAll(req.specialties());
        staffRepo.save(st);
        return toStaffDto(st);
    }

    @Transactional
    public StaffDto updateStaff(UUID ownerId, UUID staffId, UpdateStaffRequest req) {
        Business b = requireOwnedBusiness(ownerId);
        Staff st = staffRepo.findById(staffId)
                .filter(s -> s.getBusinessId().equals(b.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff not found"));
        if (req.name()        != null) st.setName(req.name());
        if (req.specialties() != null) { st.getSpecialties().clear(); st.getSpecialties().addAll(req.specialties()); }
        staffRepo.save(st);
        return toStaffDto(st);
    }

    @Transactional
    public void deleteStaff(UUID ownerId, UUID staffId) {
        Business b = requireOwnedBusiness(ownerId);
        Staff st = staffRepo.findById(staffId)
                .filter(s -> s.getBusinessId().equals(b.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff not found"));
        st.setActive(false);
        staffRepo.save(st);
    }

    // ── bookings ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(UUID ownerId) {
        Business b = requireOwnedBusiness(ownerId);
        return bookingRepo.findByBusinessIdOrderByStartAtDesc(b.getId()).stream()
                .map(this::toBookingResponse)
                .toList();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Business requireOwnedBusiness(UUID ownerId) {
        return businessRepo.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No business found for this owner"));
    }

    private ServiceItemDto toServiceDto(ServiceItem s) {
        return new ServiceItemDto(s.getId().toString(), s.getName(), s.getDurationMinutes(), s.getPrice(), s.getDescription());
    }

    private StaffDto toStaffDto(Staff s) {
        return new StaffDto(s.getId().toString(), s.getName(), s.getAvatarUrl(), s.getRating(), new ArrayList<>(s.getSpecialties()));
    }

    private BookingResponse toBookingResponse(Booking b) {
        return new BookingResponse(
                b.getId().toString(),
                b.getReferenceCode(),
                b.getBusinessId().toString(),
                "",
                b.getServiceId().toString(),
                "",
                b.getStaffId() != null ? b.getStaffId().toString() : null,
                null,
                b.getStartAt().toString(),
                b.getEndAt().toString(),
                b.getStatus(),
                b.getNotes(),
                b.getCreatedAt().toString()
        );
    }
}
