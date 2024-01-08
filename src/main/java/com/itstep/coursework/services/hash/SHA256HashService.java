package com.itstep.coursework.services.hash;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@Primary
public class SHA256HashService implements HashService {
    @Override
    public String hash(String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            for (int b : messageDigest.digest(input.getBytes(StandardCharsets.UTF_8))) {
                sb.append(String.format("%02x", b & 0xFF));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
