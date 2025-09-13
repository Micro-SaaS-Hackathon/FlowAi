package com.example.app.controller;

import com.example.app.entity.User;
import com.example.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Uğurla qeydiyyatdan keçdiniz");
            response.put("username", createdUser.getUsername());
            response.put("email", createdUser.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            try {
                User user = userService.findByUsername(auth.getName());
                Map<String, Object> response = new HashMap<>();
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());
                response.put("role", user.getRole());
                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User məlumatları tapılmadı");
                return ResponseEntity.badRequest().body(error);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Giriş etmədiniz"));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn() {
        // Basic Auth ilə avtomatik authentication aparılır
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Uğurla daxil oldunuz");
            response.put("username", auth.getName());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Giriş uğursuz"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Uğurla çıxış etdiniz"));
    }
}
