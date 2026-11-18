package com.foodbridge.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class ApiDtos {
    private ApiDtos() {}
    public record RegisterRequest(String name, String email, String password, String phone, String role,
                                  String organizationName, String address, String city, String state, String pincode) {}
    public record LoginRequest(String email, String password) {}
    public record UserDto(Long id, String name, String email, String phone, String role, boolean verified,
                          String organizationName, String city, boolean active, LocalDateTime createdAt) {}
    public record AuthResponse(String token, UserDto user) {}
    public record DonationCreateRequest(String foodName, String description, String category, BigDecimal quantity,
        String unit, String foodType, LocalDateTime preparationTime, LocalDateTime useByTime,
        LocalDateTime pickupDeadline, String storageConditions, String allergenInformation,
        String packagingInformation, String imageUrl, String address, String city, String state,
        String pincode, Double latitude, Double longitude, Boolean safetyConfirmed) {}
    public record DonationDto(Long id, Long donorId, String donorName, String donorOrganization,
        String foodName, String description, String category, BigDecimal quantity, String unit,
        String foodType, LocalDateTime preparationTime, LocalDateTime useByTime,
        LocalDateTime pickupDeadline, String storageConditions, String allergenInformation,
        String packagingInformation, String imageUrl, String address, String city, String state,
        String pincode, Double latitude, Double longitude, String status, LocalDateTime createdAt) {}
    public record RequestCreateRequest(Long donationId, BigDecimal requestedQuantity, String message) {}
    public record RequestDto(Long id, Long donationId, String foodName, String ngoId, String ngoName,
        String donorId, String donorName, BigDecimal requestedQuantity, String message,
        String requestStatus, LocalDateTime createdAt) {}
    public record DeliveryDto(Long id, Long requestId, Long donationId, String foodName, String donorName,
        String donorPhone, String pickupAddress, String city, String ngoName, String ngoPhone,
        String ngoAddress, String volunteerName, String volunteerPhone, String status,
        String verificationCode, LocalDateTime pickupTime, LocalDateTime deliveredTime) {}
    public record NotificationDto(Long id, String title, String message, String type, boolean read, LocalDateTime createdAt) {}
    public record AdminOverviewDto(long users, long donors, long ngos, long verifiedNgos, long volunteers,
        long donations, long availableDonations, long reservedDonations, long deliveredDonations,
        long pendingRequests, long completedDeliveries) {}
    public record MessageResponse(String message) {}
    public record CompleteDeliveryRequest(String verificationCode) {}
}
