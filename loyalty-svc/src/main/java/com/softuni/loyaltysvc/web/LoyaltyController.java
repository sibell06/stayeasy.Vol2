package com.softuni.loyaltysvc.web;

import com.softuni.loyaltysvc.service.LoyaltyService;
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
    public Map<String, Object> awardPoints(@RequestParam UUID userId, @RequestParam int nights) {
        int newBalance = loyaltyService.awardPoints(userId, nights);
        return Map.of("userId", userId, "pointsBalance", newBalance);
    }

    @PostMapping("/redeem")
    public Map<String, Object> redeemPoints(@RequestParam UUID userId, @RequestParam int points) {
        double discount = loyaltyService.redeemPoints(userId, points);
        return Map.of("userId", userId, "discountAmount", discount);
    }
}