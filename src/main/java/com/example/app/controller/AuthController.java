package com.example.app.controller;

import com.example.app.dto.VerifyOtpRequest;
import com.example.app.entity.User;
import com.example.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Uğurla daxil oldunuz");
            response.put("email", auth.getName()); // email qaytarır
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Giriş uğursuz"));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            try {
                User user = userService.findByEmail(auth.getName());
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Uğurla çıxış etdiniz"));
    }

    @PutMapping("/verify-otp")
    public ResponseEntity<String> verifyAccount(@RequestBody VerifyOtpRequest request){
        String result = userService.verifyAccount(request.getEmail(), request.getOtp());

        if ("OTP_SUCCESS".equals(result)) {
            return ResponseEntity.ok("OTP verified successfully");
        } else if ("OTP_INVALID".equals(result)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please regenerate OTP");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknown error");
        }
    }
}
