package com.softuni.loyaltysvc.service;

import com.softuni.loyaltysvc.exception.InsufficientPointsException;
import com.softuni.loyaltysvc.model.LoyaltyAccount;
import com.softuni.loyaltysvc.repository.LoyaltyAccountRepository;
import com.softuni.loyaltysvc.repository.PointsTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {

    @Mock
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Mock
    private PointsTransactionRepository pointsTransactionRepository;

    @InjectMocks
    private LoyaltyService loyaltyService;

    private UUID userId;
    private LoyaltyAccount account;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        account = LoyaltyAccount.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .pointsBalance(50)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    @Test
    void getBalance_existingAccount_returnsCurrentBalance() {
        when(loyaltyAccountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        int balance = loyaltyService.getBalance(userId);

        assertEquals(50, balance);
        verify(loyaltyAccountRepository, never()).save(any());
    }

    @Test
    void getBalance_noExistingAccount_createsNewAccountWithZeroBalance() {
        when(loyaltyAccountRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(loyaltyAccountRepository.save(any(LoyaltyAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        int balance = loyaltyService.getBalance(userId);

        assertEquals(0, balance);
        verify(loyaltyAccountRepository, times(1)).save(any(LoyaltyAccount.class));
    }

    @Test
    void awardPoints_validNights_increasesBalanceCorrectly() {
        when(loyaltyAccountRepository.findByUserId(userId)).thenReturn(Optional.of(account));
        when(loyaltyAccountRepository.save(any(LoyaltyAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        int newBalance = loyaltyService.awardPoints(userId, 3);

        assertEquals(80, newBalance);
        verify(pointsTransactionRepository, times(1)).save(any());
    }

    @Test
    void redeemPoints_sufficientBalance_returnsCorrectDiscount() {
        when(loyaltyAccountRepository.findByUserId(userId)).thenReturn(Optional.of(account));
        when(loyaltyAccountRepository.save(any(LoyaltyAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        double discount = loyaltyService.redeemPoints(userId, 30);

        assertEquals(3.0, discount);
        assertEquals(20, account.getPointsBalance());
        verify(pointsTransactionRepository, times(1)).save(any());
    }

    @Test
    void redeemPoints_insufficientBalance_throwsException() {
        when(loyaltyAccountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        InsufficientPointsException exception = assertThrows(
                InsufficientPointsException.class,
                () -> loyaltyService.redeemPoints(userId, 999)
        );

        assertTrue(exception.getMessage().contains("Cannot redeem"));
        verify(loyaltyAccountRepository, never()).save(any());
        verify(pointsTransactionRepository, never()).save(any());
    }
}