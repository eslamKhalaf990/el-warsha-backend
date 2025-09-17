package com.warsha.erp.dtos;

import com.warsha.erp.entities.Customer;
import com.warsha.erp.entities.Order;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class OrderResponse {
    private Long orderId;
    private String status;

    private double discount;
    private double delivery;
    private double totalPrice;
    private double downPayment;
    private String orderSource;
    private String paymentMethod;

    private LocalDate orderDate;
    private CustomerDto customer;
    private List<OrderItemDto> orderItems;

    public static OrderResponse fromEntity(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.setOrderId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setOrderSource(order.getOrderSource());
        dto.setDelivery(order.getDeliveryCharge());
        dto.setDiscount(order.getDiscount());
        dto.setTotalPrice(order.getTotalPrice());

        // Map customer
        Customer customer = order.getCustomer();
        CustomerDto customerDTO = new CustomerDto();
        customerDTO.setCustomerId(customer.getId());
        customerDTO.setFullName(customer.getFullName());
        customerDTO.setEmail(customer.getEmail());
        customerDTO.setPhone(customer.getPhone());
        customerDTO.setAddress(customer.getAddress());
        dto.setCustomer(customerDTO);

        // Map order items
        List<OrderItemDto> itemDTOs = order.getItems().stream().map(item -> {
            return new OrderItemDto(
                    item.getProduct().getProductID(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitPrice()
            );
        }).collect(Collectors.toList());

        dto.setOrderItems(itemDTOs);
        return dto;
    }
}