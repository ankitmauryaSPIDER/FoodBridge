package com.foodbridge.service;

import com.foodbridge.dto.ApiDtos.*;
import com.foodbridge.entity.Domain;
import com.foodbridge.entity.UserEntity;
import com.foodbridge.repository.UserRepository;
import com.foodbridge.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    public AuthService(UserRepository users, PasswordEncoder encoder, AuthenticationManager authManager, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.authManager = authManager; this.jwt = jwt;
    }
    public AuthResponse register(RegisterRequest req) {
        if (req.name() == null || req.name().isBlank() || req.email() == null || req.email().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and email are required");
        if (req.password() == null || req.password().length() < 8)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must contain at least 8 characters");
        Domain.Role role;
        try { role = Domain.Role.valueOf(req.role().toUpperCase(Locale.ROOT)); }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose DONOR, NGO or VOLUNTEER"); }
        if (role == Domain.Role.ADMIN) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin accounts are created by the system administrator");
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        if (role == Domain.Role.NGO && (req.organizationName() == null || req.organizationName().isBlank()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organization name is required for NGO registration");
        UserEntity user = new UserEntity();
        user.setName(req.name().trim()); user.setEmail(email); user.setPassword(encoder.encode(req.password()));
        user.setPhone(req.phone()); user.setRole(role); user.setOrganizationName(req.organizationName());
        user.setAddress(req.address()); user.setCity(req.city()); user.setState(req.state()); user.setPincode(req.pincode());
        user.setVerified(role != Domain.Role.NGO); user.setActive(true);
        users.save(user);
        return new AuthResponse(jwt.generateToken(user.getEmail()), toDto(user));
    }
    public AuthResponse login(LoginRequest req) {
        try { authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email().toLowerCase(Locale.ROOT), req.password())); }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"); }
        UserEntity user = users.findByEmail(req.email().toLowerCase(Locale.ROOT)).orElseThrow();
        if (!user.isActive()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is currently disabled");
        return new AuthResponse(jwt.generateToken(user.getEmail()), toDto(user));
    }
    public UserDto me(String email) { return toDto(users.findByEmail(email).orElseThrow()); }
    public static UserDto toDto(UserEntity u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole().name(), u.isVerified(), u.getOrganizationName(), u.getCity(), u.isActive(), u.getCreatedAt());
    }
}
