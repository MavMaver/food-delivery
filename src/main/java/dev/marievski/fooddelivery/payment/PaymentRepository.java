package dev.marievski.fooddelivery.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByExternalId(String externalId);
    List<Payment> findByOrderId(Long orderId);
}