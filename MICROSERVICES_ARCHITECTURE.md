# Microservices Architecture - Hệ Thống Quản Lý Thư Viện

## Tổng Quan Kiến Trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT (Web/Mobile)                       │
└────────────────────────────┬────────────────────────────────────┘
                             │
                    HTTP/REST Request
                             │
        ┌────────────────────▼─────────────────────┐
        │         API Gateway (Port 8080)          │
        │  - Request Routing                       │
        │  - Load Balancing                        │
        │  - Rate Limiting                         │
        └────────────────────┬─────────────────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
    ┌─────▼───────┐   ┌─────▼───────┐   ┌─────▼──────────┐
    │ Auth Service│   │ User Service │   │  Book Service  │
    │ (Port 8081) │   │ (Port 8082)  │   │  (Port 8083)   │
    │             │   │              │   │                │
    │ • Register  │   │ • Profile    │   │ • Catalog      │
    │ • Login     │   │ • Settings   │   │ • Search       │
    │ • JWT Auth  │   │ • History    │   │ • Details      │
    └──────┬──────┘   └──────┬───────┘   └────────┬───────┘
           │                 │                    │
           └─────────────────┼────────────────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
    ┌─────▼──────────┐  ┌────▼──────────┐  ┌───▼──────────────┐
    │ Borrow Service │  │ PostgreSQL    │  │  Redis Cache     │
    │ (Port 8084)    │  │  (Database)   │  │  (Optional)      │
    │                │  │               │  │  • Session       │
    │ • Borrow Book  │  │ • auth_db     │  │  • Caching       │
    │ • Return Book  │  │ • users_db    │  │                  │
    │ • Track Loans  │  │ • books_db    │  └──────────────────┘
    │ • Notifications│  │ • borrows_db  │
    └────────────────┘  └───────────────┘
```

## 4.1 Auth Service (Dịch Vụ Xác Thực)

### Mục Đích
Xử lý tất cả các hoạt động liên quan đến xác thực và ủy quyền của người dùng.

### Port: 8081

### Các Tính Năng Chính

#### 1. Đăng Ký (Registration)
```
POST /api/auth/register

Input:
- email (string) - Email duy nhất
- password (string) - Mật khẩu tối thiểu 6 ký tự
- fullName (string) - Họ tên đầy đủ

Output:
- userId
- email
- fullName
- role (mặc định: USER)
- accessToken (JWT)
- expiresIn (24 giờ)

Kiểm tra:
✓ Email không trùng lặp
✓ Mật khẩu được hash bằng BCrypt
✓ Tạo JWT token ngay lập tức
```

#### 2. Đăng Nhập (Login)
```
POST /api/auth/login

Input:
- email (string)
- password (string)

Output:
- accessToken (JWT)
- role
- userInfo

Kiểm tra:
✓ Email tồn tại
✓ Mật khẩu khớp
✓ Generate JWT token mới
```

#### 3. Xác Thực JWT (JWT Authentication)
```
Mechanism:
┌─────────────────────────────────────────┐
│  Client sends Authorization header      │
│  Authorization: Bearer {token}          │
└─────────────────────┬───────────────────┘
                      │
        ┌─────────────▼────────────────┐
        │ JwtAuthenticationFilter      │
        │ intercepts and validates     │
        │ - Extract token              │
        │ - Verify signature           │
        │ - Check expiration           │
        │ - Load user details          │
        └─────────────────┬────────────┘
                          │
              ┌───────────▼──────────┐
    Yes  ◄────┤ Valid? Authenticated │────► No  ──► 401 Unauthorized
              └──────────────────────┘
```

**Endpoint**: GET /api/auth/validate?token={token}

#### 4. Xác Thực Người Dùng (User Authentication)
```
GET /api/auth/me
Headers: Authorization: Bearer {token}

Trả về:
- userId
- email
- fullName
- role (ADMIN, LIBRARIAN, USER)
- tokenType
```

### Database Schema

```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  enabled BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  
  CONSTRAINT users_email_unique UNIQUE (email)
);

CREATE INDEX idx_users_email ON users(email);
```

### Các Role

| Role | Mô Tả | Quyền |
|------|-------|-------|
| **ADMIN** | Quản trị viên hệ thống | Toàn quyền |
| **LIBRARIAN** | Thủ thư | Quản lý sách, mượn trả |
| **USER** | Người dùng bình thường | Tìm kiếm, mượn sách |

### Quy Trình Đăng Ký và Đăng Nhập

```
┌────────────────────────────────────────────┐
│           REGISTRATION FLOW                 │
└─────────────────────────┬──────────────────┘
                          │
              ┌───────────▼──────────┐
              │ Validate Input Data  │
              │ - Check email format │
              │ - Check password min │
              └────────────┬─────────┘
                           │
        ┌──────────────────▼──────────────────────┐
        │ Check Email Already Exists?              │
        │                                          │
    YES │                        NO                │
    ────┼────►  409 Conflict  │  ┌──────────────────┐
                              │  │ Create User      │
                              │  │ - Hash Password  │
                              │  │ - Save to DB     │
                              │  └────────┬─────────┘
                              │           │
                              │  ┌────────▼──────────┐
                              │  │ Generate JWT      │
                              │  │ - Expiration: 24h │
                              │  └────────┬──────────┘
                              │           │
                              │  ┌────────▼─────────┐
                              └─►│ Return 201 + JWT │
                                 └──────────────────┘

┌────────────────────────────────────────────┐
│             LOGIN FLOW                      │
└─────────────────────────┬──────────────────┘
                          │
              ┌───────────▼──────────────────┐
              │ Validate Input Data          │
              │ - Check email format         │
              │ - Check password not empty   │
              └────────────┬─────────────────┘
                           │
        ┌──────────────────▼────────────────┐
        │ Find User by Email                 │
        │                                    │
    NOT FOUND                             EXISTS
    ────────┼──► 401 Unauthorized  │  ┌────────────────┐
                                    │  │ Compare Pwd    │
                                    │  │ with Hash      │
                                    │  └────────┬───────┘
                                    │           │
                                    │    ┌──────▼──────────┐
                         MISMATCH   │    │ Mismatch?       │
                            YES ════════►│ 401 Unauthorized│
                                    │    │                 │
                                    │    │ Match ──────────┼──┐
                                    │    └─────────────────┘  │
                                    │                         │
                                    │  ┌──────────────────────┘
                                    │  │
                                    │  ▼
                                    │  Generate JWT
                                    │  │
                                    │  ▼
                                    │  Return 200 + JWT
```

### JWT Token Structure

```
Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "sub": "user@example.com",
  "iat": 1704067200,
  "exp": 1704153600,
  "iss": "auth-service"
}

Signature: HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

### Security Configuration

```yaml
# application.yaml
jwt:
  secret: ${JWT_SECRET}  # Must be >= 256 bits
  expiration: 86400000   # 24 hours in milliseconds
```

### Exception Handling

| Exception | HTTP Code | Message |
|-----------|-----------|---------|
| EmailAlreadyExistsException | 409 | Email đã được đăng ký |
| InvalidCredentialsException | 401 | Email hoặc mật khẩu không chính xác |
| MethodArgumentNotValidException | 400 | Dữ liệu đầu vào không hợp lệ |
| General Exception | 500 | Lỗi server |

### API Endpoints Summary

| Method | Endpoint | Public | Mô Tả |
|--------|----------|--------|-------|
| POST | /api/auth/register | ✓ | Đăng ký người dùng mới |
| POST | /api/auth/login | ✓ | Đăng nhập |
| GET | /api/auth/me | ✗ | Lấy info người dùng hiện tại |
| GET | /api/auth/validate | ✓ | Xác thực token |
| POST | /api/auth/logout | ✗ | Đăng xuất |

## Tích Hợp với API Gateway

```yaml
# API Gateway Configuration
routes:
  - id: auth-service
    uri: http://auth-service:8081
    predicates:
      - Path=/api/auth/**
    filters:
      - name: CircuitBreaker
        args:
          name: authServiceCircuitBreaker
          fallbackUri: forward:/fallback/auth
```

## Dependencies

```xml
<!-- JWT -->
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.12.3</version>
</dependency>

<!-- Spring Security -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
</dependency>
```

## Running & Testing

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run

# Test
mvn test

# Docker
docker build -t auth-service .
docker run -p 8081:8081 auth-service
```

## CURL Examples

```bash
# Register
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "fullName": "Nguyễn Văn A"
  }'

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'

# Get Current User (with token)
curl -X GET http://localhost:8081/api/auth/me \
  -H "Authorization: Bearer eyJhbGc..."

# Validate Token
curl -X GET "http://localhost:8081/api/auth/validate?token=eyJhbGc..."
```

## Next Steps

1. ✓ Implement Auth Service (Done)
2. → Implement User Service
3. → Implement Book Service
4. → Implement Borrow Service
5. → Setup API Gateway
6. → Integration Testing
7. → Deployment

---

**Last Updated**: 2024-01-15  
**Version**: 1.0.0
