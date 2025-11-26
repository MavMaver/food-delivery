package dev.marievski.fooddelivery.payment;

import dev.marievski.fooddelivery.payment.dto.PaymentCreateRequest;
import dev.marievski.fooddelivery.payment.dto.PaymentResponse;
import dev.marievski.fooddelivery.payment.dto.PaymentStatusUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentCreateRequest req) {
        PaymentResponse p = service.create(req);
        // Тесты ждут 200 OK (не 201)
        return ResponseEntity.ok(p);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody PaymentStatusUpdateRequest req) {
        return ResponseEntity.ok(service.updateStatus(id, req.getStatus()));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> byOrder(@RequestParam("orderId") Long orderId) {
        return ResponseEntity.ok(service.findByOrderId(orderId));
    }
}
