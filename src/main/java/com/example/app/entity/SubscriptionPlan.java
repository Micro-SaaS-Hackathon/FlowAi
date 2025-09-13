package com.example.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Plan adı boş ola bilməz")
    private String name;

    private String description;

    @NotNull(message = "Qiymət boş ola bilməz")
    @DecimalMin(value = "0.0", inclusive = false, message = "Qiymət 0-dan böyük olmalıdır")
    private BigDecimal price;

    private boolean isActive = true;
}
