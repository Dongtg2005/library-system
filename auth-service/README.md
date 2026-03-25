# Auth Service - Dịch vụ Xác thực

## Giới thiệu

Auth Service là dịch vụ vi mô chuyên xử lý xác thực và ủy quyền cho hệ thống Quản lý Thư viện. Dịch vụ này cung cấp các tính năng:

- **Đăng ký người dùng** (Registration)
- **Đăng nhập** (Login)
- **Xác thực bằng JWT Token**
- **Kiểm tra tính hợp lệ của Token**

## Kiến trúc

### Các Component Chính

```
├── entity/          - Các entity JPA (User)
├── repository/      - Data Access Layer
├── dto/             - Data Transfer Objects
├── service/         - Business Logic
├── controller/      - REST Endpoints
├── security/        - Spring Security Configuration
├── util/            - JWT Utilities
└── exception/       - Exception Handling
```

## REST API Endpoints

### 1. Đăng ký người dùng
```
POST /api/auth/register

Request Body:
{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "Tên Người Dùng"
}

Response (201 Created):
{
  "userId": 1,
  "email": "user@example.com",
  "fullName": "Tên Người Dùng",
  "role": "USER",
  "accessToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

### 2. Đăng nhập
```
POST /api/auth/login

Request Body:
{
  "email": "user@example.com",
  "password": "password123"
}

Response (200 OK):
{
  "userId": 1,
  "email": "user@example.com",
  "fullName": "Tên Người Dùng",
  "role": "USER",
  "accessToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

### 3. Lấy thông tin người dùng hiện tại
```
GET /api/auth/me

Headers:
Authorization: Bearer {token}

Response (200 OK):
{
  "userId": 1,
  "email": "user@example.com",
  "fullName": "Tên Người Dùng",
  "role": "USER",
  "tokenType": "Bearer"
}
```

### 4. Xác thực Token
```
GET /api/auth/validate?token={token}

Response (200 OK):
{
  "userId": 1,
  "email": "user@example.com",
  "fullName": "Tên Người Dùng",
  "role": "USER",
  "tokenType": "Bearer"
}
```

### 5. Đăng xuất
```
POST /api/auth/logout

Headers:
Authorization: Bearer {token}

Response (204 No Content):
```

## Cấu hình

### application.yaml

```yaml
jwt:
  secret: "your-secret-key-change-in-production"
  expiration: 86400000  # 24 hours in milliseconds

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db
    username: postgres
    password: postgres
```

### Environment Variables

Để tăng cường bảo mật, hãy sử dụng các biến môi trường:

```bash
export JWT_SECRET="your-very-long-secret-key-minimum-256-bits"
```

## Xác thực và Ủy quyền

### JWT Token

- **Thuật toán**: HS256 (HMAC with SHA-256)
- **Thời gian hết hạn**: 24 giờ (có thể điều chỉnh)
- **Được lưu trữ trong**: Authorization header với định dạng `Bearer {token}`

### Cách sử dụng Token

Gửi token trong mọi request đến các endpoint được bảo vệ:

```bash
curl -H "Authorization: Bearer eyJhbGc..." \
  http://localhost:8081/api/auth/me
```

## Cơ sở Dữ liệu

### User Table

```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL, -- ADMIN, LIBRARIAN, USER
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP
);
```

### Roles

- **ADMIN**: Quản trị viên hệ thống
- **LIBRARIAN**: Thủ thư
- **USER**: Người dùng bình thường

## Xử lý Lỗi

Dịch vụ trả về các mã lỗi HTTP tiêu chuẩn:

| Mã | Nguyên nhân | Giải pháp |
|----|-----------|---------|
| 201 | Đăng ký thành công | - |
| 200 | Đăng nhập thành công | - |
| 400 | Dữ liệu không hợp lệ | Kiểm tra định dạng request |
| 401 | Token không hợp lệ/hướng dẫn | Gửi token hợp lệ trong header |
| 409 | Email đã được đăng ký | Sử dụng email khác |
| 500 | Lỗi server | Liên hệ admin |

### Ví dụ Error Response

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Email Already Exists",
  "message": "Email user@example.com đã được đăng ký",
  "path": "/api/auth/register"
}
```

## Kiểm thử

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn test -Dgroups=integration
```

### Manual Testing với cURL

```bash
# Đăng ký
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"user@example.com",
    "password":"password123",
    "fullName":"Test User"
  }'

# Đăng nhập
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email":"user@example.com",
    "password":"password123"
  }'

# Lấy thông tin user
curl -X GET http://localhost:8081/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Bảo mật

### Best Practices

1. **Mật khẩu**:
   - Sử dụng BCrypt để hash mật khẩu
   - Tối thiểu 6 ký tự

2. **JWT Token**:
   - Sử dụng HTTPS trong production
   - Giữ secret key an toàn
   - Đặt thời gian hết hạn hợp lý

3. **Database**:
   - Sử dụng SSL connection string
   - Giới hạn quyền truy cập database
   - Định kỳ backup dữ liệu

## Chạy Dịch vụ

### Yêu cầu

- Java 21+
- PostgreSQL 12+
- Maven 3.6+

### Môi trường Development

```bash
# Từ thư mục auth-service
mvn clean install
mvn spring-boot:run
```

Dịch vụ sẽ khởi động trên: `http://localhost:8081`

### Docker

```bash
docker-compose up auth-service
```

## Tích hợp với các Dịch vụ Khác

### Api Gateway

Auth Service cần được đăng ký trong API Gateway để các dịch vụ khác có thể xác thực:

```yaml
routes:
  - id: auth-service
    uri: http://localhost:8081
    predicates:
      - Path=/api/auth/**
```

### Các Dịch vụ Khác

Các dịch vụ khác cần xác thực token bằng cách:

1. Gửi Authorization header đến Auth Service
2. Nhận user info từ response
3. Cho phép hoặc từ chối yêu cầu dựa trên role

## Phát Triển Tương Lai

- [ ] Refresh Token Support
- [ ] OAuth2 Integration
- [ ] Two-Factor Authentication (2FA)
- [ ] Email Verification
- [ ] Password Reset Flow
- [ ] Rate Limiting
- [ ] Audit Logging

## Liên Hệ & Hỗ Trợ

Để báo cáo lỗi hoặc yêu cầu tính năng, vui lòng tạo một issue trên GitHub.
