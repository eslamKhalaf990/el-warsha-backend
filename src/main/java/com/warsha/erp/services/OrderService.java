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
    private final PaymentService paymentService;

    @Autowired
    public OrderService(OrderRepository orderRepo,
                        CustomerService customerService,
                        ProductService productService, InvoiceService invoiceService, PaymentService paymentService) {
        this.orderRepository = orderRepo;
        this.customerService = customerService;
        this.productService = productService;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Customer customer = customerService.getCustomerByID(request.getCustomerId());

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDate.now());
        order.setStatus("Pending");

        order.setOrderSource(request.getOrderSource());
        order.setDeliveryCharge(request.getDelivery());
        order.setTotalPrice(request.calculateTotalPrice());
        order.setDiscount(request.getDiscount());

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
            int oldValue = Integer.parseInt(product.getQuantity());
            int newValue = itemReq.getQuantity();

            product.setQuantity(String.valueOf((oldValue - newValue)));

            productService.updateProduct(itemReq.getProductId(), product);

            itemList.add(item);
        }

        order.setItems(itemList);

        Order savedOrder = orderRepository.save(order);

        invoiceService.generateInvoice(savedOrder.getId());

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();

        paymentRequest.setOrderId(savedOrder.getId());
        paymentRequest.setAmountPaid(request.getDownPayment());
        paymentRequest.setPaymentMethod(request.getPaymentMethod());
        paymentRequest.setPaymentStatus("Completed");

        paymentService.createPayment(paymentRequest);

        return savedOrder;
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream().map(order -> {
            List<PaymentDto> paymentDtos = paymentService.getPaymentsByOrder(order.getId());

            OrderResponse dto = new OrderResponse();

            dto.setOrderId(order.getId());

            dto.setPaymentMethod(paymentDtos.get(0).getPaymentMethod());

            dto.setOrderDate(order.getOrderDate());
            dto.setStatus(order.getStatus());

            dto.setOrderSource(order.getOrderSource());
            dto.setDownPayment(paymentDtos.get(0).getAmountPaid());
            dto.setDiscount(order.getDiscount());
            dto.setDelivery(order.getDeliveryCharge());
            dto.setTotalPrice(order.getTotalPrice());

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
