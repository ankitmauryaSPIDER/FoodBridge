package com.foodbridge.service;

import com.foodbridge.dto.ApiDtos.RequestCreateRequest;
import com.foodbridge.dto.ApiDtos.RequestDto;
import com.foodbridge.entity.*;
import com.foodbridge.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {
    private final DonationRequestRepository requests;
    private final DonationRepository donations;
    private final DeliveryRepository deliveries;
    private final CurrentUserService currentUser;
    private final NotificationService notifications;
    private final SecureRandom random = new SecureRandom();
    public RequestService(DonationRequestRepository requests, DonationRepository donations, DeliveryRepository deliveries,
                          CurrentUserService currentUser, NotificationService notifications) {
        this.requests = requests; this.donations = donations; this.deliveries = deliveries; this.currentUser = currentUser; this.notifications = notifications;
    }
    @Transactional
    public RequestDto create(String email, RequestCreateRequest req) {
        UserEntity ngo = currentUser.require(email); requireRole(ngo, Domain.Role.NGO);
        if (!ngo.isVerified()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your NGO account is awaiting admin verification");
        if (req.donationId() == null || req.requestedQuantity() == null || req.requestedQuantity().compareTo(BigDecimal.ZERO) <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donation and a valid requested quantity are required");
        DonationEntity donation = donations.findById(req.donationId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donation not found"));
        if (donation.getDonor().getId().equals(ngo.getId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot request your own donation");
        if (donation.getStatus() != Domain.DonationStatus.AVAILABLE || donation.getPickupDeadline().isBefore(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This donation is no longer available");
        if (req.requestedQuantity().compareTo(donation.getQuantity()) > 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested quantity cannot exceed available quantity");
        if (requests.findByDonation_IdAndNgo_Id(donation.getId(), ngo.getId()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Your organization has already requested this donation");
        DonationRequestEntity r = new DonationRequestEntity();
        r.setDonation(donation); r.setNgo(ngo); r.setRequestedQuantity(req.requestedQuantity()); r.setMessage(req.message());
        requests.save(r);
        notifications.notify(donation.getDonor(), "New food request", ngo.getName() + " requested " + donation.getFoodName() + ".", "REQUEST");
        return toDto(r);
    }
    @Transactional(readOnly = true)
    public List<RequestDto> mine(String email) {
        UserEntity user = currentUser.require(email);
        if (user.getRole() == Domain.Role.NGO) return requests.findAllByNgo_IdOrderByCreatedAtDesc(user.getId()).stream().map(this::toDto).toList();
        if (user.getRole() == Domain.Role.DONOR) return requests.findAllByDonation_Donor_IdOrderByCreatedAtDesc(user.getId()).stream().map(this::toDto).toList();
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Requests are available to donors and NGOs");
    }
    @Transactional
    public RequestDto accept(String email, Long id) {
        UserEntity donor = currentUser.require(email); requireRole(donor, Domain.Role.DONOR);
        DonationRequestEntity r = requests.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        DonationEntity donation = r.getDonation();
        if (!donation.getDonor().getId().equals(donor.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage requests for your own donations");
        if (r.getRequestStatus() != Domain.RequestStatus.PENDING || donation.getStatus() != Domain.DonationStatus.AVAILABLE)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This request can no longer be accepted");
        r.setRequestStatus(Domain.RequestStatus.ACCEPTED); donation.setStatus(Domain.DonationStatus.RESERVED);
        requests.findAllByDonation_IdAndRequestStatus(donation.getId(), Domain.RequestStatus.PENDING).forEach(other -> {
            other.setRequestStatus(Domain.RequestStatus.REJECTED);
            notifications.notify(other.getNgo(), "Donation unavailable", "Another NGO was selected for " + donation.getFoodName() + ".", "REQUEST");
        });
        DeliveryEntity delivery = new DeliveryEntity(); delivery.setRequest(r); delivery.setStatus(Domain.DeliveryStatus.READY_FOR_PICKUP);
        delivery.setVerificationCode(String.format("%06d", random.nextInt(1_000_000))); deliveries.save(delivery);
        notifications.notify(r.getNgo(), "Request accepted", "Your request for " + donation.getFoodName() + " was accepted. A volunteer can now claim delivery.", "REQUEST");
        return toDto(r);
    }
    @Transactional
    public RequestDto reject(String email, Long id) {
        UserEntity donor = currentUser.require(email); requireRole(donor, Domain.Role.DONOR);
        DonationRequestEntity r = requests.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        if (!r.getDonation().getDonor().getId().equals(donor.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage requests for your own donations");
        if (r.getRequestStatus() != Domain.RequestStatus.PENDING) throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending requests can be rejected");
        r.setRequestStatus(Domain.RequestStatus.REJECTED); requests.save(r);
        notifications.notify(r.getNgo(), "Request declined", "Your request for " + r.getDonation().getFoodName() + " was declined.", "REQUEST");
        return toDto(r);
    }
    @Transactional
    public void cancel(String email, Long id) {
        UserEntity ngo = currentUser.require(email); requireRole(ngo, Domain.Role.NGO);
        DonationRequestEntity r = requests.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        if (!r.getNgo().getId().equals(ngo.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request");
        if (r.getRequestStatus() != Domain.RequestStatus.PENDING) throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending requests can be cancelled");
        r.setRequestStatus(Domain.RequestStatus.CANCELLED); requests.save(r);
    }
    private void requireRole(UserEntity user, Domain.Role role) {
        if (user.getRole() != role) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This action is available only to " + role + " accounts");
    }
    private RequestDto toDto(DonationRequestEntity r) {
        DonationEntity d = r.getDonation();
        return new RequestDto(r.getId(), d.getId(), d.getFoodName(), String.valueOf(r.getNgo().getId()), r.getNgo().getOrganizationName() == null ? r.getNgo().getName() : r.getNgo().getOrganizationName(),
            String.valueOf(d.getDonor().getId()), d.getDonor().getName(), r.getRequestedQuantity(), r.getMessage(), r.getRequestStatus().name(), r.getCreatedAt());
    }
}
