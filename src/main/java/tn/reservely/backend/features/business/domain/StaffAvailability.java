package tn.reservely.backend.features.business.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "staff_availability")
@Getter @Setter @NoArgsConstructor
public class StaffAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "staff_id", nullable = false)
    private UUID staffId;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "unavailable_from", nullable = false)
    private LocalDate unavailableFrom;

    @Column(name = "unavailable_to", nullable = false)
    private LocalDate unavailableTo;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
