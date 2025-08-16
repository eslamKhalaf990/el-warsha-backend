package com.warsha.erp.services;

import com.warsha.erp.dtos.*;
import com.warsha.erp.entities.Customer;
import com.warsha.erp.entities.Order;
import com.warsha.erp.entities.OrderItems;
import com.warsha.erp.entities.Product;
import com.warsha.erp.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final InvoiceService invoiceService;

    @Autowired
    public OrderService(OrderRepository orderRepo,
                        CustomerService customerService,
                        ProductService productService, InvoiceService invoiceService) {
        this.orderRepository = orderRepo;
        this.customerService = customerService;
        this.productService = productService;
        this.invoiceService = invoiceService;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Customer customer = customerService.getCustomerByID(request.getCustomerId());

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDate.now());
        order.setStatus("Pending");

        List<OrderItems> itemList = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productService.getProductById(itemReq.getProductId());

            OrderItems item = new OrderItems();
            item.setOrder(order);
            item.setProduct(product);

            if(itemReq.getUnitPrice() == null)
                item.setUnitPrice(product.getSellingPrice());
            else
                item.setUnitPrice(itemReq.getUnitPrice());

            item.setQuantity(itemReq.getQuantity());

            itemList.add(item);
        }

        order.setItems(itemList);

        Order savedOrder = orderRepository.save(order);

        invoiceService.generateInvoice(savedOrder.getId());

        return savedOrder;
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream().map(order -> {
            OrderResponse dto = new OrderResponse();
            dto.setOrderId(order.getId());
            dto.setOrderDate(order.getOrderDate());
            dto.setStatus(order.getStatus());

            // Map customer
            Customer customer = order.getCustomer();
            CustomerDto customerDTO = new CustomerDto();
            customerDTO.setCustomerId(customer.getCustomerID());
            customerDTO.setFullName(customer.getFullName());
            customerDTO.setEmail(customer.getEmail());
            customerDTO.setPhone(customer.getPhone());
            customerDTO.setAddress(customer.getAddress());
            dto.setCustomer(customerDTO);

            // Map order items
            List<OrderItemDto> itemDTOs = order.getItems().stream().map(item -> {
                return new OrderItemDto(item.getProduct().getProductID(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                );
            }).collect(Collectors.toList());

            dto.setOrderItems(itemDTOs);

            return dto;
        }).collect(Collectors.toList());
    }
}
