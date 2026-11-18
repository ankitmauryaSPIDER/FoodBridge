package com.foodbridge.config;

import com.foodbridge.entity.Domain;
import com.foodbridge.entity.UserEntity;
import com.foodbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("prod")
public class ProductionAdminInitializer {
    @Bean
    CommandLineRunner createProductionAdmin(
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${ADMIN_EMAIL}") String adminEmail,
            @Value("${ADMIN_PASSWORD}") String adminPassword) {
        if (adminPassword.length() < 16) {
            throw new IllegalStateException("ADMIN_PASSWORD must be at least 16 characters long");
        }

        return args -> users.findByEmail(adminEmail).ifPresentOrElse(existing -> {
            if (existing.getRole() != Domain.Role.ADMIN) {
                throw new IllegalStateException("ADMIN_EMAIL is already assigned to a non-admin account");
            }
        }, () -> {
            UserEntity admin = new UserEntity();
            admin.setName("FoodBridge Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(encoder.encode(adminPassword));
            admin.setRole(Domain.Role.ADMIN);
            admin.setVerified(true);
            admin.setActive(true);
            users.save(admin);
        });
    }
}
