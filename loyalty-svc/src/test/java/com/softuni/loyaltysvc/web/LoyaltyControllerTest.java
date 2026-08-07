package com.softuni.loyaltysvc.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softuni.loyaltysvc.dto.AwardPointsRequest;
import com.softuni.loyaltysvc.dto.RedeemPointsRequest;
import com.softuni.loyaltysvc.exception.InsufficientPointsException;
import com.softuni.loyaltysvc.service.LoyaltyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoyaltyController.class)
class LoyaltyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoyaltyService loyaltyService;

    @Test
    void getBalance_returnsOkWithBalance() throws Exception {
        UUID userId = UUID.randomUUID();
        when(loyaltyService.getBalance(userId)).thenReturn(150);

        mockMvc.perform(get("/api/loyalty/balance/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsBalance").value(150));
    }

    @Test
    void awardPoints_validRequest_returnsOkWithNewBalance() throws Exception {
        AwardPointsRequest request = new AwardPointsRequest(UUID.randomUUID(), 3);
        when(loyaltyService.awardPoints(any(), anyInt())).thenReturn(30);

        mockMvc.perform(post("/api/loyalty/award")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsBalance").value(30));
    }

    @Test
    void awardPoints_invalidNights_returnsBadRequest() throws Exception {
        AwardPointsRequest request = new AwardPointsRequest(UUID.randomUUID(), -1);

        mockMvc.perform(post("/api/loyalty/award")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void redeemPoints_insufficientBalance_returnsBadRequest() throws Exception {
        RedeemPointsRequest request = new RedeemPointsRequest(UUID.randomUUID(), 999);
        when(loyaltyService.redeemPoints(any(), anyInt()))
                .thenThrow(new InsufficientPointsException("Cannot redeem 999 points; account balance is only 50"));

        mockMvc.perform(post("/api/loyalty/redeem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot redeem 999 points; account balance is only 50"));
    }
}