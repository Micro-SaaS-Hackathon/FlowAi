package com.example.app.entity;

import com.example.app.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;

    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status = SubscriptionStatus.PENDING;

    public Subscription(User user, SubscriptionPlan plan) {
        this.user = user;
        this.plan = plan;
        this.paidAmount = plan.getPrice();
    }

    private LocalDateTime createdAt = LocalDateTime.now();

    // Payment info
    private String paymentMethod;
    private String paymentStatus = "PENDING";
}
