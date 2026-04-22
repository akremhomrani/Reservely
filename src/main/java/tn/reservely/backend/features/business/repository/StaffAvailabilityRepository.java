package tn.reservely.backend.features.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.reservely.backend.features.business.domain.StaffAvailability;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface StaffAvailabilityRepository extends JpaRepository<StaffAvailability, UUID> {

    List<StaffAvailability> findByStaffId(UUID staffId);

    List<StaffAvailability> findByBusinessId(UUID businessId);

    @Query("SELECT COUNT(a) > 0 FROM StaffAvailability a " +
           "WHERE a.staffId = :staffId " +
           "AND a.unavailableFrom <= :date AND a.unavailableTo >= :date")
    boolean isStaffUnavailableOn(@Param("staffId") UUID staffId, @Param("date") LocalDate date);
}
