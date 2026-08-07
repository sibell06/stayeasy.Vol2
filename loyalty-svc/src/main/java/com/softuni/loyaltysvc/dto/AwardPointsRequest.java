package com.softuni.loyaltysvc.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AwardPointsRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @Min(value = 1, message = "Nights must be at least 1")
    private int nights;

    public AwardPointsRequest() {
    }

    public AwardPointsRequest(UUID userId, int nights) {
        this.userId = userId;
        this.nights = nights;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public int getNights() {
        return nights;
    }

    public void setNights(int nights) {
        this.nights = nights;
    }
}