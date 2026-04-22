package tn.reservely.backend.features.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reservely.backend.features.booking.domain.Booking;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByCustomerIdOrderByStartAtDesc(UUID customerId);
    List<Booking> findByBusinessIdAndStartAtBetweenAndStatusNot(UUID businessId, Instant start, Instant end, String status);
    List<Booking> findByStaffIdAndStartAtBetweenAndStatusNot(UUID staffId, Instant start, Instant end, String status);
    boolean existsByReferenceCode(String referenceCode);
    List<Booking> findByBusinessIdOrderByStartAtDesc(UUID businessId);
}
