package com.softuni.loyaltysvc.service;

import com.softuni.loyaltysvc.exception.InsufficientPointsException;
import com.softuni.loyaltysvc.model.LoyaltyAccount;
import com.softuni.loyaltysvc.model.PointsTransaction;
import com.softuni.loyaltysvc.repository.LoyaltyAccountRepository;
import com.softuni.loyaltysvc.repository.PointsTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LoyaltyService {

    private static final Logger logger = LoggerFactory.getLogger(LoyaltyService.class);

    private static final int POINTS_PER_NIGHT = 10;
    private static final int POINTS_PER_DOLLAR_DISCOUNT = 10;

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final PointsTransactionRepository pointsTransactionRepository;

    public LoyaltyService(LoyaltyAccountRepository loyaltyAccountRepository,
                          PointsTransactionRepository pointsTransactionRepository) {
        this.loyaltyAccountRepository = loyaltyAccountRepository;
        this.pointsTransactionRepository = pointsTransactionRepository;
    }

    public int getBalance(UUID userId) {
        return getOrCreateAccount(userId).getPointsBalance();
    }

    @Transactional
    public int awardPoints(UUID userId, int nights) {
        LoyaltyAccount account = getOrCreateAccount(userId);

        int pointsEarned = nights * POINTS_PER_NIGHT;
        account.setPointsBalance(account.getPointsBalance() + pointsEarned);
        account.setUpdatedOn(LocalDateTime.now());
        loyaltyAccountRepository.save(account);

        PointsTransaction transaction = PointsTransaction.builder()
                .account(account)
                .points(pointsEarned)
                .reason("Stay completed: " + nights + " night(s)")
                .createdOn(LocalDateTime.now())
                .build();
        pointsTransactionRepository.save(transaction);

        logger.info("Awarded {} points to user {} for {} night(s). New balance: {}",
                pointsEarned, userId, nights, account.getPointsBalance());

        return account.getPointsBalance();
    }

    @Transactional
    public double redeemPoints(UUID userId, int pointsToRedeem) {
        LoyaltyAccount account = getOrCreateAccount(userId);

        if (pointsToRedeem > account.getPointsBalance()) {
            logger.warn("User {} attempted to redeem {} points but only has {}",
                    userId, pointsToRedeem, account.getPointsBalance());
            throw new InsufficientPointsException(
                    "Cannot redeem " + pointsToRedeem + " points; account balance is only " + account.getPointsBalance());
        }

        account.setPointsBalance(account.getPointsBalance() - pointsToRedeem);
        account.setUpdatedOn(LocalDateTime.now());
        loyaltyAccountRepository.save(account);

        PointsTransaction transaction = PointsTransaction.builder()
                .account(account)
                .points(-pointsToRedeem)
                .reason("Redeemed for booking discount")
                .createdOn(LocalDateTime.now())
                .build();
        pointsTransactionRepository.save(transaction);

        logger.info("User {} redeemed {} points for a ${} discount. New balance: {}",
                userId, pointsToRedeem, (double) pointsToRedeem / POINTS_PER_DOLLAR_DISCOUNT, account.getPointsBalance());

        return (double) pointsToRedeem / POINTS_PER_DOLLAR_DISCOUNT;
    }

    private LoyaltyAccount getOrCreateAccount(UUID userId) {
        return loyaltyAccountRepository.findByUserId(userId)
                .orElseGet(() -> {
                    LoyaltyAccount newAccount = LoyaltyAccount.builder()
                            .userId(userId)
                            .pointsBalance(0)
                            .createdOn(LocalDateTime.now())
                            .updatedOn(LocalDateTime.now())
                            .build();
                    return loyaltyAccountRepository.save(newAccount);
                });
    }
}