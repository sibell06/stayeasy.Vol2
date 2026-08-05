package com.softuni.loyaltysvc.web;

import com.softuni.loyaltysvc.dto.AwardPointsRequest;
import com.softuni.loyaltysvc.dto.RedeemPointsRequest;
import com.softuni.loyaltysvc.service.LoyaltyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/loyalty")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    public LoyaltyController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @GetMapping("/balance/{userId}")
    public Map<String, Object> getBalance(@PathVariable UUID userId) {
        int balance = loyaltyService.getBalance(userId);
        return Map.of("userId", userId, "pointsBalance", balance);
    }

    @PostMapping("/award")
    public Map<String, Object> awardPoints(@Valid @RequestBody AwardPointsRequest request) {
        int newBalance = loyaltyService.awardPoints(request.getUserId(), request.getNights());
        return Map.of("userId", request.getUserId(), "pointsBalance", newBalance);
    }

    @PostMapping("/redeem")
    public Map<String, Object> redeemPoints(@Valid @RequestBody RedeemPointsRequest request) {
        double discount = loyaltyService.redeemPoints(request.getUserId(), request.getPoints());
        return Map.of("userId", request.getUserId(), "discountAmount", discount);
    }
}