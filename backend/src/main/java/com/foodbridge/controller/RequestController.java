package com.foodbridge.controller;

import com.foodbridge.dto.ApiDtos.*;
import com.foodbridge.service.RequestService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    private final RequestService service;
    public RequestController(RequestService service) { this.service = service; }
    @PostMapping public RequestDto create(Authentication auth, @RequestBody RequestCreateRequest request) { return service.create(auth.getName(), request); }
    @GetMapping("/mine") public List<RequestDto> mine(Authentication auth) { return service.mine(auth.getName()); }
    @PutMapping("/{id}/accept") public RequestDto accept(Authentication auth, @PathVariable Long id) { return service.accept(auth.getName(), id); }
    @PutMapping("/{id}/reject") public RequestDto reject(Authentication auth, @PathVariable Long id) { return service.reject(auth.getName(), id); }
    @PutMapping("/{id}/cancel") public MessageResponse cancel(Authentication auth, @PathVariable Long id) { service.cancel(auth.getName(), id); return new MessageResponse("Request cancelled"); }
}
