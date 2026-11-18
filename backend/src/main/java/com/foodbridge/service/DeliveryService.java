package com.foodbridge.service;

import com.foodbridge.dto.ApiDtos.DeliveryDto;
import com.foodbridge.entity.*;
import com.foodbridge.repository.DeliveryRepository;
import com.foodbridge.repository.DonationRepository;
import com.foodbridge.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryService {
    private final DeliveryRepository deliveries;
    private final DonationRepository donations;
    private final UserRepository users;
    private final CurrentUserService currentUser;
    private final NotificationService notifications;
    public DeliveryService(DeliveryRepository deliveries, DonationRepository donations, UserRepository users,
                           CurrentUserService currentUser, NotificationService notifications) {
        this.deliveries = deliveries; this.donations = donations; this.users = users; this.currentUser = currentUser; this.notifications = notifications;
    }
    @Transactional(readOnly = true)
    public List<DeliveryDto> available(String email) {
        UserEntity user = currentUser.require(email); requireRole(user, Domain.Role.VOLUNTEER);
        return deliveries.findAllByVolunteerIsNullAndStatusOrderByCreatedAtDesc(Domain.DeliveryStatus.READY_FOR_PICKUP).stream().map(d -> toDto(d, false, false)).toList();
    }
    @Transactional(readOnly = true)
    public List<DeliveryDto> mine(String email) {
        UserEntity user = currentUser.require(email);
        List<DeliveryEntity> all = deliveries.findAll();
        return all.stream().filter(d -> switch (user.getRole()) {
            case ADMIN -> true;
            case VOLUNTEER -> d.getVolunteer() != null && d.getVolunteer().getId().equals(user.getId());
            case DONOR -> d.getRequest().getDonation().getDonor().getId().equals(user.getId());
            case NGO -> d.getRequest().getNgo().getId().equals(user.getId());
        }).map(d -> toDto(d, user.getRole() != Domain.Role.VOLUNTEER, true)).toList();
    }
    @Transactional
    public DeliveryDto claim(String email, Long id) {
        UserEntity volunteer = currentUser.require(email); requireRole(volunteer, Domain.Role.VOLUNTEER);
        DeliveryEntity d = deliveries.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery task not found"));
        if (d.getVolunteer() != null || d.getStatus() != Domain.DeliveryStatus.READY_FOR_PICKUP)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This delivery task has already been claimed");
        d.setVolunteer(volunteer); deliveries.save(d);
        notifications.notify(d.getRequest().getDonation().getDonor(), "Volunteer assigned", volunteer.getName() + " will collect " + d.getRequest().getDonation().getFoodName() + ".", "DELIVERY");
        notifications.notify(d.getRequest().getNgo(), "Volunteer assigned", volunteer.getName() + " has been assigned to your delivery.", "DELIVERY");
        return toDto(d, false, true);
    }
    @Transactional
    public DeliveryDto pickup(String email, Long id) {
        UserEntity volunteer = currentUser.require(email); requireRole(volunteer, Domain.Role.VOLUNTEER);
        DeliveryEntity d = getAssigned(id, volunteer);
        if (d.getStatus() != Domain.DeliveryStatus.READY_FOR_PICKUP) throw new ResponseStatusException(HttpStatus.CONFLICT, "This delivery is not ready for pickup");
        d.setStatus(Domain.DeliveryStatus.PICKED_UP); d.setPickupTime(LocalDateTime.now());
        d.getRequest().getDonation().setStatus(Domain.DonationStatus.PICKED_UP);
        notifyParticipants(d, "Food picked up", volunteer.getName() + " has picked up the donation.");
        return toDto(deliveries.save(d), false, true);
    }
    @Transactional
    public DeliveryDto complete(String email, Long id, String code) {
        UserEntity volunteer = currentUser.require(email); requireRole(volunteer, Domain.Role.VOLUNTEER);
        DeliveryEntity d = getAssigned(id, volunteer);
        if (d.getStatus() != Domain.DeliveryStatus.PICKED_UP) throw new ResponseStatusException(HttpStatus.CONFLICT, "Pick up the food before completing delivery");
        if (code == null || !d.getVerificationCode().equals(code.trim())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The delivery verification code does not match");
        d.setStatus(Domain.DeliveryStatus.COMPLETED); d.setDeliveredTime(LocalDateTime.now());
        d.getRequest().getDonation().setStatus(Domain.DonationStatus.DELIVERED);
        notifyParticipants(d, "Delivery completed", "The food donation has been marked as delivered. Thank you for helping!");
        return toDto(deliveries.save(d), false, true);
    }
    private DeliveryEntity getAssigned(Long id, UserEntity volunteer) {
        DeliveryEntity d = deliveries.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery task not found"));
        if (d.getVolunteer() == null || !d.getVolunteer().getId().equals(volunteer.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This delivery is not assigned to you");
        return d;
    }
    private void notifyParticipants(DeliveryEntity d, String title, String message) {
        notifications.notify(d.getRequest().getDonation().getDonor(), title, message, "DELIVERY");
        notifications.notify(d.getRequest().getNgo(), title, message, "DELIVERY");
    }
    private void requireRole(UserEntity user, Domain.Role role) {
        if (user.getRole() != role) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This action is available only to " + role + " accounts");
    }
    private DeliveryDto toDto(DeliveryEntity d, boolean revealCode, boolean revealContactInfo) {
        DonationEntity donation = d.getRequest().getDonation();
        UserEntity donor = donation.getDonor(); UserEntity ngo = d.getRequest().getNgo(); UserEntity volunteer = d.getVolunteer();
        return new DeliveryDto(d.getId(), d.getRequest().getId(), donation.getId(), donation.getFoodName(), donor.getName(), revealContactInfo ? donor.getPhone() : null,
            revealContactInfo ? donation.getAddress() : donation.getCity() + (donation.getState() == null ? "" : ", " + donation.getState()), donation.getCity(), ngo.getOrganizationName() == null ? ngo.getName() : ngo.getOrganizationName(), revealContactInfo ? ngo.getPhone() : null, revealContactInfo ? ngo.getAddress() : null,
            volunteer == null ? null : volunteer.getName(), revealContactInfo && volunteer != null ? volunteer.getPhone() : null, d.getStatus().name(), revealCode ? d.getVerificationCode() : null, d.getPickupTime(), d.getDeliveredTime());
    }
}
