package com.foodbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = @Index(name = "idx_notification_user_read", columnList = "user_id,is_read"))
@Getter @Setter @NoArgsConstructor
public class NotificationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(nullable = false, length = 140)
    private String title;
    @Column(nullable = false, length = 700)
    private String message;
    @Column(length = 40)
    private String type;
    @Column(nullable = false)
    private boolean isRead = false;
    private LocalDateTime createdAt;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
}
