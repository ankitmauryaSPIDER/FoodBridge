package com.foodbridge.controller;

import com.foodbridge.dto.ApiDtos.*;
import com.foodbridge.service.DonationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/donations")
public class DonationController {
    private final DonationService service;
    public DonationController(DonationService service) { this.service = service; }
    @GetMapping public List<DonationDto> available() { return service.available(); }
    @GetMapping("/mine") public List<DonationDto> mine(Authentication auth) { return service.mine(auth.getName()); }
    @GetMapping("/{id}") public DonationDto getOne(Authentication auth, @PathVariable Long id) { return service.getOne(auth.getName(), id); }
    @PostMapping public DonationDto create(Authentication auth, @RequestBody DonationCreateRequest request) { return service.create(auth.getName(), request); }
    @PutMapping("/{id}") public DonationDto update(Authentication auth, @PathVariable Long id, @RequestBody DonationCreateRequest request) { return service.update(auth.getName(), id, request); }
    @DeleteMapping("/{id}") public MessageResponse cancel(Authentication auth, @PathVariable Long id) { service.cancel(auth.getName(), id); return new MessageResponse("Donation cancelled"); }
}
