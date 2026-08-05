package com.softuni.stayeasy.client;

import com.softuni.stayeasy.model.dto.loyalty.AwardPointsRequest;
import com.softuni.stayeasy.model.dto.loyalty.RedeemPointsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "loyalty-svc", url = "${loyalty-svc.url}")
public interface LoyaltyServiceClient {

    @GetMapping("/api/loyalty/balance/{userId}")
    Map<String, Object> getBalance(@PathVariable("userId") UUID userId);

    @PostMapping("/api/loyalty/award")
    Map<String, Object> awardPoints(@RequestBody AwardPointsRequest request);

    @PostMapping("/api/loyalty/redeem")
    Map<String, Object> redeemPoints(@RequestBody RedeemPointsRequest request);
}