package com.warsha.erp.controllers;

import com.warsha.erp.dtos.CreateOrderRequest;
import com.warsha.erp.dtos.OrderCountByGovernorateDto;
import com.warsha.erp.dtos.OrderResponse;
import com.warsha.erp.dtos.UpdateOrderStatus;
import com.warsha.erp.entities.Order;
import com.warsha.erp.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        Order newOrder = orderService.createOrder(request);
        return ResponseEntity.ok(OrderResponse.fromEntity(newOrder));
    }

    @PostMapping("/placeOrder")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody CreateOrderRequest request) {
        Order newOrder = orderService.placeOrder(request);
        return ResponseEntity.ok(OrderResponse.fromEntity(newOrder));
    }

    @GetMapping("/countGovernorates")
    public List<OrderCountByGovernorateDto> getOrderCountsByGovernorate() {
        return orderService.getOrderCountsByGovernorate();
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id, @RequestBody CreateOrderRequest request) {
        Order updatedOrder = orderService.updateOrder(id, request);
        return ResponseEntity.ok(OrderResponse.fromEntity(updatedOrder));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatus request) {

        Order updatedOrder = orderService.updateOrderStatus(id, request.getStatus(), request.getBankAccountId());
        return ResponseEntity.ok(OrderResponse.fromEntity(updatedOrder));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(orderService.getAllOrders(startDate, endDate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
