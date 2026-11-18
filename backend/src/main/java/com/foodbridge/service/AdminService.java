package com.foodbridge.service;

import com.foodbridge.dto.ApiDtos.AdminOverviewDto;
import com.foodbridge.dto.ApiDtos.UserDto;
import com.foodbridge.entity.Domain;
import com.foodbridge.entity.UserEntity;
import com.foodbridge.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class AdminService {
    private final UserRepository users;
    private final DonationRepository donations;
    private final DonationRequestRepository requests;
    private final DeliveryRepository deliveries;
    public AdminService(UserRepository users, DonationRepository donations, DonationRequestRepository requests, DeliveryRepository deliveries) {
        this.users = users; this.donations = donations; this.requests = requests; this.deliveries = deliveries;
    }
    public AdminOverviewDto overview() {
        return new AdminOverviewDto(users.count(), users.countByRole(Domain.Role.DONOR), users.countByRole(Domain.Role.NGO),
            users.findAllByRoleOrderByCreatedAtDesc(Domain.Role.NGO).stream().filter(UserEntity::isVerified).count(),
            users.countByRole(Domain.Role.VOLUNTEER), donations.count(), donations.countByStatus(Domain.DonationStatus.AVAILABLE),
            donations.countByStatus(Domain.DonationStatus.RESERVED), donations.countByStatus(Domain.DonationStatus.DELIVERED),
            requests.countByRequestStatus(Domain.RequestStatus.PENDING), deliveries.countByStatus(Domain.DeliveryStatus.COMPLETED));
    }
    public List<UserDto> allUsers() { return users.findAll().stream().map(com.foodbridge.service.AuthService::toDto).toList(); }
    public List<UserDto> pendingNgos() { return users.findAllByRoleAndVerifiedFalseOrderByCreatedAtAsc(Domain.Role.NGO).stream().filter(UserEntity::isActive).map(com.foodbridge.service.AuthService::toDto).toList(); }
    @Transactional
    public UserDto approveNgo(Long id) {
        UserEntity user = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));
        if (user.getRole() != Domain.Role.NGO) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected account is not an NGO");
        user.setVerified(true); return com.foodbridge.service.AuthService.toDto(users.save(user));
    }
    @Transactional
    public UserDto rejectNgo(Long id) {
        UserEntity user = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));
        if (user.getRole() != Domain.Role.NGO) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected account is not an NGO");
        user.setActive(false); return com.foodbridge.service.AuthService.toDto(users.save(user));
    }
    @Transactional
    public UserDto setActive(Long id, boolean active) {
        UserEntity user = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() == Domain.Role.ADMIN) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin account cannot be changed here");
        user.setActive(active); return com.foodbridge.service.AuthService.toDto(users.save(user));
    }
}
