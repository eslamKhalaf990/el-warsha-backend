package com.warsha.erp.services;

import com.warsha.erp.dtos.*;
import com.warsha.erp.entities.*;
import com.warsha.erp.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final BankTransactionService bankTransactionService;

    @Autowired
    public OrderService(OrderRepository orderRepo,
                        CustomerService customerService,
                        ProductService productService,
                        InvoiceService invoiceService,
                        PaymentService paymentService, BankTransactionService bankTransactionService) {
        this.orderRepository = orderRepo;
        this.customerService = customerService;
        this.productService = productService;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.bankTransactionService = bankTransactionService;
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
        order.setNotes(request.getNotes());

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

    @Transactional
    public Order updateOrder(Long orderId, CreateOrderRequest request) {
        // 1. Fetch existing order
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Update basic order info
        existingOrder.setOrderSource(request.getOrderSource());
        existingOrder.setCustomer(customerService.getCustomerByID(request.getCustomerId()));
        existingOrder.setDeliveryCharge(request.getDelivery());
        existingOrder.setDiscount(request.getDiscount());
        existingOrder.setNotes(request.getNotes());
        existingOrder.setTotalPrice(request.calculateTotalPrice());

        // 3. Revert previous product quantities
        for (OrderItems item : existingOrder.getItems()) {
            Product product = item.getProduct();
            int restoredQty = Integer.parseInt(product.getQuantity()) + item.getQuantity();
            product.setQuantity(String.valueOf(restoredQty));
            productService.updateProduct(product.getProductID(), product);
        }

        // 4. Update items
        existingOrder.getItems().clear(); // keep the reference
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productService.getProductById(itemReq.getProductId());

            // Deduct new quantities
            int oldValue = Integer.parseInt(product.getQuantity());
            int newValue = itemReq.getQuantity();
            if (oldValue < newValue) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            product.setQuantity(String.valueOf(oldValue - newValue));
            productService.updateProduct(product.getProductID(), product);

            // Create new order item
            OrderItems item = new OrderItems();
            item.setOrder(existingOrder);
            item.setProduct(product);
            item.setQuantity(newValue);
            item.setUnitPrice(itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : product.getSellingPrice());

            existingOrder.getItems().add(item);
        }

        // 5. Save order before dependent entities (important!)
        Order savedOrder = orderRepository.save(existingOrder);

        // 6. Handle invoice updates
        invoiceService.regenerateInvoice(savedOrder.getId());

        // clean up old payments
        paymentService.deletePaymentsByOrderId(savedOrder.getId());

        // create new payment from updated request
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setOrderId(savedOrder.getId());
        paymentRequest.setAmountPaid(request.getDownPayment());
        paymentRequest.setPaymentMethod(request.getPaymentMethod());
        paymentRequest.setPaymentStatus("Completed"); // or based on logic

        paymentService.createPayment(paymentRequest);

        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        existingOrder.setStatus(status);
        // create transaction when order completed
        if (status.equals("Completed")){
            BankTransactionDTO bankTransactionDTO = new BankTransactionDTO();
            bankTransactionDTO.setBankAccountId(3L);
            bankTransactionDTO.setTransactionType("Deposit");
            bankTransactionDTO.setReferenceType("Order");
            bankTransactionDTO.setReferenceId(orderId);
            bankTransactionDTO.setCategoryId(1L);
            bankTransactionDTO.setDescription("Order #" + orderId + " Completed");
            bankTransactionDTO.setAmount(BigDecimal.valueOf(existingOrder.getTotalPrice() - 5));
            bankTransactionService.createTransaction(bankTransactionDTO);
        }

        return orderRepository.save(existingOrder);
    }

    public List<OrderCountByGovernorateDto> getOrderCountsByGovernorate() {
        return orderRepository.countOrdersByGovernorate();
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        return orders.stream().map(order -> {
            List<PaymentDto> paymentDTOs = paymentService.getPaymentsByOrder(order.getId());

            OrderResponse dto = new OrderResponse();

            dto.setOrderId(order.getId());
            dto.setNotes(order.getNotes());

            dto.setPaymentMethod(paymentDTOs.getFirst().getPaymentMethod());

            dto.setOrderDate(order.getOrderDate());
            dto.setStatus(order.getStatus());

            dto.setOrderSource(order.getOrderSource());
            dto.setDownPayment(paymentDTOs.getFirst().getAmountPaid());
            dto.setDiscount(order.getDiscount());
            dto.setDelivery(order.getDeliveryCharge());
            dto.setTotalPrice(order.getTotalPrice());

            // Map customer
            Customer customer = order.getCustomer();
            CustomerDto customerDTO = new CustomerDto();
            customerDTO.setCustomerId(customer.getId());
            customerDTO.setFullName(customer.getFullName());
            customerDTO.setGovernorate(customer.getGovernorate());
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

    @Transactional
    public void deleteOrder(Long orderId) {
        // 1. Fetch order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Restock products
        for (OrderItems item : order.getItems()) {
            Product product = item.getProduct();
            int currentStock = Integer.parseInt(product.getQuantity());
            int restoredQty = currentStock + item.getQuantity();
            product.setQuantity(String.valueOf(restoredQty));

            productService.updateProduct(product.getProductID(), product);
        }

        // 3. Delete related payments
        paymentService.deletePaymentsByOrderId(orderId);

        // 4. Delete related invoice
        invoiceService.deleteInvoiceByOrderId(orderId);

        // 5. Finally, delete the order
        orderRepository.delete(order);
    }
}
