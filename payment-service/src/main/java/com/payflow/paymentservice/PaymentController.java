package com.payflow.paymentservice;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentRepository repository;
    private final RestClient accountClient;
    private final ObjectMapper mapper;

    public PaymentController(PaymentRepository repository, ObjectMapper mapper,
                             @Value("${payflow.account.url}") String accountUrl) {
        this.repository = repository;
        this.mapper = mapper;
        this.accountClient = RestClient.builder().baseUrl(accountUrl).build();
    }

    // Milestone 1: synchronous flow Client -> Payment -> Account -> Ledger.
    // Idempotency-Key is accepted but not enforced yet (Milestone 3).
    @PostMapping
    public ResponseEntity<Payment> create(@Valid @RequestBody CreatePaymentRequest request,
                                          @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        // Persisted first so every attempt is visible, even if the downstream call fails.
        Payment payment = repository.save(new Payment(request.fromAccount(),
            request.toAccount(), request.amount(), request.currency()));
        try {
            accountClient.post().uri("/api/accounts/transfer").body(Map.of(
                "paymentId", payment.getPaymentId().toString(),
                "fromAccount", request.fromAccount(),
                "toAccount", request.toAccount(),
                "amount", request.amount(),
                "currency", request.currency()))
                .retrieve().toBodilessEntity();
            payment.markCompleted();
        } catch (RestClientResponseException e) {
            payment.markFailed(extractMessage(e));
        } catch (ResourceAccessException e) {
            payment.markFailed("Account service unreachable");
        }
        repository.save(payment);
        HttpStatus http = Payment.COMPLETED.equals(payment.getStatus()) ? HttpStatus.CREATED : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(http).body(payment);
    }

    @GetMapping
    public List<Payment> list() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> get(@PathVariable UUID paymentId) {
        return repository.findById(paymentId).map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    private String extractMessage(RestClientResponseException e) {
        try {
            var node = mapper.readTree(e.getResponseBodyAsString());
            if (node.hasNonNull("detail")) return node.get("detail").asText();
            if (node.hasNonNull("message")) return node.get("message").asText();
        } catch (Exception ignored) {
            // fall through to the raw status text
        }
        return e.getStatusCode() + " " + e.getStatusText();
    }

    public record CreatePaymentRequest(
        @NotBlank String fromAccount,
        @NotBlank String toAccount,
        @NotNull @DecimalMin(value="0.01") BigDecimal amount,
        @NotBlank @Size(min=3,max=3) String currency) {}
}
