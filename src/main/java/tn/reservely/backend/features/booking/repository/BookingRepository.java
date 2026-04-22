package tn.reservely.backend.features.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.reservely.backend.features.booking.domain.Booking;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByCustomerIdOrderByStartAtDesc(UUID customerId);

    @Query("SELECT b FROM Booking b WHERE b.businessId = :businessId " +
           "AND b.startAt >= :start AND b.startAt < :end " +
           "AND b.status <> :status")
    List<Booking> findByBusinessIdInRange(
            @Param("businessId") UUID businessId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("status") String status);

    @Query("SELECT b FROM Booking b WHERE b.staffId = :staffId " +
           "AND b.startAt >= :start AND b.startAt < :end " +
           "AND b.status <> :status")
    List<Booking> findByStaffIdInRange(
            @Param("staffId") UUID staffId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("status") String status);

    boolean existsByReferenceCode(String referenceCode);

    List<Booking> findByBusinessIdOrderByStartAtDesc(UUID businessId);
}
