package com.example.app.service;

import com.example.app.entity.Subscription;
import com.example.app.entity.SubscriptionPlan;
import com.example.app.entity.User;
import com.example.app.enums.SubscriptionStatus;
import com.example.app.repository.SubscriptionPlanRepository;
import com.example.app.repository.SubscriptionRepository;
import com.example.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository planRepository;

    @Autowired
    private UserRepository userRepository;

    public SubscriptionPlan createPlan(SubscriptionPlan plan) {
        return planRepository.save(plan);
    }

    public SubscriptionPlan updatePlan(Long planId, SubscriptionPlan updatedPlan) {
        SubscriptionPlan existingPlan = getPlanById(planId);

        existingPlan.setName(updatedPlan.getName());
        existingPlan.setDescription(updatedPlan.getDescription());
        existingPlan.setPrice(updatedPlan.getPrice());
        existingPlan.setActive(updatedPlan.isActive());

        return planRepository.save(existingPlan);
    }

    public void deletePlan(Long planId) {
        SubscriptionPlan plan = getPlanById(planId);
        plan.setActive(false); // Soft delete - plan-ı deaktiv et
        planRepository.save(plan);
    }

    public List<SubscriptionPlan> getAllPlans() {
        return planRepository.findAll(); // Admin üçün bütün planlar (aktiv + deaktiv)
    }


    public List<SubscriptionPlan> getAvailablePlans() {
        return planRepository.findByIsActiveTrue();
    }

    public SubscriptionPlan getPlanById(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan tapılmadı"));
    }

    @Transactional
    public Subscription createSubscription(User user, Long planId) {
        SubscriptionPlan plan = getPlanById(planId);

        // Əvvəlki aktiv subscription-ı yoxla
        Optional<Subscription> existingActive = subscriptionRepository
                .findByUserAndStatus(user, SubscriptionStatus.ACTIVE);

        if (existingActive.isPresent()) {
            throw new RuntimeException("Artıq aktiv abonelik mövcuddur");
        }

        Subscription subscription = new Subscription(user, plan);
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription processSubscriptionPayment(Long subscriptionId, String paymentMethod) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Abonelik tapılmadı"));

        // Payment simulation
        boolean paymentSuccess = simulatePayment(subscription.getPaidAmount(), paymentMethod);

        if (paymentSuccess) {
            subscription.setPaymentStatus("COMPLETED");
            subscription.setPaymentMethod(paymentMethod);
            subscription.setStatus(SubscriptionStatus.ACTIVE);

            // Premium müddətini hesabla
            // User-in account type-ını premium et
            User user = subscription.getUser();
            user.setAccountType("PREMIUM");
            userRepository.save(user);

        } else {
            subscription.setPaymentStatus("FAILED");
        }

        return subscriptionRepository.save(subscription);
    }

    public List<Subscription> getUserSubscriptions(User user) {
        return subscriptionRepository.findByUserOrderByCreatedAtDesc(user);
    }


    private boolean simulatePayment(BigDecimal amount, String method) {
        // Hackathon üçün sadə simulation
        return !method.equals("FAIL_TEST");
    }
}
