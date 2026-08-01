package com.softuni.loyaltysvc.repository;

import com.softuni.loyaltysvc.model.LoyaltyAccount;
import com.softuni.loyaltysvc.model.PointsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, UUID> {

    List<PointsTransaction> findAllByAccount(LoyaltyAccount account);
}