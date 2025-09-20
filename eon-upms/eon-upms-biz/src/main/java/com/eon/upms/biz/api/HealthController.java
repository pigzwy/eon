package com.eon.upms.biz.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/actuator/health")
    public Map<String, Object> health() {
        return Map.of(
                "service", "eon-upms-biz",
                "status", "UP",
                "time", Instant.now().toString()
        );
    }
}
