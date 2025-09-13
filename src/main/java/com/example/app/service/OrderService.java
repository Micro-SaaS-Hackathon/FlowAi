package com.example.app.service;

import com.example.app.entity.Order;
import com.example.app.entity.OrderItem;
import com.example.app.entity.Product;
import com.example.app.entity.User;
import com.example.app.enums.OrderStatus;
import com.example.app.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Transactional
    public Order createOrder(User user, List<Map<String, Object>> items) {
        Order order = new Order();
        order.setUser(user);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("productId").toString());
            Integer quantity = Integer.valueOf(item.get("quantity").toString());

            Product product = productService.getProductById(productId);

            if (!productService.checkStock(productId, quantity)) {
                throw new RuntimeException("Məhsul: " + product.getName() + " - kifayət qədər stok yoxdur");
            }

            OrderItem orderItem = new OrderItem(order, product, quantity);
            order.getOrderItems().add(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Stoku azald
        for (OrderItem item : savedOrder.getOrderItems()) {
            productService.reduceStock(item.getProduct().getId(), item.getQuantity());
        }

        return savedOrder;
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sifariş tapılmadı"));
    }

    @Transactional
    public Order processPayment(Long orderId, String paymentMethod) {
        Order order = getOrderById(orderId);

        // Sadə payment simulation
        boolean paymentSuccess = simulatePayment(order.getTotalAmount(), paymentMethod);

        if (paymentSuccess) {
            order.setPaymentStatus("COMPLETED");
            order.setPaymentMethod(paymentMethod);
            order.setStatus(OrderStatus.CONFIRMED);
        } else {
            order.setPaymentStatus("FAILED");
        }

        return orderRepository.save(order);
    }

    private boolean simulatePayment(BigDecimal amount, String method) {
        // Hackathon üçün sadə simulation
        // Real həyatda payment gateway integration olardı
        return !method.equals("FAIL_TEST"); // Test üçün FAIL_TEST göndərsən payment fail olar
    }
}
