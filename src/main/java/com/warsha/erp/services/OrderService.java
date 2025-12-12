package com.warsha.erp.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warsha.erp.dtos.*;
import com.warsha.erp.entities.*;
import com.warsha.erp.repository.OrderRepository;
import com.warsha.erp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final BankTransactionService bankTransactionService;

    @Autowired
    public OrderService(OrderRepository orderRepo, ProductRepository productRepository,
                        CustomerService customerService,
                        ProductService productService,
                        InvoiceService invoiceService,
                        PaymentService paymentService, BankTransactionService bankTransactionService) {
        this.orderRepository = orderRepo;
        this.productRepository = productRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.bankTransactionService = bankTransactionService;
    }
    // ERP Place Order
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Customer customer = customerService.getCustomerByID(request.getCustomerId());

        // prepare basic order info
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDate.now());
        order.setStatus("Pending");
        order.setOrderSource(request.getOrderSource());
        order.setDeliveryCharge(request.getDelivery());
        // todo: you can't let the client send the unit prices
        order.setTotalPrice(request.calculateTotalPrice());
        order.setDiscount(request.getDiscount());
        order.setNotes(request.getNotes());

        List<OrderItems> itemList = new ArrayList<>();

        // loop through items
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

            // ===== 1. Reduce stock =====
            int oldValue = Integer.parseInt(product.getQuantity());
            int newValue = itemReq.getQuantity();
            product.setQuantity(String.valueOf(oldValue - newValue));

            // ===== 2. Increase sold =====
            int soldBefore = product.getSold() != null ? product.getSold() : 0;
            product.setSold(soldBefore + newValue);

            // ===== 3. Save product =====
            productService.updateProduct(itemReq.getProductId(), product);

            itemList.add(item);
        }

        order.setItems(itemList);

        Order savedOrder = orderRepository.save(order);

        // generate invoice for order
        invoiceService.generateInvoice(savedOrder.getId());

        // prepare payment for the order
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setOrderId(savedOrder.getId());
        paymentRequest.setAmountPaid(request.getDownPayment());
        paymentRequest.setPaymentMethod(request.getPaymentMethod());
        paymentRequest.setPaymentStatus("Completed");

        // deposit the down payment to egypt post -5 EGP
        BankTransactionDTO bankTransactionDTO = new BankTransactionDTO();
        bankTransactionDTO.setBankAccountId(request.getBankAccountId());
        bankTransactionDTO.setTransactionType("Deposit");
        bankTransactionDTO.setReferenceType("Order");
        bankTransactionDTO.setReferenceId(savedOrder.getId());
        bankTransactionDTO.setCategoryId(1L);
        bankTransactionDTO.setDescription("A down payment from order #" + savedOrder.getId());
        bankTransactionDTO.setAmount(BigDecimal.valueOf( request.getBankAccountId() == 3L ? request.getDownPayment() - 5: request.getDownPayment()));
        bankTransactionService.createTransaction(bankTransactionDTO);

        paymentService.createPayment(paymentRequest);

        return savedOrder;
    }

    @Transactional
    public Order placeOrder(CreateOrderRequest request) {
        // 1. Fetch Customer
        Customer customer = customerService.getCustomerByID(request.getCustomerId());

        // 2. Extract IDs and Lock Products
        // We fetch all products in ONE query with a LOCK to prevent race conditions
        List<Long> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toList());

        List<Product> products = productRepository.findAllByIdWithLock(productIds);

        // Map for fast lookup: ID -> Product
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductID, Function.identity()));

        // 3. Prepare Order Header
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDate.now());
        order.setStatus("Pending");
        order.setOrderSource(request.getOrderSource());
        order.setDeliveryCharge(request.getDelivery());
        order.setDiscount(request.getDiscount());
        order.setNotes(request.getNotes());

        List<OrderItems> orderItemsList = new ArrayList<>();
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        // 4. Process Items (Validate & Update)
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productMap.get(itemReq.getProductId());

            // A. Existence Check
            if (product == null) {
                throw new IllegalArgumentException("Product ID " + itemReq.getProductId() + " not found.");
            }

            // B. Soft Delete Check (Don't sell deleted items)
            if ("true".equalsIgnoreCase(product.getDeleted()) || product.getDeletedAt() != null) {
                throw new IllegalArgumentException("Product " + product.getName() + " is no longer available.");
            }

            // C. Parse Quantity (String -> Int) safely
            int currentStock = 0;
            try {
                currentStock = Integer.parseInt(product.getQuantity());
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Data Error: Product " + product.getName() + " has invalid stock format.");
            }

            // D. Stock Check
            if (currentStock < itemReq.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for: " + product.getName());
            }

            // E. Secure Price Calculation (Use DB Price, convert Double to BigDecimal)
            // We use BigDecimal for money math to avoid floating point errors (e.g. 0.1 + 0.2 = 0.3000004)
            BigDecimal unitPrice = BigDecimal.valueOf(product.getSellingPrice());
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            calculatedTotal = calculatedTotal.add(lineTotal);

            // F. Update Product State (In Memory)
            // Reduce Stock
            product.setQuantity(String.valueOf(currentStock - itemReq.getQuantity()));

            // Increase Sold Count (Handle null safety)
            int currentSold = product.getSold() == null ? 0 : product.getSold();
            product.setSold(currentSold + itemReq.getQuantity());
            product.setUpdatedAt(LocalDateTime.now());

            // G. Create Order Item
            OrderItems item = new OrderItems();
            item.setOrder(order);
            item.setProduct(product);
            item.setUnitPrice(product.getSellingPrice()); // Store as Double as per your entity
            item.setQuantity(itemReq.getQuantity());

            orderItemsList.add(item);
        }

        // 5. Save Bulk Updates (Products)
        productRepository.saveAll(products);

        // 6. Finalize Order
        // Calculate final total: (Sum of Items + Delivery) - Discount
        BigDecimal finalTotal = calculatedTotal.add(BigDecimal.valueOf(request.getDelivery()));
        finalTotal = finalTotal.subtract(BigDecimal.valueOf(request.getDiscount()));

        order.setTotalPrice(finalTotal.doubleValue());
        order.setItems(orderItemsList);

        Order savedOrder = orderRepository.save(order);

        // 7. Handle Integrations (Invoice / Payment)
        // Extracted to keep the main logic clean
        processPostOrderIntegrations(request, savedOrder);

        return savedOrder;
    }

    private void processPostOrderIntegrations(CreateOrderRequest request, Order savedOrder) {
        // Generate Invoice
        invoiceService.generateInvoice(savedOrder.getId());

        // Egypt Post Fee Logic
        // Defining constants makes logic readable and easy to change later
        final long EGYPT_POST_BANK_ID = 3L;
        final BigDecimal POST_FEE = BigDecimal.valueOf(5);

        BigDecimal depositAmount = BigDecimal.valueOf(request.getDownPayment());

        if (request.getBankAccountId() != null && request.getBankAccountId() == EGYPT_POST_BANK_ID) {
            depositAmount = depositAmount.subtract(POST_FEE);
        }

        // Bank Transaction
        BankTransactionDTO bankTx = new BankTransactionDTO();
        bankTx.setBankAccountId(request.getBankAccountId());
        bankTx.setTransactionType("Deposit");
        bankTx.setReferenceType("Order");
        bankTx.setReferenceId(savedOrder.getId());
        bankTx.setCategoryId(1L);
        bankTx.setDescription("Down payment for order #" + savedOrder.getId());
        bankTx.setAmount(depositAmount);
        bankTransactionService.createTransaction(bankTx);

        // Payment Record
        CreatePaymentRequest payReq = new CreatePaymentRequest();
        payReq.setOrderId(savedOrder.getId());
        payReq.setAmountPaid(request.getDownPayment()); // User pays full amount
        payReq.setPaymentMethod(request.getPaymentMethod());
        payReq.setPaymentStatus("Completed");
        paymentService.createPayment(payReq);
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
    public Order updateOrderStatus(Long orderId, String status, Long bankAccountId) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        existingOrder.setStatus(status);

        // create transaction when order completed
        if (status.equals("Completed")){
            BankTransactionDTO bankTransactionDTO = new BankTransactionDTO();
            bankTransactionDTO.setBankAccountId(bankAccountId);
            bankTransactionDTO.setTransactionType("Deposit");
            bankTransactionDTO.setReferenceType("Order");
            bankTransactionDTO.setReferenceId(orderId);
            bankTransactionDTO.setCategoryId(1L);
            bankTransactionDTO.setDescription("Order #" + orderId + " Completed");
            bankTransactionDTO.setAmount(BigDecimal.valueOf((existingOrder.getTotalPrice())));
            bankTransactionService.createTransaction(bankTransactionDTO);
        }

        return orderRepository.save(existingOrder);
    }

    public List<OrderCountByGovernorateDto> getOrderCountsByGovernorate() {
        return orderRepository.countOrdersByGovernorate();
    }

    public List<OrderResponse> getAllOrders(LocalDate userStart, LocalDate userEnd) {

        LocalDate startDateTime;
        LocalDateTime endDateTime;

        // 1. Determine the Date Range
        if (userStart != null && userEnd != null) {
            // CASE A: User provided specific dates
            startDateTime = LocalDate.from(userStart.atStartOfDay());
            // We go to the very end of the end date (e.g., 23:59:59)
            endDateTime = userEnd.atTime(LocalTime.MAX);
        } else {
            // CASE B: No dates provided, default to CURRENT MONTH
            startDateTime = LocalDate.from(YearMonth.now().atDay(1).atStartOfDay());
            // Start of next month (exclusive) covers all current month
            endDateTime = YearMonth.now().plusMonths(1).atDay(1).atStartOfDay();
        }

        // 2. Fetch from Repo
        List<Order> orders = orderRepository.findByOrderDateBetween(
                startDateTime,
                LocalDate.from(endDateTime),
                Sort.by(Sort.Direction.DESC, "id")
        );

        // 3. Map to DTO (Your original logic)
        return orders.stream().map(order -> {
            // WARNING: This line causes an "N+1" performance issue (see note below)
            List<PaymentDto> paymentDTOs = paymentService.getPaymentsByOrder(order.getId());

            OrderResponse dto = new OrderResponse();

            dto.setOrderId(order.getId());
            dto.setNotes(order.getNotes());

            // Safety check in case an order has no payments yet to avoid IndexOutOfBoundsException
            if (!paymentDTOs.isEmpty()) {
                dto.setPaymentMethod(paymentDTOs.getFirst().getPaymentMethod());
                dto.setDownPayment(paymentDTOs.getFirst().getAmountPaid());
            }

            dto.setOrderDate(order.getOrderDate());
            dto.setStatus(order.getStatus());
            dto.setOrderSource(order.getOrderSource());
            dto.setDiscount(order.getDiscount());
            dto.setDelivery(order.getDeliveryCharge());
            dto.setTotalPrice(BigDecimal.valueOf(order.getTotalPrice()));

            // Map customer
            Customer customer = order.getCustomer();
            if (customer != null) {
                CustomerDto customerDTO = new CustomerDto();
                customerDTO.setCustomerId(customer.getId());
                customerDTO.setFullName(customer.getFullName());
                customerDTO.setGovernorate(customer.getGovernorate());
                customerDTO.setSecondaryPhone(customer.getSecondaryPhone());
                customerDTO.setCity(customer.getCity());
                customerDTO.setPhone(customer.getPhone());
                customerDTO.setAddress(customer.getAddress());
                dto.setCustomer(customerDTO);
            }

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

    @Transactional
    public void cancelOrder(Long orderId) {
        // 1. Fetch order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Good Practice: Check if already cancelled
        if ("Cancelled".equals(order.getStatus())) {
            // Or use an enum: OrderStatus.CANCELLED.equals(order.getStatus())
            throw new IllegalStateException("Order " + orderId + " is already cancelled.");
        }

        // 2. Restock products
        // This logic is correct and should remain.
        // If an order is cancelled, items go back in stock.
        for (OrderItems item : order.getItems()) {
            Product product = item.getProduct();
            int currentStock = Integer.parseInt(product.getQuantity());
            int restoredQty = currentStock + item.getQuantity();
            product.setQuantity(String.valueOf(restoredQty));

            productService.updateProduct(product.getProductID(), product);
        }

        // 3. Soft-delete related payments (Update status)
        // We replace the delete method with a new 'cancel' method.
        paymentService.cancelPaymentsByOrderId(orderId);

        // 4. Handle related invoice (Remove deletion)
        // We NO LONGER delete the invoice.
        // It remains in the system, linked to a "Cancelled" order.
        // This preserves the historical record.

        // 5. Finally, soft-delete the order (Update status)
        // Instead of deleting, we set its status and save.
        order.setStatus("Cancelled"); // Use "Cancelled", "Deleted", or an enum

        // Your 'Orders' table has an 'UpdatedAt' column.
        // If you're using @UpdateTimestamp, Spring Data JPA handles this automatically.
        // If not, set it manually: order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }
}
