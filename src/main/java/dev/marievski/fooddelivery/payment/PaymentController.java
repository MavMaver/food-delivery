package dev.marievski.fooddelivery.payment;

import dev.marievski.fooddelivery.payment.dto.PaymentCreateRequest;
import dev.marievski.fooddelivery.payment.dto.PaymentResponse;
import dev.marievski.fooddelivery.payment.dto.PaymentStatusUpdateRequest;
import dev.marievski.fooddelivery.payment.mapper.PaymentMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService service;
    private final PaymentMapper paymentMapper;

    public PaymentController(PaymentService service, PaymentMapper paymentMapper) {
        this.service = service;
        this.paymentMapper = paymentMapper;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentCreateRequest req) {
        PaymentResponse response = service.create(req);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody PaymentStatusUpdateRequest req) {
        PaymentResponse response = service.updateStatus(id, req.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> byOrder(@RequestParam("orderId") Long orderId) {
        List<PaymentResponse> responses = service.findByOrderId(orderId);
        return ResponseEntity.ok(responses);
    }
}