package com.foodbridge.entity;

public final class Domain {
    private Domain() {}
    public enum Role { DONOR, NGO, VOLUNTEER, ADMIN }
    public enum DonationStatus { AVAILABLE, RESERVED, PICKED_UP, DELIVERED, EXPIRED, CANCELLED }
    public enum FoodType { VEGETARIAN, NON_VEGETARIAN, VEGAN, OTHER }
    public enum RequestStatus { PENDING, ACCEPTED, REJECTED, CANCELLED }
    public enum DeliveryStatus { READY_FOR_PICKUP, PICKED_UP, COMPLETED, CANCELLED }
}
