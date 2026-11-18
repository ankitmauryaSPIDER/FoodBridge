package com.foodbridge.controller;

import com.foodbridge.dto.ApiDtos.*;
import com.foodbridge.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }
    @PostMapping("/register") public AuthResponse register(@RequestBody RegisterRequest request) { return auth.register(request); }
    @PostMapping("/login") public AuthResponse login(@RequestBody LoginRequest request) { return auth.login(request); }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me") public UserDto me(Authentication authn) { return auth.me(authn.getName()); }
    @PostMapping("/logout") public MessageResponse logout() { return new MessageResponse("Logged out on this device. Clear the stored token in the browser."); }
}
