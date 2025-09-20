package com.eon.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@SpringBootApplication
public class EonBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(EonBootApplication.class, args);
    }

    @RestController
    static class HelloController {
        @GetMapping("/")
        public Map<String, Object> root() {
            return Map.of(
                    "app", "eon-boot",
                    "status", "ok",
                    "time", Instant.now().toString()
            );
        }
    }
}
