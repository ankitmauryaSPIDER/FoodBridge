package com.foodbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter @Setter @NoArgsConstructor
public class DeliveryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private DonationRequestEntity request;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id")
    private UserEntity volunteer;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private Domain.DeliveryStatus status = Domain.DeliveryStatus.READY_FOR_PICKUP;
    @Column(nullable = false, length = 8)
    private String verificationCode;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveredTime;
    private LocalDateTime createdAt;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
}
