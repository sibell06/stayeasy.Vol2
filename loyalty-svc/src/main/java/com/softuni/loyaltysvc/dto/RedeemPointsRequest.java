package com.softuni.loyaltysvc.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class RedeemPointsRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @Min(value = 1, message = "Points must be at least 1")
    private int points;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}