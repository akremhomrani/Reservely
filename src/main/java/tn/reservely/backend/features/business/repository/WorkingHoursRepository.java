package tn.reservely.backend.features.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reservely.backend.features.business.domain.WorkingHoursEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkingHoursRepository extends JpaRepository<WorkingHoursEntry, UUID> {
    List<WorkingHoursEntry> findByBusinessId(UUID businessId);
    List<WorkingHoursEntry> findByBusinessIdIn(List<UUID> businessIds);
    Optional<WorkingHoursEntry> findByBusinessIdAndDayOfWeek(UUID businessId, String dayOfWeek);
}
