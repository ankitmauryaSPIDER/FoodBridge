package com.foodbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations", indexes = {
    @Index(name = "idx_donation_status_deadline", columnList = "status,pickup_deadline"),
    @Index(name = "idx_donation_city", columnList = "city")
})
@Getter @Setter @NoArgsConstructor
public class DonationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    private UserEntity donor;
    @Column(nullable = false, length = 120)
    private String foodName;
    @Column(length = 1000)
    private String description;
    @Column(length = 80)
    private String category;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;
    @Column(nullable = false, length = 20)
    private String unit;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private Domain.FoodType foodType;
    private LocalDateTime preparationTime;
    private LocalDateTime useByTime;
    @Column(nullable = false)
    private LocalDateTime pickupDeadline;
    @Column(length = 250)
    private String storageConditions;
    @Column(length = 250)
    private String allergenInformation;
    @Column(length = 250)
    private String packagingInformation;
    @Column(length = 700)
    private String imageUrl;
    @Column(nullable = false, length = 400)
    private String address;
    @Column(nullable = false, length = 80)
    private String city;
    @Column(length = 80)
    private String state;
    @Column(length = 12)
    private String pincode;
    private Double latitude;
    private Double longitude;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Domain.DonationStatus status = Domain.DonationStatus.AVAILABLE;
    @Column(nullable = false)
    private boolean safetyConfirmed = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }
    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }
}
