package com.foodbridge.controller;

import com.foodbridge.dto.ApiDtos.*;
import com.foodbridge.service.AdminService;
import com.foodbridge.service.DonationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService admin;
    private final DonationService donations;
    public AdminController(AdminService admin, DonationService donations) { this.admin = admin; this.donations = donations; }
    @GetMapping("/overview") public AdminOverviewDto overview() { return admin.overview(); }
    @GetMapping("/users") public List<UserDto> users() { return admin.allUsers(); }
    @GetMapping("/ngos/pending") public List<UserDto> pendingNgos() { return admin.pendingNgos(); }
    @PutMapping("/ngos/{id}/approve") public UserDto approve(@PathVariable Long id) { return admin.approveNgo(id); }
    @PutMapping("/ngos/{id}/reject") public UserDto reject(@PathVariable Long id) { return admin.rejectNgo(id); }
    @PutMapping("/users/{id}/status") public UserDto status(@PathVariable Long id, @RequestParam boolean active) { return admin.setActive(id, active); }
    @GetMapping("/donations") public List<DonationDto> donations() { return donations.allForAdmin(); }
}
