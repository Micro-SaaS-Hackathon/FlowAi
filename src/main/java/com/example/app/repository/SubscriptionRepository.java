package com.example.app.repository;

import com.example.app.entity.Subscription;
import com.example.app.entity.User;
import com.example.app.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository  extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserOrderByCreatedAtDesc(User user);
    Optional<Subscription> findByUserAndStatus(User user, SubscriptionStatus status);
    List<Subscription> findByPaymentStatus(String paymentStatus);
}
