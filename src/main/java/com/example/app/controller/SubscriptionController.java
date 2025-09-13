package com.example.app.controller;

import com.example.app.entity.Subscription;
import com.example.app.entity.SubscriptionPlan;
import com.example.app.entity.User;
import com.example.app.service.SubscriptionService;
import com.example.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserService userService;

    @PostMapping("/admin/plans")
    public ResponseEntity<?> createPlan(@Valid @RequestBody SubscriptionPlan plan) {
        try {
            SubscriptionPlan createdPlan = subscriptionService.createPlan(plan);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Plan uğurla yaradıldı");
            response.put("planId", createdPlan.getId());
            response.put("planName", createdPlan.getName());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/admin/plans/{planId}")
    public ResponseEntity<?> updatePlan(@PathVariable Long planId,
                                        @Valid @RequestBody SubscriptionPlan plan) {
        try {
            SubscriptionPlan updatedPlan = subscriptionService.updatePlan(planId, plan);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Plan uğurla yeniləndi");
            response.put("planId", updatedPlan.getId());
            response.put("planName", updatedPlan.getName());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/plans/{planId}")
    public ResponseEntity<?> deletePlan(@PathVariable Long planId) {
        try {
            subscriptionService.deletePlan(planId);
            return ResponseEntity.ok(Map.of("message", "Plan deaktiv edildi"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/plans")
    public ResponseEntity<List<SubscriptionPlan>> getAllPlans() {
        // Admin üçün - bütün planları gör (aktiv + deaktiv)
        List<SubscriptionPlan> plans = subscriptionService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/{planId}")
    public ResponseEntity<?> getPlanDetails(@PathVariable Long planId) {
        try {
            SubscriptionPlan plan = subscriptionService.getPlanById(planId);
            return ResponseEntity.ok(plan);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlan>> getAvailablePlans() {
        List<SubscriptionPlan> plans = subscriptionService.getAvailablePlans();
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseSubscription(@RequestBody Map<String, Object> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(auth.getName());

            Long planId = Long.valueOf(request.get("planId").toString());

            Subscription subscription = subscriptionService.createSubscription(user, planId);

            Map<String, Object> response = new HashMap<>();
            response.put("subscriptionId", subscription.getId());
            response.put("planName", subscription.getPlan().getName());
            response.put("amount", subscription.getPaidAmount());
            response.put("status", subscription.getStatus());
            response.put("message", "Abonelik sifarişi yaradıldı");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/payment")
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> request) {
        try {
            Long subscriptionId = Long.valueOf(request.get("subscriptionId").toString());
            String paymentMethod = request.get("paymentMethod").toString();

            Subscription subscription = subscriptionService.processSubscriptionPayment(subscriptionId, paymentMethod);

            Map<String, Object> response = new HashMap<>();
            response.put("subscriptionId", subscription.getId());
            response.put("paymentStatus", subscription.getPaymentStatus());
            response.put("subscriptionStatus", subscription.getStatus());

            if ("COMPLETED".equals(subscription.getPaymentStatus())) {
                response.put("message", "Premium abonelik aktivləşdirildi!");
            } else {
                response.put("message", "Ödəniş uğursuz oldu");
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-subscriptions")
    public ResponseEntity<?> getUserSubscriptions() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(auth.getName());

            List<Subscription> subscriptions = subscriptionService.getUserSubscriptions(user);
            return ResponseEntity.ok(subscriptions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
