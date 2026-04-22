package tn.reservely.backend.features.business.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.reservely.backend.features.booking.domain.Booking;
import tn.reservely.backend.features.booking.repository.BookingRepository;
import tn.reservely.backend.features.booking.dto.SlotDto;
import tn.reservely.backend.features.business.domain.*;
import tn.reservely.backend.features.business.dto.*;
import tn.reservely.backend.features.business.repository.*;
import tn.reservely.backend.shared.exception.NotFoundException;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private static final ZoneId TUNIS = ZoneId.of("Africa/Tunis");

    private static final Map<String, String> DAY_ABBREV = Map.of(
            "MONDAY", "Mon", "TUESDAY", "Tue", "WEDNESDAY", "Wed",
            "THURSDAY", "Thu", "FRIDAY", "Fri", "SATURDAY", "Sat", "SUNDAY", "Sun"
    );

    private final BusinessRepository          businessRepo;
    private final ServiceItemRepository       serviceRepo;
    private final StaffRepository             staffRepo;
    private final WorkingHoursRepository      hoursRepo;
    private final BookingRepository           bookingRepo;
    private final StaffAvailabilityRepository staffAvailabilityRepo;

    @Transactional(readOnly = true)
    public List<BusinessResponse> listAll(String city) {
        List<Business> businesses = city != null && !city.isBlank()
                ? businessRepo.findByCityIgnoreCaseAndStatusOrderByRatingAvgDesc(city, "ACTIVE")
                : businessRepo.findByStatusOrderByRatingAvgDesc("ACTIVE");

        if (businesses.isEmpty()) return List.of();

        List<UUID> ids = businesses.stream().map(Business::getId).toList();

        Map<UUID, List<ServiceItem>>       svcMap   = serviceRepo.findByBusinessIdInAndActiveTrue(ids).stream().collect(groupingBy(ServiceItem::getBusinessId));
        Map<UUID, List<Staff>>             staffMap = staffRepo.findByBusinessIdInAndActiveTrue(ids).stream().collect(groupingBy(Staff::getBusinessId));
        Map<UUID, List<WorkingHoursEntry>> hoursMap = hoursRepo.findByBusinessIdIn(ids).stream().collect(groupingBy(WorkingHoursEntry::getBusinessId));

        return businesses.stream()
                .map(b -> toResponse(b,
                        svcMap.getOrDefault(b.getId(), List.of()),
                        staffMap.getOrDefault(b.getId(), List.of()),
                        hoursMap.getOrDefault(b.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public BusinessResponse getById(UUID id) {
        Business b = businessRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Business not found: " + id));
        return toResponse(b,
                serviceRepo.findByBusinessIdAndActiveTrue(id),
                staffRepo.findByBusinessIdAndActiveTrue(id),
                hoursRepo.findByBusinessId(id));
    }

    @Transactional(readOnly = true)
    public List<SlotDto> getSlots(UUID businessId, String date, UUID serviceId, UUID staffId) {
        LocalDate localDate = LocalDate.parse(date);

        ServiceItem service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));
        int durationMins = service.getDurationMinutes();

        if (staffId != null && staffAvailabilityRepo.isStaffUnavailableOn(staffId, localDate)) {
            return List.of();
        }

        String dow = localDate.getDayOfWeek().name();
        WorkingHoursEntry wh = hoursRepo.findByBusinessIdAndDayOfWeek(businessId, dow).orElse(null);
        if (wh == null || wh.isClosed() || wh.getOpenTime() == null || wh.getCloseTime() == null) return List.of();

        LocalTime openTime  = LocalTime.parse(wh.getOpenTime());
        LocalTime closeTime = LocalTime.parse(wh.getCloseTime());

        Instant dayStart = localDate.atStartOfDay(TUNIS).toInstant();
        Instant dayEnd   = localDate.plusDays(1).atStartOfDay(TUNIS).toInstant();

        List<Booking> existing = staffId != null
                ? bookingRepo.findByStaffIdInRange(staffId, dayStart, dayEnd, "CANCELLED")
                : bookingRepo.findByBusinessIdInRange(businessId, dayStart, dayEnd, "CANCELLED");

        ZonedDateTime nowTunis = ZonedDateTime.now(TUNIS);
        List<SlotDto> slots = new ArrayList<>();
        LocalTime current = openTime;

        while (!current.plusMinutes(durationMins).isAfter(closeTime)) {
            ZonedDateTime slotStart = localDate.atTime(current).atZone(TUNIS);

            // Skip slots that have already started
            if (!slotStart.isAfter(nowTunis)) {
                current = current.plusMinutes(30);
                continue;
            }

            ZonedDateTime slotEnd = slotStart.plusMinutes(durationMins);
            Instant s = slotStart.toInstant();
            Instant e = slotEnd.toInstant();

            boolean available = existing.stream()
                    .noneMatch(b -> b.getStartAt().isBefore(e) && b.getEndAt().isAfter(s));

            slots.add(new SlotDto(s.toString(), available));
            current = current.plusMinutes(30);
        }
        return slots;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private BusinessResponse toResponse(Business b, List<ServiceItem> services,
                                         List<Staff> staff, List<WorkingHoursEntry> hours) {
        return new BusinessResponse(
                b.getId().toString(),
                b.getName(),
                b.getAddress(),
                b.getCity(),
                b.getLat(),
                b.getLng(),
                b.getGenderTarget(),
                b.getPhone(),
                b.getImageUrl(),
                b.getRatingAvg(),
                b.getReviewCount(),
                new ArrayList<>(b.getTags()),
                computeIsOpen(hours),
                computeOpeningHours(hours),
                services.stream().map(s -> new ServiceItemDto(s.getId().toString(), s.getName(), s.getDurationMinutes(), s.getPrice(), s.getDescription())).toList(),
                staff.stream().map(s -> new StaffDto(s.getId().toString(), s.getName(), s.getAvatarUrl(), s.getRating(), new ArrayList<>(s.getSpecialties()), s.getPhone())).toList(),
                toWorkingHoursMap(hours),
                b.getInstagramHandle(),
                b.getFacebookHandle(),
                b.getTiktokHandle(),
                b.getWhatsappNumber()
        );
    }

    private boolean computeIsOpen(List<WorkingHoursEntry> hours) {
        ZonedDateTime now = ZonedDateTime.now(TUNIS);
        String today = now.getDayOfWeek().name();
        return hours.stream()
                .filter(h -> h.getDayOfWeek().equals(today) && !h.isClosed()
                             && h.getOpenTime() != null && h.getCloseTime() != null)
                .anyMatch(h -> {
                    LocalTime open  = LocalTime.parse(h.getOpenTime());
                    LocalTime close = LocalTime.parse(h.getCloseTime());
                    LocalTime ct    = now.toLocalTime();
                    return !ct.isBefore(open) && ct.isBefore(close);
                });
    }

    private String computeOpeningHours(List<WorkingHoursEntry> hours) {
        String today = ZonedDateTime.now(TUNIS).getDayOfWeek().name();
        return hours.stream()
                .filter(h -> h.getDayOfWeek().equals(today))
                .findFirst()
                .map(h -> h.isClosed() ? "Closed today" : h.getOpenTime() + " – " + h.getCloseTime())
                .orElse("–");
    }

    private Map<String, WorkingHoursDto> toWorkingHoursMap(List<WorkingHoursEntry> hours) {
        Map<String, WorkingHoursDto> map = new LinkedHashMap<>();
        for (WorkingHoursEntry h : hours) {
            String key = DAY_ABBREV.getOrDefault(h.getDayOfWeek(), h.getDayOfWeek());
            map.put(key, new WorkingHoursDto(
                    h.isClosed() ? "" : h.getOpenTime(),
                    h.isClosed() ? "" : h.getCloseTime(),
                    h.isClosed()
            ));
        }
        return map;
    }
}
