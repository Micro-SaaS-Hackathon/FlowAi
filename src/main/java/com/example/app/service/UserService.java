package com.example.app.service;


import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import com.example.app.util.EmailUtil;
import com.example.app.util.OtpUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service

public class UserService implements UserDetailsService {

    @Autowired
    private EmailUtil emailUtil;
    @Autowired
    private OtpUtil otpUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User tapılmadı: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>()
        );
    }

    public User createUser(User user) {

        String otp = otpUtil.generateOtp();
        try {
            emailUtil.sendOtpEmail(user.getEmail(), otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to sent otp,try again");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username artıq mövcuddur");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email artıq mövcuddur");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setOtp(otp);
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User tapılmadı"));
    }

    public String verifyAccount(String email,String otp){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found by email: " + email));

        if(user.getOtp().equals(otp) && Duration.between(user.getCreatedTime(),
                LocalDateTime.now()).getSeconds() < (1*60)) {
            user.setActive(true);
            userRepository.save(user);
            return "OTP_SUCCESS";
        }

        return "OTP_INVALID";
    }
}
