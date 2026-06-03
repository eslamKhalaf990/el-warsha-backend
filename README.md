# Warsha ERP Backend

A comprehensive Enterprise Resource Planning (ERP) system built with Spring Boot for managing business operations including inventory, orders, invoices, payments, customers, vendors, and financial tracking.

## Project Overview

Warsha ERP is a full-featured backend system designed to streamline business operations. It provides APIs for managing products, customers, vendors, orders, invoices, payments, and financial transactions. The system includes role-based authentication with JWT tokens, email notifications, PDF invoice generation, and Google Drive integration for file management.

## Key Features

### Authentication & Security
- User and customer authentication with JWT tokens
- Role-based access control (ADMIN and CUSTOMER roles)
- Password encryption using BCrypt
- Activity logging for user actions
- CORS configuration for cross-origin requests

### Product & Inventory Management
- Product CRUD operations
- Category management and organization
- Product variant tracking
- Vendor management with contact information
- Shipping zone configuration

### Order Management
- Order creation and tracking
- Order items management with quantities and pricing
- Order status tracking
- Invoice generation from orders
- Payment processing and reconciliation

### Financial Management
- Invoice generation and management
- Automatic PDF invoice generation with Arabic support
- Payment tracking with multiple payment methods
- Voucher management for discounts
- Bank transaction recording
- Cash flow analysis and reporting
- Bank account management

### Customer Management
- Customer CRUD operations
- Customer contact and address information
- Customer order history
- Activity logging for customer interactions

### Additional Features
- Email notifications via Gmail SMTP
- Google Drive integration for file uploads and management
- User activity tracking and logging
- PDF generation with Arabic text support (Noto Kufi Arabic font)
- RESTful API endpoints for all features
- Global exception handling with meaningful error responses
- File upload management with configurable limits

## Technology Stack

### Backend Framework
- **Spring Boot 3.1.5** - Java 21 compatible
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database abstraction layer
- **Spring Mail** - Email sending capabilities

### Database
- **Microsoft SQL Server** - Primary database
- **Hibernate ORM** - Object-relational mapping

### Authentication & Security
- **JWT (JJWT 0.11.5)** - Token-based authentication
- **BCrypt** - Password encryption

### File Processing & Generation
- **OpenPDF 1.3.39** - PDF generation and manipulation
- **Noto Kufi Arabic Font** - Arabic text support in PDFs
- **ICU4J 77.1** - International character support

### External Integrations
- **Google Drive API** - Cloud file storage and management
- **Google OAuth Client** - Authentication for Google services
- **Gmail SMTP** - Email notifications

### Build & Deployment
- **Maven 3.8.3** - Build automation
- **Docker** - Containerization
- **Linux/VPS Deployment** - Production hosting

## Project Structure

```
src/main/java/com/warsha/erp/
├── config/              # Security, JWT, CORS, database configuration
├── controllers/         # REST API endpoints
├── entities/           # JPA entity models
├── dtos/               # Data transfer objects
├── services/           # Business logic layer
├── repository/         # Data access layer
└── exceptions/         # Custom exception classes

src/main/resources/
├── application.properties   # Configuration file
└── static/              # Static assets
```

## API Endpoints

### Authentication
- `POST /auth/login` - User login
- `POST /auth/customer-login` - Customer login
- `POST /auth/change-password` - Change user password

### Products & Categories
- `GET/POST /product` - Product management
- `GET/POST /category` - Category management

### Orders & Invoices
- `GET/POST /order` - Order management
- `POST /invoice/{id}` - Generate invoice from order
- `GET /invoice/{id}` - Get invoice details
- `GET /invoice/pdf/{id}` - Download invoice as PDF

### Payments
- `POST /payment` - Record payment
- `GET /payment` - Get payment history

### Customers & Vendors
- `GET/POST /customer` - Customer management
- `GET/POST /vendor` - Vendor management

### Financial Management
- `POST /bank-transaction` - Record bank transactions
- `GET /cash-flow` - Cash flow analysis
- `GET/POST /voucher` - Discount voucher management

### Shipping
- `GET/POST /shipping-zone` - Shipping zone configuration

## Configuration

The application requires configuration through `application.properties`:

```properties
# Database Connection
spring.datasource.url=jdbc:sqlserver://[HOST]:1433;databaseName=[DB_NAME]
spring.datasource.username=[USERNAME]
spring.datasource.password=[PASSWORD]

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=[GMAIL_ADDRESS]
spring.mail.password=[APP_PASSWORD]

# JWT Secret
jwt.secret=[SECURE_SECRET_KEY]

# Google OAuth (for Drive integration)
google.client.id=[CLIENT_ID]
google.client.secret=[CLIENT_SECRET]
google.refresh.token=[REFRESH_TOKEN]

# File Upload Limits
spring.servlet.multipart.max-file-size=15MB
spring.servlet.multipart.max-request-size=15MB
```

## Building the Application

### Prerequisites
- Java 21+
- Maven 3.8.3+
- Docker (for containerized deployment)

### Build Process
```bash
# Clone the repository
git clone https://github.com/eslamKhalaf990/el-warsha-backend.git
cd warsha_backend

# Build with Maven
mvn clean package

# The built JAR will be in target/erp-0.0.1-SNAPSHOT.jar
```

## Deployment

### Docker Containerization

The application is containerized using a two-stage Docker build for optimized image size:

**Dockerfile Overview:**
- **Build Stage**: Uses Maven 3.8.3 with OpenJDK 17 to compile and package the application
- **Runtime Stage**: Uses Eclipse Temurin JDK 17 for lightweight production image
- **Exposure**: Application listens on port 8080

```bash
# Build Docker image
docker build -t warsha-erp:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:sqlserver://[HOST]:1433 \
  -e SPRING_DATASOURCE_USERNAME=[USER] \
  -e SPRING_DATASOURCE_PASSWORD=[PASS] \
  --name warsha-erp \
  warsha-erp:latest
```

### SQL Server Database Deployment

The application uses Microsoft SQL Server as the primary database, deployed in a containerized environment:

```bash
# Run SQL Server container
docker run -d \
  --name warsha-sqlserver \
  -e "ACCEPT_EULA=Y" \
  -e "SA_PASSWORD=YourStrongPassword123!" \
  -p 1433:1433 \
  mcr.microsoft.com/mssql/server:latest

# Create database
docker exec -it warsha-sqlserver \
  /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P "YourStrongPassword123!" \
  -Q "CREATE DATABASE WarshaDB"
```

Alternatively, use Docker Compose for simplified setup:

```yaml
version: '3.8'
services:
  sqlserver:
    image: mcr.microsoft.com/mssql/server:latest
    environment:
      ACCEPT_EULA: "Y"
      SA_PASSWORD: "YourStrongPassword123!"
    ports:
      - "1433:1433"
    volumes:
      - sqlserver_data:/var/opt/mssql

  erp-backend:
    image: warsha-erp:latest
    depends_on:
      - sqlserver
    environment:
      SPRING_DATASOURCE_URL: jdbc:sqlserver://sqlserver:1433;databaseName=WarshaDB
      SPRING_DATASOURCE_USERNAME: sa
      SPRING_DATASOURCE_PASSWORD: YourStrongPassword123!
    ports:
      - "8080:8080"

volumes:
  sqlserver_data:
```

### VPS Deployment

The application is deployed to a dedicated VPS (Virtual Private Server) for production environments:

**Deployment Strategy:**
- **Zero-Downtime Deployment**: The system supports seamless updates without service interruption
- **Automated Deployment Script**: `deploy.sh` handles the entire deployment process
- **JAR-based Execution**: The compiled Spring Boot JAR runs directly on the VPS

**Deployment Process:**

1. **Build the application:**
   ```bash
   mvn clean package
   ```

2. **Deploy to VPS using the deployment script:**
   ```bash
   ./deploy.sh
   ```

**What the deployment script does:**
- Uploads the new JAR file to the VPS via SCP
- Stops the running application gracefully
- Starts the new application in the background
- Maintains application logs in `app.log`

**Manual VPS Deployment (if needed):**

```bash
# 1. SSH into your VPS
ssh root@your-vps-ip

# 2. Upload JAR file
scp target/erp-0.0.1-SNAPSHOT.jar root@your-vps-ip:/root/

# 3. Stop existing application
pkill -f erp-0.0.1-SNAPSHOT.jar

# 4. Start new application
nohup java -jar /root/erp-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 5. Verify it's running
ps aux | grep erp-0.0.1-SNAPSHOT.jar
```

**VPS Configuration:**
- **Server IP**: 67.211.210.15
- **Port**: 8080 (configurable via environment variable PORT)
- **User**: root
- **Application Logs**: `/root/app.log`
- **Database Connection**: Established to hosted SQL Server instance

## Running Locally

```bash
# Using Maven
mvn spring-boot:run

# Or using Java directly after building
java -jar target/erp-0.0.1-SNAPSHOT.jar

# With custom port
PORT=8081 java -jar target/erp-0.0.1-SNAPSHOT.jar
```

## Database Schema

The application uses Hibernate with SQL Server and maintains the schema in database. Key entities include:

- **User** - System administrators
- **Customer** - End customers
- **Product** - Product catalog
- **Category** - Product categorization
- **Order** - Customer orders
- **OrderItems** - Line items in orders
- **Invoice** - Generated from orders
- **Payment** - Payment records
- **Vendor** - Supplier information
- **BankAccount** - Bank account details
- **BankTransaction** - Financial transactions
- **Voucher** - Discount codes
- **ShippingZone** - Shipping regions
- **UserActivityLog** - User action tracking

## Development

### Running Tests
```bash
mvn test
```

### Code Structure Best Practices
- Controllers handle HTTP requests/responses
- Services contain business logic
- Repositories manage database operations
- DTOs transfer data between layers
- Entities represent database models
- Config classes define application configuration

## Postman Collection

A Postman collection is provided (`warsha_api_collection.json`) for testing all API endpoints. Import it into Postman to:
- Test all endpoints
- Manage authentication tokens
- Organize API calls by feature
- Share API documentation with team members

Generate or update Postman collection:
```bash
python generate_postman.py
```

## Logging & Monitoring

The application includes comprehensive logging with timestamps:
- Authentication attempts and successes
- Business operation tracking
- Error logging with stack traces
- Activity logs stored in database

Configure logging in `application.properties`:
```properties
spring.jpa.show-sql=false
logging.level.org.hibernate.SQL=ERROR
```

## Performance Considerations

- JPA lazy loading configured for relationships
- Database connection pooling via Spring Boot defaults
- Efficient PDF generation with OpenPDF
- Async email sending capability
- Request size limits: 15MB per file, 15MB total per request

## Security Features

- JWT-based stateless authentication
- Password encryption with BCrypt
- CORS configuration for frontend integration
- SQL injection prevention through parameterized queries
- Role-based access control
- Activity audit logging

## API Response Format

All API responses follow a consistent format with proper HTTP status codes:
- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Troubleshooting

### Database Connection Issues
- Verify SQL Server is running and accessible
- Check connection string in application.properties
- Ensure database name exists

### Email Not Sending
- Verify Gmail credentials and app password
- Enable SMTP in Gmail security settings
- Check SMTP configuration in application.properties

### PDF Generation Issues
- Ensure Noto Kufi Arabic font is in resources/fonts
- Check OpenPDF dependency is properly included

### Google Drive Integration
- Verify OAuth credentials are valid
- Check refresh token hasn't expired
- Ensure folder ID has proper permissions

## Future Enhancements

- Advanced analytics and reporting
- Mobile application integration
- Inventory forecasting with AI
- Multi-currency support
- Advanced user role management
- API rate limiting
- Webhook integrations

## License

This project is proprietary software for Warsha business operations.
