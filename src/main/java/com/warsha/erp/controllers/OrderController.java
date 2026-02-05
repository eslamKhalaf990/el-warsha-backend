package com.warsha.erp.controllers;

import com.warsha.erp.config.CustomerUserDetails;
import com.warsha.erp.dtos.CreateOrderRequest;
import com.warsha.erp.dtos.OrderCountByGovernorateDto;
import com.warsha.erp.dtos.OrderResponse;
import com.warsha.erp.dtos.UpdateOrderStatus;
import com.warsha.erp.entities.Order;
import com.warsha.erp.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final DateTimeFormatter logFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(logFormat);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        System.out.println("[" + getTimestamp() + "] INFO: ERP Admin creating order for Customer ID: " + request.getCustomerId());
        Order newOrder = orderService.createOrder(request);
        return ResponseEntity.ok(OrderResponse.fromEntity(newOrder));
    }

    @PostMapping(value = "/placeOrder", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestPart("order") CreateOrderRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomerUserDetails userDetails) {

        Long customerId = userDetails.getId();
        int imageCount = (images != null) ? images.size() : 0;

        System.out.println("[" + getTimestamp() + "] INFO: E-Commerce order attempt by Customer ID: " + customerId + " (" + imageCount + " images)");

        Order order = orderService.placeOrder(request, customerId, images);
        System.out.println("[" + getTimestamp() + "] SUCCESS: Order placed successfully. Order ID: " + order.getId());

        return ResponseEntity.ok(OrderResponse.fromEntity(order));
    }

    @GetMapping("/countGovernorates")
    public List<OrderCountByGovernorateDto> getOrderCountsByGovernorate() {
        System.out.println("[" + getTimestamp() + "] INFO: Fetching order stats by governorate");
        return orderService.getOrderCountsByGovernorate();
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id, @RequestBody CreateOrderRequest request) {
        System.out.println("[" + getTimestamp() + "] WARN: Updating Order ID: " + id);
        Order updatedOrder = orderService.updateOrder(id, request);
        return ResponseEntity.ok(OrderResponse.fromEntity(updatedOrder));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatus request) {

        System.out.println("[" + getTimestamp() + "] INFO: Status update for Order #" + id + " to " + request.getStatus());
        Order updatedOrder = orderService.updateOrderStatus(id, request.getStatus(), request.getBankAccountId());
        return ResponseEntity.ok(OrderResponse.fromEntity(updatedOrder));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        System.out.println("[" + getTimestamp() + "] INFO: Fetching orders from " + startDate + " to " + endDate);
        return ResponseEntity.ok(orderService.getAllOrders(startDate, endDate));
    }

    @GetMapping("/customer")
    public ResponseEntity<List<OrderResponse>> getAllOrdersByCustomerId(
            @AuthenticationPrincipal CustomerUserDetails userDetails
    ) {
        Long customerId = userDetails.getId();
        System.out.println("[" + getTimestamp() + "] INFO: Customer ID " + customerId + " viewing their order history");
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        System.out.println("[" + getTimestamp() + "] ALERT: Deleting Order ID: " + id);
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        System.out.println("[" + getTimestamp() + "] WARN: Cancelling Order ID: " + id);
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}