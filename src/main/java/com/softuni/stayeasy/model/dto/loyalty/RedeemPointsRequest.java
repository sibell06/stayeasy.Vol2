package com.softuni.stayeasy.model.dto.loyalty;

import java.util.UUID;

public class RedeemPointsRequest {

    private UUID userId;
    private int points;

    public RedeemPointsRequest() {
    }

    public RedeemPointsRequest(UUID userId, int points) {
        this.userId = userId;
        this.points = points;
    }

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