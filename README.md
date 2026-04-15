# Library Management System

A comprehensive library management system built with Spring Boot, React, and PostgreSQL following Clean Architecture principles.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │ Controllers  │  │   API Docs  │  │  Frontend   │ │
│  └─────────────┘  └─────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│                   Application Layer                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │   Services  │  │     DTOs    │  │   Mappers    │ │
│  └─────────────┘  └─────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│                     Domain Layer                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │   Entities  │  │ Repositories │  │ Exceptions   │ │
│  └─────────────┘  └─────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│                Infrastructure Layer                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │ Persistence  │  │   Security   │  │   Config     │ │
│  └─────────────┘  └─────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- Java 17+ (for local development)
- Node.js 18+ (for frontend development)

### Using Docker Compose (Recommended)

1. **Clone repository:**
   ```bash
   git clone <repository-url>
   cd library-system
   ```

2. **Configure environment variables:**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start system:**
   ```bash
   # Development
   docker-compose up -d
   
   # Production
   docker-compose -f docker-compose.prod.yaml up -d --build
   ```

4. **Access application:**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api
   - Swagger Documentation: http://localhost:8080/swagger-ui/index.html
   - Grafana Dashboard: http://localhost:3001 (admin/admin123)
   - Prometheus: http://localhost:9090

### Test Accounts (Local)

Use these accounts for local testing after `docker compose up --build -d`.

- Admin account (easy to identify)
   - Display name: ADMIN_TEST_ACCOUNT
   - Email: admin@library.local
   - Password: Admin@123
   - Role: ADMIN

Notes:
- This account is stored in PostgreSQL runtime data, not in source code.
- If you run `docker compose down -v`, the database volume is removed and you need to recreate test accounts.

### Local Development

1. **Start PostgreSQL:**
   ```bash
   docker-compose up postgres -d
   ```

2. **Run Backend:**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Run Frontend:**
   ```bash
   cd frontend
   npm install
   npm start
   ```

## 📁 Project Structure

```
library-system/
├── src/main/java/com/lms/library/
│   ├── domain/                    # Business logic
│   │   ├── entity/              # Domain entities
│   │   ├── repository/          # Repository interfaces
│   │   ├── exception/          # Business exceptions
│   │   └── service/            # Domain services
│   ├── application/              # Application layer
│   │   ├── service/            # Application services
│   │   └── dto/               # Data Transfer Objects
│   ├── infrastructure/          # Infrastructure layer
│   │   ├── persistence/        # Database persistence
│   │   │   ├── jpa/          # JPA entities
│   │   │   ├── repository/    # Repository implementations
│   │   │   └── mapper/        # MapStruct mappers
│   │   ├── security/           # Security configuration
│   │   └── exception/        # Infrastructure exceptions
│   ├── presentation/            # Presentation layer
│   │   ├── controller/         # REST controllers
│   │   └── exception/        # Global exception handler
│   └── LibrarySystemApplication.java
├── src/main/resources/
│   ├── db/migration/           # Flyway migrations
│   └── application.yml         # Configuration
├── frontend/                   # React frontend
├── docker-compose.yaml          # Development compose
├── Dockerfile                 # Backend Dockerfile
├── nginx/                     # Nginx configuration
└── docs/                      # Documentation
```

## 🗄️ Database

### Schema Overview

The system uses PostgreSQL with the following main entities:

- **Users & Authentication**: Multi-role system with JWT tokens
- **Books**: Physical and e-books with categories and tags
- **Borrowing**: Reservations, borrow records, and fine management
- **Reviews**: Ratings and review system
- **Notifications**: Real-time notifications for users
- **Analytics**: User activity and system metrics

### Migrations

Database migrations are managed with Flyway:
- `V1__Create_initial_schema.sql` - Complete schema
- `V1_1__Add_sample_data.sql` - Sample data for testing

## 🔧 Configuration

### Environment Variables

Key environment variables (see `.env.example`):

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=library_db
DB_USER=postgres
DB_PASSWORD=postgres

# Redis Cache
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis123

# Application
JWT_SECRET=your-secret-key
BACKEND_PORT=8080
FRONTEND_PORT=3000
```

### Profiles

- **dev**: Development with debug logging
- **prod**: Production with optimizations

## 🔐 Security

- **Authentication**: JWT tokens with refresh mechanism
- **Authorization**: Role-based access control (RBAC)
- **Password**: BCrypt encryption
- **API Security**: Rate limiting, CORS, security headers
- **Data**: PII encryption and audit logging

## 📊 Features

### Core Features
- ✅ User management with multi-role system
- ✅ Book catalog with advanced search
- ✅ Borrow/return system with fine calculation
- ✅ Review and rating system
- ✅ Reservation system
- ✅ Real-time notifications
- ✅ Favorites and wishlist
- ✅ Reading history tracking

### Technical Features
- ✅ Clean Architecture with separation of concerns
- ✅ MapStruct for entity mapping
- ✅ Flyway for database migrations
- ✅ Redis caching for performance
- ✅ Prometheus metrics and Grafana dashboards
- ✅ Docker containerization
- ✅ Nginx reverse proxy with SSL
- ✅ Health checks and monitoring

## 🧪 Testing

### Run Tests
```bash
# Backend tests
./mvnw test

# Frontend tests
cd frontend
npm test
```

### Test Coverage
- Unit tests for services and repositories
- Integration tests for APIs
- E2E tests for critical user flows

## 📈 Monitoring & Logging

### Application Metrics
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001
- **Health Checks**: http://localhost:8080/api/actuator/health

### Logging
- Structured JSON logging
- Different levels for different environments
- Log rotation and retention policies

## 🚀 Deployment

### Production Deployment

1. **Build and deploy:**
   ```bash
   docker-compose -f docker-compose.prod.yaml up -d --build
   ```

2. **SSL Configuration:**
   - Place SSL certificates in `nginx/ssl/`
   - Update `nginx.conf` with your domain

3. **Environment Setup:**
   - Configure production environment variables
   - Set up database backups
   - Configure monitoring alerts

### Scaling

- **Horizontal scaling**: Multiple backend instances
- **Database**: Read replicas and connection pooling
- **Cache**: Redis cluster for distributed caching
- **CDN**: Static assets delivery

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new features
5. Submit a pull request

### Code Style
- Follow Clean Architecture principles
- Use MapStruct for entity mapping
- Write comprehensive unit tests
- Document API changes

## 📝 API Documentation

### Swagger UI
Access interactive API documentation at:
http://localhost:8080/swagger-ui.html

### Key Endpoints

#### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/logout` - User logout

#### Books
- `GET /api/books` - List books with pagination
- `GET /api/books/{id}` - Get book details
- `POST /api/books` - Create book (admin/librarian)
- `PUT /api/books/{id}` - Update book (admin/librarian)

#### Borrowing
- `POST /api/borrows` - Create borrow request
- `POST /api/borrows/return` - Return book
- `GET /api/borrows/history` - User borrow history

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check PostgreSQL is running: `docker-compose ps`
   - Verify environment variables
   - Check database logs: `docker-compose logs postgres`

2. **Build Fails**
   - Clear Docker cache: `docker system prune -a`
   - Check Java version: `java -version`
   - Verify Maven wrapper: `./mvnw -version`

3. **Frontend Not Loading**
   - Check API URL in environment variables
   - Verify CORS configuration
   - Check backend health: `curl localhost:8080/api/actuator/health`

### Logs

```bash
# View all logs
docker-compose logs

# View specific service logs
docker-compose logs backend
docker-compose logs postgres
docker-compose logs frontend
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- Create an issue in the repository
- Check documentation in the `/docs` folder
- Review the troubleshooting section above

---

**Built with ❤️ using Clean Architecture principles and modern Java technologies.**
