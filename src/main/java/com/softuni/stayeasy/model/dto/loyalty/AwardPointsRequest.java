package com.softuni.stayeasy.model.dto.loyalty;

import java.util.UUID;

public class AwardPointsRequest {

    private UUID userId;
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