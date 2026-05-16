import json
import re

controller_data = """
src/main/java/com/warsha/erp/controllers/AuthController.java:23:@RequestMapping("/auth")
src/main/java/com/warsha/erp/controllers/AuthController.java:41:    @PostMapping("/login")
src/main/java/com/warsha/erp/controllers/AuthController.java-42-    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
--
src/main/java/com/warsha/erp/controllers/AuthController.java:61:    @PostMapping("/customerLogin")
src/main/java/com/warsha/erp/controllers/AuthController.java-62-    public ResponseEntity<?> customerLogin(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
--
src/main/java/com/warsha/erp/controllers/AuthController.java:105:    @PostMapping("/verifyCode")
src/main/java/com/warsha/erp/controllers/AuthController.java-106-    public ResponseEntity<?> verifyCode(@RequestBody VerifyRequest request) {
--
src/main/java/com/warsha/erp/controllers/AuthController.java:118:    @PostMapping("/changePassword")
src/main/java/com/warsha/erp/controllers/AuthController.java-119-    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
--
src/main/java/com/warsha/erp/controllers/AuthController.java:131:    @PostMapping("/forgotPassword")
src/main/java/com/warsha/erp/controllers/AuthController.java-132-    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
--
src/main/java/com/warsha/erp/controllers/AuthController.java:144:    @PostMapping("/resetPassword")
src/main/java/com/warsha/erp/controllers/AuthController.java-145-    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
--
src/main/java/com/warsha/erp/controllers/AuthController.java:157:    @PostMapping("/register")
src/main/java/com/warsha/erp/controllers/AuthController.java-158-    public User register(@RequestBody User user) {
--
src/main/java/com/warsha/erp/controllers/BankTransactionController.java:16:@RequestMapping("/api/bank")
src/main/java/com/warsha/erp/controllers/BankTransactionController.java:22:    @PostMapping("/transaction")
src/main/java/com/warsha/erp/controllers/BankTransactionController.java-23-    public ResponseEntity<BankTransaction> addTransaction(@RequestBody BankTransactionDTO dto) {
--
src/main/java/com/warsha/erp/controllers/BankTransactionController.java:28:    @GetMapping("/transactions/{accountId}")
src/main/java/com/warsha/erp/controllers/BankTransactionController.java-29-    public ResponseEntity<List<BankTransaction>> getTransactions(@PathVariable Long accountId) {
--
src/main/java/com/warsha/erp/controllers/BankTransactionController.java:33:    @GetMapping("/transactions")
src/main/java/com/warsha/erp/controllers/BankTransactionController.java-34-    public ResponseEntity<List<BankTransaction>> getTransactions() {
--
src/main/java/com/warsha/erp/controllers/BankTransactionController.java:38:    @GetMapping("/balance/{accountId}")
src/main/java/com/warsha/erp/controllers/BankTransactionController.java-39-    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long accountId) {
--
src/main/java/com/warsha/erp/controllers/BankTransactionController.java:43:    @GetMapping("/summary")
src/main/java/com/warsha/erp/controllers/BankTransactionController.java-44-    public ResponseEntity<BankSummaryDTO> getSummary() {
--
src/main/java/com/warsha/erp/controllers/BankTransactionController.java:49:    @GetMapping("/accounts")
src/main/java/com/warsha/erp/controllers/BankTransactionController.java-50-    public ResponseEntity<List<BankAccountDTO>> getAllAccounts(
--
src/main/java/com/warsha/erp/controllers/BankTransactionController.java:60:    @GetMapping("/transactionCategories")
src/main/java/com/warsha/erp/controllers/BankTransactionController.java-61-    public ResponseEntity<List<TransactionCategoryDTO>> getTransactionCategories() {
--
src/main/java/com/warsha/erp/controllers/BankTransactionController.java:66:    @DeleteMapping("/resetTransactions")
src/main/java/com/warsha/erp/controllers/BankTransactionController.java-67-    public ResponseEntity<String> resetSystem() {
--
src/main/java/com/warsha/erp/controllers/CashFlowController.java:15:@RequestMapping("/cashFlow")
src/main/java/com/warsha/erp/controllers/CashFlowController.java:26:    @GetMapping("/daily")
src/main/java/com/warsha/erp/controllers/CashFlowController.java-27-    public List<DailyCashFlowDto> getDailyCashFlow() {
--
src/main/java/com/warsha/erp/controllers/CashFlowController.java:31:    @GetMapping("/revenueSummary")
src/main/java/com/warsha/erp/controllers/CashFlowController.java-32-    public RevenueSummaryDto getRevenueSummary() {
--
src/main/java/com/warsha/erp/controllers/CashFlowController.java:36:    @GetMapping("/topSoldProducts")
src/main/java/com/warsha/erp/controllers/CashFlowController.java-37-    public List<TopProductDTO> getTop5SoldProductsForMonth(
--
src/main/java/com/warsha/erp/controllers/CashFlowController.java:46:    @GetMapping("/analysis/customers/loyalty")
src/main/java/com/warsha/erp/controllers/CashFlowController.java-47-    public List<CustomerLoyaltyDto> getCustomerLoyaltyReport() {
--
src/main/java/com/warsha/erp/controllers/CashFlowController.java:51:    @GetMapping("/analysis/customers/vip")
src/main/java/com/warsha/erp/controllers/CashFlowController.java-52-    public List<VipCustomerDto> getTopVipCustomers() {
--
src/main/java/com/warsha/erp/controllers/CashFlowController.java:57:    @GetMapping("/analysis/customers/at-risk")
src/main/java/com/warsha/erp/controllers/CashFlowController.java-58-    public List<AtRiskCustomerDto> getAtRiskCustomers() {
--
src/main/java/com/warsha/erp/controllers/CashFlowController.java:63:    @GetMapping("/analysis/revenue-by-source")
src/main/java/com/warsha/erp/controllers/CashFlowController.java-64-    public List<OrderSourceDto> getRevenueBySource() {
--
src/main/java/com/warsha/erp/controllers/CashFlowController.java:69:    @GetMapping("/analysis/discount-seekers")
src/main/java/com/warsha/erp/controllers/CashFlowController.java-70-    public List<DiscountSeekerDto> getDiscountSeekers() {
--
src/main/java/com/warsha/erp/controllers/CashFlowController.java:75:    @GetMapping("/analysis/products/top-performers")
src/main/java/com/warsha/erp/controllers/CashFlowController.java-76-    public List<ProductPerformanceDto> getTop20Products() {
--
src/main/java/com/warsha/erp/controllers/CashFlowController.java:81:    @GetMapping("/analysis/governorate-performance")
src/main/java/com/warsha/erp/controllers/CashFlowController.java-82-    public List<RegionalPerformanceDto> getPerformanceByGovernorate() {
--
src/main/java/com/warsha/erp/controllers/CashFlowController.java:87:    @GetMapping("/analysis/kpi/average-basket-size")
src/main/java/com/warsha/erp/controllers/CashFlowController.java-88-    public Double getAverageBasketSize() {
--
src/main/java/com/warsha/erp/controllers/CashFlowController.java:93:    @GetMapping("/analysis/daily-revenue-report")
src/main/java/com/warsha/erp/controllers/CashFlowController.java-94-    public List<DailyRevenueReportDto> getDailyRevenueReport() {
--
src/main/java/com/warsha/erp/controllers/CategoryController.java:12:@RequestMapping("/category")
src/main/java/com/warsha/erp/controllers/CategoryController.java:17:    @GetMapping
src/main/java/com/warsha/erp/controllers/CategoryController.java-18-    public List<Category> getAll() {
--
src/main/java/com/warsha/erp/controllers/CategoryController.java:22:    @PostMapping
src/main/java/com/warsha/erp/controllers/CategoryController.java-23-    public Category create(@RequestBody Category category) {
--
src/main/java/com/warsha/erp/controllers/CategoryController.java:27:    @DeleteMapping("/{id}")
src/main/java/com/warsha/erp/controllers/CategoryController.java-28-    public void delete(@PathVariable Long id) {
--
src/main/java/com/warsha/erp/controllers/CategoryController.java:32:    @PutMapping("/{id}")
src/main/java/com/warsha/erp/controllers/CategoryController.java-33-    public Category update(@PathVariable Long id, @RequestBody Category category) {
--
src/main/java/com/warsha/erp/controllers/CustomerController.java:18:@RequestMapping("/customers")
src/main/java/com/warsha/erp/controllers/CustomerController.java:37:    @GetMapping
src/main/java/com/warsha/erp/controllers/CustomerController.java-38-    public List<Customer> getAll() {
--
src/main/java/com/warsha/erp/controllers/CustomerController.java:43:    @GetMapping("/countsByGovernorate")
src/main/java/com/warsha/erp/controllers/CustomerController.java-44-    public List<CustomerCountByGovernorate> getCountsByGovernorate() {
--
src/main/java/com/warsha/erp/controllers/CustomerController.java:49:    @GetMapping("/{id}")
src/main/java/com/warsha/erp/controllers/CustomerController.java-50-    public ResponseEntity<Customer> getById(@PathVariable Long id) {
--
src/main/java/com/warsha/erp/controllers/CustomerController.java:55:    @PostMapping
src/main/java/com/warsha/erp/controllers/CustomerController.java-56-    public ResponseEntity<Customer> create(@RequestBody Customer customer) {
--
src/main/java/com/warsha/erp/controllers/CustomerController.java:61:    @PostMapping("/customerSignUp")
src/main/java/com/warsha/erp/controllers/CustomerController.java-62-    public ResponseEntity<?> customerSignUp(@RequestBody Customer customer) {
--
src/main/java/com/warsha/erp/controllers/CustomerController.java:78:    @PutMapping("/{id}")
src/main/java/com/warsha/erp/controllers/CustomerController.java-79-    public ResponseEntity<Customer> update(@PathVariable Long id, @RequestBody Customer customer) {
--
src/main/java/com/warsha/erp/controllers/CustomerController.java:84:    @DeleteMapping("/{id}")
src/main/java/com/warsha/erp/controllers/CustomerController.java-85-    public ResponseEntity<Void> delete(@PathVariable Long id) {
--
src/main/java/com/warsha/erp/controllers/CustomerController.java:91:    @DeleteMapping
src/main/java/com/warsha/erp/controllers/CustomerController.java-92-    public ResponseEntity<Void> deleteAll() {
--
src/main/java/com/warsha/erp/controllers/CustomerController.java:98:    @PostMapping("/resend-otp")
src/main/java/com/warsha/erp/controllers/CustomerController.java-99-    public ResponseEntity<String> resendOtp(@RequestParam String email) {
--
src/main/java/com/warsha/erp/controllers/FileController.java:15:@RequestMapping("/api/files")
src/main/java/com/warsha/erp/controllers/FileController.java:24:    @GetMapping("/{fileId}")
src/main/java/com/warsha/erp/controllers/FileController.java-25-    public ResponseEntity<byte[]> getFile(@PathVariable String fileId) throws Exception {
--
src/main/java/com/warsha/erp/controllers/InvoiceController.java:14:@RequestMapping("/invoice")
src/main/java/com/warsha/erp/controllers/InvoiceController.java:19:    @PostMapping("/{id}")
src/main/java/com/warsha/erp/controllers/InvoiceController.java-20-    public ResponseEntity<InvoiceDto> generateInvoice(@PathVariable Long id) {
--
src/main/java/com/warsha/erp/controllers/InvoiceController.java:27:    @GetMapping("/{id}")
src/main/java/com/warsha/erp/controllers/InvoiceController.java-28-    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id) {
--
src/main/java/com/warsha/erp/controllers/InvoiceController.java:34:    @GetMapping
src/main/java/com/warsha/erp/controllers/InvoiceController.java-35-    public ResponseEntity<List<InvoiceDto>> getAllInvoices() {
--
src/main/java/com/warsha/erp/controllers/InvoiceController.java:45:    @GetMapping("/pdf/{id}")
src/main/java/com/warsha/erp/controllers/InvoiceController.java-46-    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable Long id) {
--
src/main/java/com/warsha/erp/controllers/OrderController.java:24:@RequestMapping("/orders")
src/main/java/com/warsha/erp/controllers/OrderController.java:39:    @PostMapping
src/main/java/com/warsha/erp/controllers/OrderController.java-40-    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
--
src/main/java/com/warsha/erp/controllers/OrderController.java:46:    @PostMapping(value = "/placeOrder", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
src/main/java/com/warsha/erp/controllers/OrderController.java-47-    public ResponseEntity<OrderResponse> placeOrder(
--
src/main/java/com/warsha/erp/controllers/OrderController.java:63:    @GetMapping("/countGovernorates")
src/main/java/com/warsha/erp/controllers/OrderController.java-64-    public List<OrderCountByGovernorateDto> getOrderCountsByGovernorate() {
--
src/main/java/com/warsha/erp/controllers/OrderController.java:69:    @PutMapping("/{id}")
src/main/java/com/warsha/erp/controllers/OrderController.java-70-    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id, @RequestBody CreateOrderRequest request) {
--
src/main/java/com/warsha/erp/controllers/OrderController.java:76:    @PutMapping("/status/{id}")
src/main/java/com/warsha/erp/controllers/OrderController.java-77-    public ResponseEntity<OrderResponse> updateOrderStatus(
--
src/main/java/com/warsha/erp/controllers/OrderController.java:86:    @GetMapping
src/main/java/com/warsha/erp/controllers/OrderController.java-87-    public ResponseEntity<List<OrderResponse>> getAllOrders(
--
src/main/java/com/warsha/erp/controllers/OrderController.java:95:    @GetMapping("/customer")
src/main/java/com/warsha/erp/controllers/OrderController.java-96-    public ResponseEntity<List<OrderResponse>> getAllOrdersByCustomerId(
--
src/main/java/com/warsha/erp/controllers/OrderController.java:104:    @DeleteMapping("/{id}")
src/main/java/com/warsha/erp/controllers/OrderController.java-105-    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
--
src/main/java/com/warsha/erp/controllers/OrderController.java:111:    @PutMapping("/cancel/{id}")
src/main/java/com/warsha/erp/controllers/OrderController.java-112-    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
--
src/main/java/com/warsha/erp/controllers/PaymentController.java:12:@RequestMapping("/payments")
src/main/java/com/warsha/erp/controllers/PaymentController.java:20:    @PostMapping
src/main/java/com/warsha/erp/controllers/PaymentController.java-21-    public ResponseEntity<PaymentDto> createPayment(@RequestBody CreatePaymentRequest request) {
--
src/main/java/com/warsha/erp/controllers/PaymentController.java:25:    @GetMapping
src/main/java/com/warsha/erp/controllers/PaymentController.java-26-    public ResponseEntity<List<PaymentDto>> getAllPayments() {
--
src/main/java/com/warsha/erp/controllers/PaymentController.java:30:    @GetMapping("/order/{orderId}")
src/main/java/com/warsha/erp/controllers/PaymentController.java-31-    public ResponseEntity<List<PaymentDto>> getPaymentsByOrder(@PathVariable Long orderId) {
--
src/main/java/com/warsha/erp/controllers/ProductController.java:20:@RequestMapping("/products")
src/main/java/com/warsha/erp/controllers/ProductController.java:26:    @GetMapping
src/main/java/com/warsha/erp/controllers/ProductController.java-27-    public List<ProductDTO> getAll() {
--
src/main/java/com/warsha/erp/controllers/ProductController.java:31:    @GetMapping("/getByCategoryId")
src/main/java/com/warsha/erp/controllers/ProductController.java-32-    public List<ProductDTO> getAllByCategoryId(@RequestParam Long categoryId) {
--
src/main/java/com/warsha/erp/controllers/ProductController.java:36:    @GetMapping("/getImage")
src/main/java/com/warsha/erp/controllers/ProductController.java-37-    public ResponseEntity<Resource> getProductImage(@RequestParam String filename) {
--
src/main/java/com/warsha/erp/controllers/ProductController.java:62:    @GetMapping("/{id}")
src/main/java/com/warsha/erp/controllers/ProductController.java-63-    public ResponseEntity<Product> getById(@PathVariable Long id) {
--
src/main/java/com/warsha/erp/controllers/ProductController.java:67:    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
src/main/java/com/warsha/erp/controllers/ProductController.java-68-    public ResponseEntity<Product> create(
--
src/main/java/com/warsha/erp/controllers/ProductController.java:78:    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
src/main/java/com/warsha/erp/controllers/ProductController.java-79-    public Product updateProduct(
--
src/main/java/com/warsha/erp/controllers/ProductController.java:89:    @DeleteMapping("/{id}")
src/main/java/com/warsha/erp/controllers/ProductController.java-90-    public ResponseEntity<Void> delete(@PathVariable Long id) {
--
src/main/java/com/warsha/erp/controllers/ProductController.java:95:    @DeleteMapping
src/main/java/com/warsha/erp/controllers/ProductController.java-96-    public ResponseEntity<Void> deleteAll() {
--
src/main/java/com/warsha/erp/controllers/ShippingZoneController.java:14:@RequestMapping("/shipping")
src/main/java/com/warsha/erp/controllers/ShippingZoneController.java:20:    @GetMapping
src/main/java/com/warsha/erp/controllers/ShippingZoneController.java-21-    public List<ShippingZone> getAll() {
--
src/main/java/com/warsha/erp/controllers/ShippingZoneController.java:25:    @GetMapping("/{id}")
src/main/java/com/warsha/erp/controllers/ShippingZoneController.java-26-    public ResponseEntity<ShippingZone> getById(@PathVariable Integer id) {
--
src/main/java/com/warsha/erp/controllers/ShippingZoneController.java:31:    @PostMapping
src/main/java/com/warsha/erp/controllers/ShippingZoneController.java-32-    public ShippingZone create(@RequestBody ShippingZone shippingZone) {
--
src/main/java/com/warsha/erp/controllers/ShippingZoneController.java:36:    @DeleteMapping("/{id}")
src/main/java/com/warsha/erp/controllers/ShippingZoneController.java-37-    public void delete(@PathVariable Integer id) {
--
src/main/java/com/warsha/erp/controllers/ShippingZoneController.java:41:    @PutMapping("/{id}")
src/main/java/com/warsha/erp/controllers/ShippingZoneController.java-42-    public ShippingZone updatePrice(@PathVariable Integer id, @RequestParam BigDecimal shippingFee) {
--
src/main/java/com/warsha/erp/controllers/VendorController.java:13:@RequestMapping("/vendors")
src/main/java/com/warsha/erp/controllers/VendorController.java:18:    @GetMapping
src/main/java/com/warsha/erp/controllers/VendorController.java-19-    public ResponseEntity<List<Vendor>> getAll() {
--
src/main/java/com/warsha/erp/controllers/VendorController.java:23:    @PostMapping
src/main/java/com/warsha/erp/controllers/VendorController.java-24-    public ResponseEntity<Vendor> create(@RequestBody Vendor vendor) {
--
src/main/java/com/warsha/erp/controllers/VendorController.java:28:    @PutMapping("/{id}")
src/main/java/com/warsha/erp/controllers/VendorController.java-29-    public ResponseEntity<Vendor> update(@PathVariable Long id, @RequestBody Vendor vendor) {
--
src/main/java/com/warsha/erp/controllers/VendorController.java:33:    @DeleteMapping("/{id}")
src/main/java/com/warsha/erp/controllers/VendorController.java-34-    public ResponseEntity<Void> delete(@PathVariable Long id) {
--
src/main/java/com/warsha/erp/controllers/VoucherController.java:14:@RequestMapping("/vouchers")
src/main/java/com/warsha/erp/controllers/VoucherController.java:20:    @PostMapping("/validate")
src/main/java/com/warsha/erp/controllers/VoucherController.java-21-    public ResponseEntity<VoucherValidationResponse> validateVoucher(@RequestBody ValidateVoucherRequest request) {
"""

base_urls = {}
for line in controller_data.split('\\n'):
    m = re.search(r'controllers/(.+?Controller)\.java.*?@RequestMapping\("(.+?)"\)', line)
    if m:
        base_urls[m.group(1)] = m.group(2)

items = []
current_controller = None
base_url = ""

for line in controller_data.split('\\n'):
    m = re.search(r'controllers/(.+?Controller)\.java.*?\s@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)(?:\(["\']([^"\']+)["\']\)|.*?value\s*=\s*["\']([^"\']+)["\'])?', line)
    if m:
        current_controller = m.group(1)
        base_url = base_urls.get(current_controller, '')
        method = m.group(2).replace('Mapping', '').upper()
        path = m.group(3) or m.group(4) or ''

        full_path = f"{base_url}{path}"
        # remove starting slash
        if full_path.startswith('/'): full_path = full_path[1:]

        path_segments = full_path.split('/')

        item = {
            "name": f"{method} {full_path}",
            "request": {
                "method": method,
                "header": [],
                "url": {
                    "raw": f"{{{{base_url}}}}/{full_path}",
                    "host": [
                        "{{base_url}}"
                    ],
                    "path": path_segments
                },
                "description": f"Endpoint for {current_controller} {method} {path}"
            },
            "response": []
        }
        items.append(item)

collection = {
    "info": {
        "name": "Warsha Backend API Collection",
        "description": "Generated from Spring Boot controllers.",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
    },
    "item": items,
    "variable": [
        {
            "key": "base_url",
            "value": "http://localhost:8080",
            "type": "string"
        }
    ]
}

with open("warsha_api_collection.json", "w") as f:
    json.dump(collection, f, indent=4)
print("Generated warsha_api_collection.json")
