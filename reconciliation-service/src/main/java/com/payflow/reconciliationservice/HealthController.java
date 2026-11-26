package com.payflow.reconciliationservice;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/api/reconciliation-service/health")
    public Map<String, String> health() {
        return Map.of("service", "reconciliation-service", "status", "UP");
    }
}
