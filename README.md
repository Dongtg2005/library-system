# Library Management System - Microservices Architecture

A scalable Library Management System built with Spring Boot microservices, featuring service discovery, API gateway, authentication, and book/borrow management services.

## Services Overview

### 1. **API Gateway** (Port: 8080)
- Central entry point for all client requests
- Routing to appropriate microservices
- Load balancing
- Located in `api-gateway/`

### 2. **Auth Service** (Port: 8081)
- User authentication and authorization
- JWT token generation and validation
- Role-based access control
- Located in `auth-service/`

### 3. **User Service** (Port: 8082)
- User profile management
- User registration and updates
- Located in `user-service/`

### 4. **Borrow Service** (Port: 8083)
- Book borrowing and returning
- Borrow history
- Utilizes Feign client for inter-service communication
- Located in `borrow-service/`

### 5. **Book Service** (Port: 8084)
- Book catalog management
- Book inventory
- Located in `book-service/`

## Project Structure

```
library-system/
├── pom.xml (Parent POM)
├── api-gateway/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lms/library/gateway/ApiGatewayApplication.java
│   │   │   └── resources/application.yaml
│   │   └── test/
│   └── mvnw
├── auth-service/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lms/library/auth/AuthServiceApplication.java
│   │   │   └── resources/application.yaml
│   │   └── test/
│   └── mvnw
├── user-service/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lms/library/user/UserServiceApplication.java
│   │   │   └── resources/application.yaml
│   │   └── test/
│   └── mvnw
├── book-service/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lms/library/book/BookServiceApplication.java
│   │   │   └── resources/application.yaml
│   │   └── test/
│   └── mvnw
└── borrow-service/
    ├── pom.xml
    ├── src/
    │   ├── main/
    │   │   ├── java/com/lms/library/borrow/BorrowServiceApplication.java
    │   │   └── resources/application.yaml
    │   └── test/
    └── mvnw
```

## Technology Stack

- **Java 21** - Latest Java LTS version
- **Spring Boot 3.5.12-SNAPSHOT** - Latest Spring Boot
- **Spring Cloud 2024.0.0** - Microservices patterns
- **Spring Cloud Gateway** - API Gateway
- **Spring Cloud Eureka** - Service Discovery
- **Spring Cloud OpenFeign** - Declarative HTTP Client
- **Spring Security** - Authentication & Authorization
- **JWT (JJWT)** - Token-based authentication
- **Spring Data JPA** - Data persistence
- **PostgreSQL** - Relational Database
- **Lombok** - Code generation
- **Maven** - Build tool

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.9+
- PostgreSQL 12+

### Database Setup

Create databases for each service:

```sql
CREATE DATABASE auth_db;
CREATE DATABASE user_db;
CREATE DATABASE book_db;
CREATE DATABASE borrow_db;
```

### Build and Run

**Build entire project:**
```bash
mvn clean install
```

**Run individual services:**
```bash
# API Gateway
cd api-gateway
mvn spring-boot:run

# Auth Service
cd auth-service
mvn spring-boot:run

# User Service
cd user-service
mvn spring-boot:run

# Book Service
cd book-service
mvn spring-boot:run

# Borrow Service
cd borrow-service
mvn spring-boot:run
```

### Service Routing (via API Gateway)

- `/auth/**` → Auth Service (8081)
- `/users/**` → User Service (8082)
- `/books/**` → Book Service (8084)
- `/borrows/**` → Borrow Service (8083)

## Configuration

Each service has its own `application.yaml` configuration file with:
- Service name
- Database connection details
- Port assignment
- JWT settings (for Auth Service)

### Default Configuration

**Database Connection:**
- Host: localhost
- Port: 5432
- Username: postgres
- Password: postgres

## Development

### Adding New Endpoints

1. Create a Controller class in the appropriate service
2. Add necessary models and repositories
3. Implement business logic in services
4. Test endpoints via API Gateway or directly

### Inter-Service Communication

Services use Spring Cloud OpenFeign for REST-based inter-service calls:

```java
@FeignClient(name = "book-service")
public interface BookServiceClient {
    @GetMapping("/api/books/{id}")
    BookDTO getBook(@PathVariable Long id);
}
```

## Testing

Run tests for all services:
```bash
mvn test
```

Run tests for a specific service:
```bash
cd service-name
mvn test
```

## License

This project is licensed under the MIT License.