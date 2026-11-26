package com.payflow.accountservice;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/api/account-service/health")
    public Map<String, String> health() {
        return Map.of("service", "account-service", "status", "UP");
    }
}
