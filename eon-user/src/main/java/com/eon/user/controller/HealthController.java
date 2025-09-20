package com.eon.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {
    
    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "app", "eon-user",
                "status", "ok",
                "time", Instant.now().toString()
        );
    }
    
    @GetMapping("/_health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "app", "eon-user",
                "timestamp", Instant.now().toString()
        );
    }
}