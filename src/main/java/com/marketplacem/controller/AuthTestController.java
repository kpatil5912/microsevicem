package com.marketplacem.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthTestController {

    @GetMapping("/api/auth-test")
    public Mono<Map<String, Object>> authTest() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(authentication -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("authenticated", authentication.isAuthenticated());
                    response.put("principal", authentication.getPrincipal());
                    response.put("authorities", authentication.getAuthorities());
                    return response;
                })
                .defaultIfEmpty(Map.of("authenticated", false, "message", "No authentication found"));
    }
}