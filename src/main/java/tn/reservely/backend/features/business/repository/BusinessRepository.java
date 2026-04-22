package tn.reservely.backend.features.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reservely.backend.features.business.domain.Business;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
    List<Business> findByStatusOrderByRatingAvgDesc(String status);
    List<Business> findByCityIgnoreCaseAndStatusOrderByRatingAvgDesc(String city, String status);
    Optional<Business> findByOwnerId(UUID ownerId);
}
