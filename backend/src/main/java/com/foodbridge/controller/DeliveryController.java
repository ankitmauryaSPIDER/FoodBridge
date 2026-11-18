package com.foodbridge.controller;

import com.foodbridge.dto.ApiDtos.*;
import com.foodbridge.service.DeliveryService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
    private final DeliveryService service;
    public DeliveryController(DeliveryService service) { this.service = service; }
    @GetMapping("/available") public List<DeliveryDto> available(Authentication auth) { return service.available(auth.getName()); }
    @GetMapping("/mine") public List<DeliveryDto> mine(Authentication auth) { return service.mine(auth.getName()); }
    @PutMapping("/{id}/claim") public DeliveryDto claim(Authentication auth, @PathVariable Long id) { return service.claim(auth.getName(), id); }
    @PutMapping("/{id}/pickup") public DeliveryDto pickup(Authentication auth, @PathVariable Long id) { return service.pickup(auth.getName(), id); }
    @PutMapping("/{id}/complete") public DeliveryDto complete(Authentication auth, @PathVariable Long id, @RequestBody CompleteDeliveryRequest request) { return service.complete(auth.getName(), id, request.verificationCode()); }
}
