package tn.reservely.backend.features.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reservely.backend.features.business.domain.Staff;

import java.util.List;
import java.util.UUID;

public interface StaffRepository extends JpaRepository<Staff, UUID> {
    List<Staff> findByBusinessIdAndActiveTrue(UUID businessId);
    List<Staff> findByBusinessIdInAndActiveTrue(List<UUID> businessIds);
}
