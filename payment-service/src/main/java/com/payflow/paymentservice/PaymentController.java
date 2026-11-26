package com.payflow.paymentservice;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentRepository repository;
    public PaymentController(PaymentRepository repository) { this.repository = repository; }

    // Starter endpoint only: idempotency and Saga orchestration are next milestones.
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> create(@Valid @RequestBody CreatePaymentRequest request,
                                      @RequestHeader("Idempotency-Key") String idempotencyKey) {
        Payment payment = repository.save(new Payment(request.fromAccount(),
            request.toAccount(), request.amount(), request.currency()));
        return Map.of("paymentId", payment.getPaymentId(),
            "status", payment.getStatus(),
            "idempotencyKeyReceived", idempotencyKey,
            "message", "Payment accepted; orchestration will be added in the next milestone");
    }

    public record CreatePaymentRequest(
        @NotBlank String fromAccount,
        @NotBlank String toAccount,
        @NotNull @DecimalMin(value="0.01") BigDecimal amount,
        @NotBlank @Size(min=3,max=3) String currency) {}
}
