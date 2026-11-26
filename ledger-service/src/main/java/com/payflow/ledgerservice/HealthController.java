package com.payflow.ledgerservice;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/api/ledger-service/health")
    public Map<String, String> health() {
        return Map.of("service", "ledger-service", "status", "UP");
    }
}
