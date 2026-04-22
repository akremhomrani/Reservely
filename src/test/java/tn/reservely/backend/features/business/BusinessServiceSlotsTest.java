package tn.reservely.backend.features.business;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.reservely.backend.features.booking.dto.SlotDto;
import tn.reservely.backend.features.booking.repository.BookingRepository;
import tn.reservely.backend.features.business.domain.ServiceItem;
import tn.reservely.backend.features.business.domain.WorkingHoursEntry;
import tn.reservely.backend.features.business.repository.*;
import tn.reservely.backend.features.business.service.BusinessService;
import tn.reservely.backend.shared.exception.NotFoundException;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessServiceSlotsTest {

    @Mock BusinessRepository          businessRepo;
    @Mock ServiceItemRepository       serviceRepo;
    @Mock StaffRepository             staffRepo;
    @Mock WorkingHoursRepository      hoursRepo;
    @Mock BookingRepository           bookingRepo;
    @Mock StaffAvailabilityRepository staffAvailabilityRepo;

    @InjectMocks BusinessService businessService;

    private final UUID businessId = UUID.randomUUID();
    private final UUID serviceId  = UUID.randomUUID();

    private ServiceItem service30min;

    @BeforeEach
    void setUp() {
        service30min = new ServiceItem();
        service30min.setDurationMinutes(30);
    }

    @Test
    void getSlots_shouldReturnSlots_whenDayIsOpen() {
        when(serviceRepo.findById(serviceId)).thenReturn(Optional.of(service30min));

        WorkingHoursEntry wh = new WorkingHoursEntry();
        wh.setBusinessId(businessId);
        wh.setDayOfWeek("FRIDAY");
        wh.setOpenTime("09:00");
        wh.setCloseTime("11:00");
        wh.setClosed(false);

        when(hoursRepo.findByBusinessIdAndDayOfWeek(eq(businessId), eq("FRIDAY")))
                .thenReturn(Optional.of(wh));
        when(bookingRepo.findByBusinessIdInRange(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<SlotDto> slots = businessService.getSlots(businessId, "2026-04-24", serviceId, null);

        // 09:00→09:30, 09:30→10:00, 10:00→10:30, 10:30→11:00 → 4 slots
        assertThat(slots).hasSize(4);
        assertThat(slots).allMatch(SlotDto::available);
    }

    @Test
    void getSlots_shouldReturnEmpty_whenDayIsClosed() {
        when(serviceRepo.findById(serviceId)).thenReturn(Optional.of(service30min));

        WorkingHoursEntry wh = new WorkingHoursEntry();
        wh.setBusinessId(businessId);
        wh.setDayOfWeek("SUNDAY");
        wh.setClosed(true);

        when(hoursRepo.findByBusinessIdAndDayOfWeek(eq(businessId), eq("SUNDAY")))
                .thenReturn(Optional.of(wh));

        List<SlotDto> slots = businessService.getSlots(businessId, "2026-04-26", serviceId, null);

        assertThat(slots).isEmpty();
        verifyNoInteractions(bookingRepo);
    }

    @Test
    void getSlots_shouldReturnEmpty_whenNoWorkingHoursEntry() {
        when(serviceRepo.findById(serviceId)).thenReturn(Optional.of(service30min));
        when(hoursRepo.findByBusinessIdAndDayOfWeek(eq(businessId), any()))
                .thenReturn(Optional.empty());

        List<SlotDto> slots = businessService.getSlots(businessId, "2026-04-24", serviceId, null);

        assertThat(slots).isEmpty();
        verifyNoInteractions(bookingRepo);
    }

    @Test
    void getSlots_shouldReturnEmpty_whenOpenTimeIsNull() {
        when(serviceRepo.findById(serviceId)).thenReturn(Optional.of(service30min));

        WorkingHoursEntry wh = new WorkingHoursEntry();
        wh.setBusinessId(businessId);
        wh.setDayOfWeek("FRIDAY");
        wh.setOpenTime(null);
        wh.setCloseTime(null);
        wh.setClosed(false);

        when(hoursRepo.findByBusinessIdAndDayOfWeek(eq(businessId), eq("FRIDAY")))
                .thenReturn(Optional.of(wh));

        List<SlotDto> slots = businessService.getSlots(businessId, "2026-04-24", serviceId, null);

        assertThat(slots).isEmpty();
        verifyNoInteractions(bookingRepo);
    }

    @Test
    void getSlots_shouldReturnEmpty_whenCloseTimeIsNull_notThrow500() {
        // This is the bug fix: closeTime=null with openTime set should not throw NPE
        when(serviceRepo.findById(serviceId)).thenReturn(Optional.of(service30min));

        WorkingHoursEntry wh = new WorkingHoursEntry();
        wh.setBusinessId(businessId);
        wh.setDayOfWeek("FRIDAY");
        wh.setOpenTime("09:00");
        wh.setCloseTime(null); // missing closeTime — was causing 500
        wh.setClosed(false);

        when(hoursRepo.findByBusinessIdAndDayOfWeek(eq(businessId), eq("FRIDAY")))
                .thenReturn(Optional.of(wh));

        assertThatCode(() -> businessService.getSlots(businessId, "2026-04-24", serviceId, null))
                .doesNotThrowAnyException();

        List<SlotDto> slots = businessService.getSlots(businessId, "2026-04-24", serviceId, null);
        assertThat(slots).isEmpty();
        verifyNoInteractions(bookingRepo);
    }

    @Test
    void getSlots_shouldReturnEmpty_forPastDate() {
        // A date in the past — all slots have already passed, so none should be returned
        when(serviceRepo.findById(serviceId)).thenReturn(Optional.of(service30min));

        WorkingHoursEntry wh = new WorkingHoursEntry();
        wh.setBusinessId(businessId);
        wh.setDayOfWeek("WEDNESDAY");
        wh.setOpenTime("09:00");
        wh.setCloseTime("21:00");
        wh.setClosed(false);

        when(hoursRepo.findByBusinessIdAndDayOfWeek(eq(businessId), eq("WEDNESDAY")))
                .thenReturn(Optional.of(wh));
        when(bookingRepo.findByBusinessIdInRange(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<SlotDto> slots = businessService.getSlots(businessId, "2020-01-01", serviceId, null);

        assertThat(slots).isEmpty();
    }

    @Test
    void getSlots_shouldThrowNotFoundException_whenServiceNotFound() {
        when(serviceRepo.findById(serviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessService.getSlots(businessId, "2026-04-24", serviceId, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Service not found");
    }
}
