package com.example.app.controller;

import com.example.app.dto.OrderDTO;
import com.example.app.entity.Order;
import com.example.app.entity.User;
import com.example.app.service.OrderService;
import com.example.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByUsername(auth.getName());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");

            Order order = orderService.createOrder(user, items);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("totalAmount", order.getTotalAmount());
            response.put("status", order.getStatus());
            response.put("message", "Sifariş uğurla yaradıldı");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/payment")
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> request) {
        try {
            Long orderId = Long.valueOf(request.get("orderId").toString());
            String paymentMethod = request.get("paymentMethod").toString();

            Order order = orderService.processPayment(orderId, paymentMethod);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("paymentStatus", order.getPaymentStatus());
            response.put("orderStatus", order.getStatus());

            if ("COMPLETED".equals(order.getPaymentStatus())) {
                response.put("message", "Ödəniş uğurla tamamlandı");
            } else {
                response.put("message", "Ödəniş uğursuz oldu");
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/orders")
    public ResponseEntity<?> getUserOrders() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByUsername(auth.getName());


            List<Order> orders = orderService.getUserOrders(user);

            List<OrderDTO> orderDTOs = orders.stream()
                    .map(OrderDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(orderDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);

            OrderDTO orderDTO = new OrderDTO(order);
            return ResponseEntity.ok(orderDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
