package com.payflow.banksimulator;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/api/bank-simulator/health")
    public Map<String, String> health() {
        return Map.of("service", "bank-simulator", "status", "UP");
    }
}
