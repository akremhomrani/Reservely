package tn.reservely.backend.features.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reservely.backend.features.business.domain.ServiceItem;

import java.util.List;
import java.util.UUID;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, UUID> {
    List<ServiceItem> findByBusinessIdAndActiveTrue(UUID businessId);
    List<ServiceItem> findByBusinessIdInAndActiveTrue(List<UUID> businessIds);
}
