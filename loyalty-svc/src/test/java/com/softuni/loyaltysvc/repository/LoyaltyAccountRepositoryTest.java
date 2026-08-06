package com.softuni.loyaltysvc.repository;

import com.softuni.loyaltysvc.model.LoyaltyAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LoyaltyAccountRepositoryTest {

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Test
    void save_and_findByUserId_returnsCorrectAccount() {
        UUID userId = UUID.randomUUID();
        LoyaltyAccount account = LoyaltyAccount.builder()
                .userId(userId)
                .pointsBalance(100)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        loyaltyAccountRepository.save(account);

        Optional<LoyaltyAccount> found = loyaltyAccountRepository.findByUserId(userId);

        assertTrue(found.isPresent());
        assertEquals(100, found.get().getPointsBalance());
        assertEquals(userId, found.get().getUserId());
    }

    @Test
    void findByUserId_nonExistentUser_returnsEmpty() {
        Optional<LoyaltyAccount> found = loyaltyAccountRepository.findByUserId(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void save_duplicateUserId_throwsConstraintViolation() {
        UUID userId = UUID.randomUUID();
        LoyaltyAccount first = LoyaltyAccount.builder()
                .userId(userId)
                .pointsBalance(10)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        loyaltyAccountRepository.saveAndFlush(first);

        LoyaltyAccount duplicate = LoyaltyAccount.builder()
                .userId(userId)
                .pointsBalance(20)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        assertThrows(Exception.class, () -> loyaltyAccountRepository.saveAndFlush(duplicate));
    }
}