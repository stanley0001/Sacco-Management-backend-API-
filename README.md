# SACCO Management System - Backend API

A comprehensive SACCO (Savings and Credit Cooperative) management system built with Spring Boot 3.2.5.

## Features

### Core Modules
- **Customer Management** - Complete member lifecycle management
- **Loan Management** - Loan origination, disbursement, repayment tracking
- **Savings Management** - Multiple savings products, deposits, withdrawals
- **Reports & Analytics** - Comprehensive reporting with SASRA compliance
- **Communication** - SMS, Email, and WhatsApp integration
- **User Management** - Role-based access control with JWT authentication

### SASRA Compliance
- SG3 - Loan Classification Report
- SG4 - Liquidity Report  
- SG5 - Capital Adequacy Report
- Prudential Returns
- Monthly Returns

## Technology Stack

- **Framework**: Spring Boot 3.2.5
- **Java Version**: 17
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT
- **API Documentation**: OpenAPI 3.0 (Swagger)
- **Build Tool**: Maven
- **Containerization**: Docker

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ (or use Docker)
- (Optional) Docker and Docker Compose

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Sacco-Management-backend-API-
```

### 2. Configure Environment Variables

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` with your configuration:

```properties
DB_URL=jdbc:postgresql://localhost:5432/sacco_management
DB_USERNAME=sacco_admin
DB_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret_key_min_256_bits
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### 3. Run with Docker (Recommended)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f sacco-api

# Stop services
docker-compose down
```

### 4. Run Locally

```bash
# Install dependencies
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8082`

## API Documentation

Once running, access the API documentation at:
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8082/api-docs

## Key Endpoints

### Authentication
- `POST /authenticate` - User login
- `POST /users/create` - Register new user

### Customers
- `GET /api/customers/all` - List all customers
- `POST /api/customers/create` - Create customer
- `GET /api/customers/findCus{id}` - Get customer details

### Loans
- `POST /api/customers/loanApplication` - Apply for loan
- `POST /api/customers/loanRepayment` - Record payment
- `GET /api/products/getLoanAccountId{id}` - Get loan details

### Savings
- `POST /api/savings/accounts` - Create savings account
- `POST /api/savings/accounts/{id}/deposit` - Deposit to savings
- `POST /api/savings/accounts/{id}/withdraw` - Withdraw from savings
- `GET /api/savings/accounts/customer/{customerId}` - Get customer accounts

### Reports
- `GET /api/reports/loan-portfolio` - Loan portfolio report
- `GET /api/reports/sasra` - SASRA compliance report
- `GET /api/reports/sasra/sg3` - SG3 Loan Classification
- `GET /api/reports/loan-portfolio/export` - Export to Excel

## Database Schema

The application will automatically create/update the database schema on startup (DDL_AUTO=update).

For production, set `DDL_AUTO=validate` and manage migrations manually.

## Security

- JWT-based authentication
- Role-based access control (RBAC)
- Password encryption with BCrypt
- CORS configuration for frontend integration
- Security headers and CSRF protection

## Deployment

### Production Deployment

1. **Build the application**:
```bash
./mvnw clean package -DskipTests
```

2. **Set production environment variables**:
```bash
export DDL_AUTO=validate
export SHOW_SQL=false
export LOG_LEVEL=WARN
export JWT_SECRET=<strong-secret-key>
```

3. **Run with Docker**:
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Cloud Deployment

The application is containerized and can be deployed to:
- AWS ECS/EKS
- Google Cloud Run/GKE
- Azure Container Instances/AKS
- Heroku
- DigitalOcean App Platform

## Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

Proprietary - SACCO Management System © 2025

## Support

For support and queries, contact: support@sacco-management.com
