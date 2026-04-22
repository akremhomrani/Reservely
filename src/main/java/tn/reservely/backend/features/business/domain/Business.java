package tn.reservely.backend.features.business.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "businesses")
@Getter @Setter @NoArgsConstructor
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(nullable = false)
    private String city;

    private double lat;
    private double lng;

    @Column(nullable = false)
    private String category;

    @Column(name = "gender_target", nullable = false)
    private String genderTarget;

    @Column(nullable = false)
    private String status;

    private String phone;

    @Column(name = "instagram_handle")
    private String instagramHandle;

    @Column(name = "facebook_handle")
    private String facebookHandle;

    @Column(name = "tiktok_handle")
    private String tiktokHandle;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "rating_avg")
    private double ratingAvg;

    @Column(name = "review_count")
    private int reviewCount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "business_tags", joinColumns = @JoinColumn(name = "business_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
