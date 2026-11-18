package com.foodbridge.config;

import com.foodbridge.entity.*;
import com.foodbridge.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@Profile({"local", "demo"})
public class DemoDataInitializer {
    @Bean
    CommandLineRunner seedDemoData(UserRepository users, DonationRepository donations, PasswordEncoder encoder,
                                   @Value("${ADMIN_EMAIL:admin@foodbridge.local}") String adminEmail,
                                   @Value("${ADMIN_PASSWORD:Admin123!}") String adminPassword) {
        return args -> {
            if (users.count() > 0) return;
            UserEntity admin = user("FoodBridge Admin", adminEmail, adminPassword, Domain.Role.ADMIN, true, encoder);
            UserEntity donor = user("Ankit Restaurant", "donor@foodbridge.local", "Donor123!", Domain.Role.DONOR, true, encoder);
            donor.setOrganizationName("Ankit Community Kitchen"); donor.setPhone("9000000001"); donor.setAddress("Sector 62, Noida"); donor.setCity("Noida"); donor.setState("Uttar Pradesh"); donor.setPincode("201309");
            UserEntity ngo = user("Helping Hands NGO", "ngo@foodbridge.local", "Ngo123!", Domain.Role.NGO, true, encoder);
            ngo.setOrganizationName("Helping Hands Foundation"); ngo.setPhone("9000000002"); ngo.setAddress("Indirapuram, Ghaziabad"); ngo.setCity("Ghaziabad"); ngo.setState("Uttar Pradesh"); ngo.setPincode("201014");
            UserEntity pendingNgo = user("Hope Community Trust", "pending@foodbridge.local", "Ngo123!", Domain.Role.NGO, false, encoder);
            pendingNgo.setOrganizationName("Hope Community Trust"); pendingNgo.setCity("Delhi");
            UserEntity volunteer = user("Rahul Volunteer", "volunteer@foodbridge.local", "Volunteer123!", Domain.Role.VOLUNTEER, true, encoder);
            volunteer.setPhone("9000000003"); volunteer.setCity("Noida");
            users.save(admin); users.save(donor); users.save(ngo); users.save(pendingNgo); users.save(volunteer);

            saveDonation(donations, donor, "Fresh vegetable pulao", "Freshly prepared vegetarian rice meal in sealed containers.", "Prepared meals", "18", "packets", Domain.FoodType.VEGETARIAN, "Keep covered; collect promptly", "May contain traces of nuts", "Sealed food-grade containers", "Community Kitchen, Sector 62", "Noida", "Uttar Pradesh", "201309");
            saveDonation(donations, donor, "Seasonal fruit boxes", "Clean, sorted seasonal fruit boxes.", "Fruits", "12", "boxes", Domain.FoodType.VEGAN, "Store in a cool place", "None declared", "Clean reusable crates", "Community Kitchen, Sector 62", "Noida", "Uttar Pradesh", "201309");
            saveDonation(donations, donor, "Fresh chapati and dal", "A surplus batch of chapati and dal prepared today.", "Prepared meals", "25", "meals", Domain.FoodType.VEGETARIAN, "Keep temperature controlled", "May contain wheat", "Food-grade covered containers", "Community Kitchen, Sector 62", "Noida", "Uttar Pradesh", "201309");
        };
    }
    private UserEntity user(String name, String email, String password, Domain.Role role, boolean verified, PasswordEncoder encoder) {
        UserEntity u = new UserEntity(); u.setName(name); u.setEmail(email); u.setPassword(encoder.encode(password));
        u.setRole(role); u.setVerified(verified); u.setActive(true); return u;
    }
    private void saveDonation(DonationRepository repo, UserEntity donor, String name, String desc, String category, String quantity,
                              String unit, Domain.FoodType type, String storage, String allergens, String packaging,
                              String address, String city, String state, String pincode) {
        LocalDateTime now = LocalDateTime.now(); DonationEntity d = new DonationEntity();
        d.setDonor(donor); d.setFoodName(name); d.setDescription(desc); d.setCategory(category); d.setQuantity(new BigDecimal(quantity));
        d.setUnit(unit); d.setFoodType(type); d.setPreparationTime(now.minusMinutes(35)); d.setPickupDeadline(now.plusHours(8)); d.setUseByTime(now.plusHours(12));
        d.setStorageConditions(storage); d.setAllergenInformation(allergens); d.setPackagingInformation(packaging); d.setAddress(address);
        d.setCity(city); d.setState(state); d.setPincode(pincode); d.setStatus(Domain.DonationStatus.AVAILABLE); d.setSafetyConfirmed(true); repo.save(d);
    }
}
