package com.marketplacem.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "Gateway Service is up";
    }

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello from gateway-service";
    }
}
