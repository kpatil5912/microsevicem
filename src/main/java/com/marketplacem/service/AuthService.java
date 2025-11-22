package com.marketplacem.service;

import com.marketplacem.dto.AuthRequest;
import com.marketplacem.dto.AuthResponse;
import com.marketplacem.security.JwtService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AuthService {

    private final JwtService jwtService;

    public AuthService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public AuthResponse authenticate(AuthRequest request) {
        // For testing purposes, just create a UserDetails object directly
        // In a real application, you would validate credentials and fetch the user
        UserDetails userDetails = new User(
            request.getUsername(),
            "", // No password needed for JWT
            new ArrayList<>()
        );

        // Generate JWT token
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token);
    }
}
