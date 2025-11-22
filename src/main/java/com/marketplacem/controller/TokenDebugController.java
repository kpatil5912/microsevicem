package com.marketplacem.controller;

import com.marketplacem.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TokenDebugController {

    private final JwtUtil jwtUtil;

    public TokenDebugController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/debug/token")
    public Mono<Map<String, Object>> debugToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        Map<String, Object> response = new HashMap<>();

        try {
            Claims claims = jwtUtil.extractAllClaims(token);
            response.put("valid", true);
            response.put("subject", claims.getSubject());
            response.put("expiration", claims.getExpiration());
            response.put("issuedAt", claims.getIssuedAt());
            response.put("claims", claims);
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
        }

        return Mono.just(response);
    }
}