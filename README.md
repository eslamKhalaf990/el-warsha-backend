# Warsha ERP: A Production-Ready Backend for Modern Business Management

Warsha ERP is a comprehensive, enterprise-grade backend system built with Spring Boot and Java 21. It provides a robust suite of APIs to power business operations, from inventory and order management to financial tracking and cloud integration. This project is designed to showcase a deep understanding of modern backend architecture, security, and DevOps best practices.

## Key Features & Strengths

This project is more than just a simple application; it's a demonstration of building and deploying a complex, real-world system.

### Core Engine Features
- **Comprehensive ERP Modules:** Full lifecycle management for Inventory, Orders, Invoicing, Payments, Customers, and Vendors.
- **Advanced Financial Tracking:** From bank transaction recording and voucher management to cash flow analysis.
- **Dynamic PDF Invoice Generation:** Creates professional, bilingual invoices on the fly, with full **Arabic and English language support**.
- **Robust Inventory System:** Manages products, categories, variants, and stock levels.

### API, Security & Integrations
- **Secure RESTful API:** A complete API secured with **JWT (JSON Web Tokens)** and role-based access control (Admin vs. Customer).
- **Google Drive Integration:** Seamlessly manages file storage in the cloud for documents, invoices, and reports.
- **Automated Email Notifications:** Keeps users informed with event-driven emails via Gmail SMTP.
- **Rock-Solid Security:** Implements modern security standards, including password encryption (BCrypt), SQL injection prevention, and detailed user activity logging.

### DevOps & Deployment
- **Containerized & Scalable:** Fully containerized with **Docker** and orchestrated with **Docker Compose** for easy, reproducible setups and scaling.
- **Zero-Downtime Deployment:** Includes an automated shell script (`deploy.sh`) for seamless updates on a live VPS, demonstrating practical CI/CD knowledge.
- **Optimized Docker Builds:** Utilizes multi-stage Docker builds to create lightweight, production-ready images for efficient deployment.
- **Production-Ready Configuration:** Manages environment-specific settings (database, secrets, API keys) for local, staging, and production.

## Technology Stack

| Category                  | Technology                                                              |
| ------------------------- | ----------------------------------------------------------------------- |
| **Backend Framework**     | Spring Boot 3.1.5 (Java 21)                                             |
| **Database**              | Microsoft SQL Server, Spring Data JPA, Hibernate ORM                    |
| **Security**              | Spring Security, JWT (JSON Web Tokens), BCrypt                          |
| **API & Integrations**    | Google Drive API, Google OAuth, Spring Mail                             |
| **File Processing**       | OpenPDF (for PDF generation), Noto Kufi Arabic Font                     |
| **Build & Deployment**    | Maven, **Docker**, **Docker Compose**, Bash (for deployment scripts)      |
| **Hosting**               | VPS (Virtual Private Server)                                            |

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.8+
- Docker & Docker Compose

### Running Locally with Docker Compose (Recommended)
This is the easiest way to get the entire stack (backend + database) running.

1.  **Start the services:**
    ```bash
    docker-compose up --build
    ```
    The backend will be available at `http://localhost:8080`.

2.  **To stop the services:**
    ```bash
    docker-compose down
    ```

### Running the Backend Manually
1.  **Build the application:**
    ```bash
    mvn clean package
    ```

2.  **Run the JAR file:**
    (Ensure you have a SQL Server instance running and have configured it in `application.properties`)
    ```bash
    java -jar target/erp-0.0.1-SNAPSHOT.jar
    ```

## Deployment

This project is configured for a professional deployment workflow on a Virtual Private Server (VPS), showcasing zero-downtime update capabilities.

### Automated Zero-Downtime Deployment
The `deploy.sh` script automates the entire process:
1.  Builds the latest version of the application.
2.  Securely transfers the JAR file to the VPS.
3.  Stops the old process and starts the new one without any service interruption.
4.  Redirects application output to a log file (`app.log`).

To deploy, simply run:
```bash
./deploy.sh
```

### Manual VPS Deployment Steps
1.  **SSH into your VPS:** `ssh root@your-vps-ip`
2.  **Upload the JAR:** `scp target/erp-0.0.1-SNAPSHOT.jar root@your-vps-ip:/root/`
3.  **Stop the old process:** `pkill -f erp-0.0.1-SNAPSHOT.jar`
4.  **Start the new one:** `nohup java -jar /root/erp-0.0.1-SNAPSHOT.jar > app.log 2>&1 &`

## API Testing

A Postman collection is available (`warsha_api_collection.json`) for testing all API endpoints. Import it into Postman to explore the API's capabilities, manage authentication, and view example requests/responses.
