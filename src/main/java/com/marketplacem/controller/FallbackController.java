package com.marketplacem.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/api/fallback")
    public Mono<Map<String, String>> fallback() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Service is currently unavailable. Please try again later.");
        return Mono.just(response);
    }
}