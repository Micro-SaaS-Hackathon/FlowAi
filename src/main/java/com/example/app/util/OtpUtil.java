package com.example.app.util;

import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
public class OtpUtil {

    public String generateOtp() {
        String output;
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();

            int randomNumber = random.nextInt(999999);
            output = String.valueOf(randomNumber);
            while (output.length() < 6) {
                output = "0" + output;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }


        return output;
    }
}
