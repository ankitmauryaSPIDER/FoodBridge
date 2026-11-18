package com.foodbridge.service;

import com.foodbridge.dto.ApiDtos.DonationCreateRequest;
import com.foodbridge.dto.ApiDtos.DonationDto;
import com.foodbridge.entity.DonationEntity;
import com.foodbridge.entity.Domain;
import com.foodbridge.entity.UserEntity;
import com.foodbridge.repository.DonationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class DonationService {
    private final DonationRepository donations;
    private final CurrentUserService currentUser;
    public DonationService(DonationRepository donations, CurrentUserService currentUser) {
        this.donations = donations; this.currentUser = currentUser;
    }
    @Transactional
    public DonationDto create(String email, DonationCreateRequest req) {
        UserEntity donor = currentUser.require(email);
        requireRole(donor, Domain.Role.DONOR);
        validate(req);
        DonationEntity d = new DonationEntity();
        d.setDonor(donor); apply(d, req); d.setStatus(Domain.DonationStatus.AVAILABLE); d.setSafetyConfirmed(Boolean.TRUE.equals(req.safetyConfirmed()));
        return toDto(donations.save(d));
    }
    @Transactional
    public List<DonationDto> available() {
        expireOld();
        return donations.findAllByStatusAndPickupDeadlineAfterOrderByCreatedAtDesc(Domain.DonationStatus.AVAILABLE, LocalDateTime.now()).stream().map(this::toListingDto).toList();
    }
    @Transactional(readOnly = true)
    public List<DonationDto> mine(String email) {
        UserEntity user = currentUser.require(email); requireRole(user, Domain.Role.DONOR);
        return donations.findAllByDonor_IdOrderByCreatedAtDesc(user.getId()).stream().map(this::toDto).toList();
    }
    @Transactional(readOnly = true)
    public DonationDto getOne(String email, Long id) {
        UserEntity viewer = currentUser.require(email);
        DonationEntity d = donations.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donation not found"));
        if (d.getStatus() == Domain.DonationStatus.AVAILABLE && d.getPickupDeadline().isBefore(LocalDateTime.now())) throw new ResponseStatusException(HttpStatus.GONE, "This donation has passed its pickup deadline");
        return d.getDonor().getId().equals(viewer.getId()) || viewer.getRole() == Domain.Role.ADMIN ? toDto(d) : toListingDto(d);
    }
    @Transactional
    public DonationDto update(String email, Long id, DonationCreateRequest req) {
        UserEntity user = currentUser.require(email); requireRole(user, Domain.Role.DONOR);
        DonationEntity d = donations.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donation not found"));
        if (!d.getDonor().getId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own donations");
        if (d.getStatus() != Domain.DonationStatus.AVAILABLE) throw new ResponseStatusException(HttpStatus.CONFLICT, "Only available donations can be edited");
        validate(req); apply(d, req); d.setSafetyConfirmed(Boolean.TRUE.equals(req.safetyConfirmed()));
        return toDto(donations.save(d));
    }
    @Transactional
    public void cancel(String email, Long id) {
        UserEntity user = currentUser.require(email); requireRole(user, Domain.Role.DONOR);
        DonationEntity d = donations.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donation not found"));
        if (!d.getDonor().getId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own donations");
        if (d.getStatus() != Domain.DonationStatus.AVAILABLE) throw new ResponseStatusException(HttpStatus.CONFLICT, "Only available donations can be cancelled");
        d.setStatus(Domain.DonationStatus.CANCELLED); donations.save(d);
    }
    @Transactional(readOnly = true)
    public List<DonationDto> allForAdmin() { return donations.findAll().stream().map(this::toDto).toList(); }

    private DonationDto toListingDto(DonationEntity d) {
        DonationDto full = toDto(d);
        String approximateAddress = d.getCity() + (d.getState() == null || d.getState().isBlank() ? "" : ", " + d.getState());
        return new DonationDto(full.id(), full.donorId(), full.donorName(), full.donorOrganization(), full.foodName(), full.description(), full.category(),
            full.quantity(), full.unit(), full.foodType(), full.preparationTime(), full.useByTime(), full.pickupDeadline(), full.storageConditions(),
            full.allergenInformation(), full.packagingInformation(), full.imageUrl(), approximateAddress, full.city(), full.state(), null,
            null, null, full.status(), full.createdAt());
    }
    private void expireOld() {
        List<DonationEntity> expired = donations.findAllByStatusAndPickupDeadlineBefore(Domain.DonationStatus.AVAILABLE, LocalDateTime.now());
        expired.forEach(d -> d.setStatus(Domain.DonationStatus.EXPIRED));
        if (!expired.isEmpty()) donations.saveAll(expired);
    }
    private void validate(DonationCreateRequest r) {
        if (r.foodName() == null || r.foodName().isBlank() || r.quantity() == null || r.quantity().compareTo(BigDecimal.ZERO) <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Food name and a quantity greater than zero are required");
        if (r.unit() == null || r.unit().isBlank() || r.address() == null || r.address().isBlank() || r.city() == null || r.city().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit, pickup address and city are required");
        if (r.pickupDeadline() == null || r.pickupDeadline().isBefore(LocalDateTime.now().plusMinutes(5)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pickup deadline must be at least 5 minutes in the future");
        if (r.useByTime() == null || r.useByTime().isBefore(r.pickupDeadline()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use-by time must be after the pickup deadline");
        if (!Boolean.TRUE.equals(r.safetyConfirmed())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please confirm that the food is safe and suitable for donation");
        try { Domain.FoodType.valueOf((r.foodType() == null ? "OTHER" : r.foodType()).toUpperCase(Locale.ROOT)); }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid food type"); }
    }
    private void apply(DonationEntity d, DonationCreateRequest r) {
        d.setFoodName(r.foodName().trim()); d.setDescription(r.description()); d.setCategory(r.category()); d.setQuantity(r.quantity());
        d.setUnit(r.unit()); d.setFoodType(Domain.FoodType.valueOf((r.foodType() == null ? "OTHER" : r.foodType()).toUpperCase(Locale.ROOT)));
        d.setPreparationTime(r.preparationTime()); d.setUseByTime(r.useByTime()); d.setPickupDeadline(r.pickupDeadline());
        d.setStorageConditions(r.storageConditions()); d.setAllergenInformation(r.allergenInformation()); d.setPackagingInformation(r.packagingInformation());
        d.setImageUrl(r.imageUrl()); d.setAddress(r.address()); d.setCity(r.city()); d.setState(r.state()); d.setPincode(r.pincode());
        d.setLatitude(r.latitude()); d.setLongitude(r.longitude());
    }
    private void requireRole(UserEntity user, Domain.Role role) {
        if (user.getRole() != role) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This action is available only to " + role + " accounts");
    }
    public DonationDto toDto(DonationEntity d) {
        UserEntity donor = d.getDonor();
        return new DonationDto(d.getId(), donor.getId(), donor.getName(), donor.getOrganizationName(), d.getFoodName(), d.getDescription(), d.getCategory(),
            d.getQuantity(), d.getUnit(), d.getFoodType().name(), d.getPreparationTime(), d.getUseByTime(), d.getPickupDeadline(), d.getStorageConditions(),
            d.getAllergenInformation(), d.getPackagingInformation(), d.getImageUrl(), d.getAddress(), d.getCity(), d.getState(), d.getPincode(),
            d.getLatitude(), d.getLongitude(), d.getStatus().name(), d.getCreatedAt());
    }
}
