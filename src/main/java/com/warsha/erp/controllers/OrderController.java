package com.warsha.erp.controllers;

import com.warsha.erp.config.CustomerUserDetails;
import com.warsha.erp.dtos.CreateOrderRequest;
import com.warsha.erp.dtos.OrderCountByGovernorateDto;
import com.warsha.erp.dtos.OrderResponse;
import com.warsha.erp.dtos.UpdateOrderStatus;
import com.warsha.erp.entities.Order;
import com.warsha.erp.services.OrderService;
import io.jsonwebtoken.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
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

    @PostMapping(value = "/placeOrder", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OrderResponse> placeOrder(
            // 1. Receive the JSON data
            @RequestPart("order") CreateOrderRequest request,

            // 2. Receive the Files (Optional)
            @RequestPart(value = "images", required = false) List<MultipartFile> images,

            @AuthenticationPrincipal CustomerUserDetails userDetails) {

        Long customerId = userDetails.getId();

        // 3. Pass images to the service
        Order order = orderService.placeOrder(request, customerId, images);

        return ResponseEntity.ok(OrderResponse.fromEntity(order));
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

    @GetMapping("/customer")
    public ResponseEntity<List<OrderResponse>> getAllOrdersByCustomerId(
            @AuthenticationPrincipal CustomerUserDetails userDetails
    ) {
        Long customerId = userDetails.getId();
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
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
