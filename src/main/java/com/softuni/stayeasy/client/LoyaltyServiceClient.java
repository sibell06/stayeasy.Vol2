package com.softuni.stayeasy.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "loyalty-svc", url = "${loyalty-svc.url}")
public interface LoyaltyServiceClient {

    @GetMapping("/api/loyalty/balance/{userId}")
    Map<String, Object> getBalance(@PathVariable("userId") UUID userId);

    @PostMapping("/api/loyalty/award")
    Map<String, Object> awardPoints(@RequestParam("userId") UUID userId, @RequestParam("nights") int nights);

    @PostMapping("/api/loyalty/redeem")
    Map<String, Object> redeemPoints(@RequestParam("userId") UUID userId, @RequestParam("points") int points);
}