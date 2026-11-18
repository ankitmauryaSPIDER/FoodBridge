package com.foodbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, unique = true, length = 180)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(length = 30)
    private String phone;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Domain.Role role;
    @Column(nullable = false)
    private boolean verified = false;
    @Column(nullable = false)
    private boolean active = true;
    @Column(length = 180)
    private String organizationName;
    @Column(length = 500)
    private String address;
    @Column(length = 80)
    private String city;
    @Column(length = 80)
    private String state;
    @Column(length = 12)
    private String pincode;
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}
