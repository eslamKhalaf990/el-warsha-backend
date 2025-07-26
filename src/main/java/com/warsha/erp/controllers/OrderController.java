package com.warsha.erp.controllers;

import com.warsha.erp.dtos.CreateOrderRequest;
import com.warsha.erp.dtos.OrderResponse;
import com.warsha.erp.entities.Order;
import com.warsha.erp.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@CrossOrigin // optional: allows cross-origin requests (good for Flutter)
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        Order newOrder = orderService.createOrder(request);
        return ResponseEntity.ok(newOrder);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}
