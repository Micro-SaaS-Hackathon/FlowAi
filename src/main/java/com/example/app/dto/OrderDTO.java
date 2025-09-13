package com.example.app.dto;

import com.example.app.entity.Order;
import com.example.app.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDTO {

    private Long orderId;
    private Long userId;
    private String username;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;

    public OrderDTO(Order order) {
        this.orderId = order.getId();
        this.userId = order.getUser().getId();
        this.username = order.getUser().getUsername();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.paymentStatus = order.getPaymentStatus();
        this.paymentMethod = order.getPaymentMethod();
        this.createdAt = order.getCreatedAt();
        this.items = order.getOrderItems().stream()
                .map(OrderItemDTO::new)
                .collect(Collectors.toList());
    }

    // Getters
    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItemDTO> getItems() { return items; }
}
