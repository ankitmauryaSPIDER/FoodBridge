package com.foodbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation_requests", uniqueConstraints = @UniqueConstraint(name = "uk_donation_ngo", columnNames = {"donation_id", "ngo_id"}))
@Getter @Setter @NoArgsConstructor
public class DonationRequestEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donation_id", nullable = false)
    private DonationEntity donation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ngo_id", nullable = false)
    private UserEntity ngo;
    @Column(length = 500)
    private String message;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal requestedQuantity;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Domain.RequestStatus requestStatus = Domain.RequestStatus.PENDING;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
