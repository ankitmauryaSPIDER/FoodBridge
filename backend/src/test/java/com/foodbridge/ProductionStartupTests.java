package com.foodbridge;

import com.foodbridge.entity.Domain;
import com.foodbridge.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:foodbridge-prod-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "JWT_SECRET=test-production-jwt-secret-with-at-least-32-characters",
        "FRONTEND_URL=https://foodbridge-test.vercel.app",
        "ADMIN_EMAIL=admin@foodbridge.test",
        "ADMIN_PASSWORD=test-production-admin-password"
})
@ActiveProfiles("prod")
class ProductionStartupTests {
    @Autowired
    private UserRepository users;

    @Test
    void appliesDatabaseMigrationAndCreatesConfiguredAdmin() {
        var admin = users.findByEmail("admin@foodbridge.test").orElseThrow();
        assertEquals(Domain.Role.ADMIN, admin.getRole());
        assertNotEquals("test-production-admin-password", admin.getPassword());
    }
}
