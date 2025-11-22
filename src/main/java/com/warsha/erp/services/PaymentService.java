package com.warsha.erp.services;

import com.warsha.erp.dtos.CreatePaymentRequest;
import com.warsha.erp.dtos.PaymentDto;
import com.warsha.erp.entities.Order;
import com.warsha.erp.entities.Payment;
import com.warsha.erp.repository.OrderRepository;
import com.warsha.erp.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;

    public PaymentService(PaymentRepository paymentRepo, OrderRepository orderRepo) {
        this.paymentRepo = paymentRepo;
        this.orderRepo = orderRepo;
    }

    public PaymentDto createPayment(CreatePaymentRequest request) {
        Order order = orderRepo.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmountPaid(request.getAmountPaid());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : "Unpaid");
        payment.setPaymentDate(LocalDateTime.now());

        paymentRepo.save(payment);
        return new PaymentDto(payment);
    }

    public List<PaymentDto> getAllPayments() {
        return paymentRepo.findAll().stream()
                .map(PaymentDto::new)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getPaymentsByOrder(Long orderId) {
        return paymentRepo.findByOrderId(orderId).stream()
                .map(PaymentDto::new)
                .collect(Collectors.toList());
    }

    public void deletePaymentsByOrderId(Long orderId) {
        List<Payment> payments = paymentRepo.findByOrderId(orderId);

        if (payments != null && !payments.isEmpty()) {
            paymentRepo.deleteAll(payments);
        }
    }

    public void cancelPaymentsByOrderId(Long orderId) {

        List<Payment> payments = paymentRepo.findByOrderId(orderId); // Assuming you have this method

        for (Payment payment : payments) {

            payment.setPaymentStatus("Cancelled");
            paymentRepo.save(payment);
        }
    }
}
