# Quick Reference: Auth Service

## 📁 Files Created/Modified

### Core Implementation Files

| File | Type | Purpose |
|------|------|---------|
| `entity/User.java` | Entity | User database model |
| `repository/UserRepository.java` | Repository | Database access |
| `dto/RegisterRequest.java` | DTO | Registration input |
| `dto/LoginRequest.java` | DTO | Login input |
| `dto/AuthResponse.java` | DTO | Auth response |
| `service/AuthenticationService.java` | Service | Business logic |
| `controller/AuthController.java` | Controller | REST endpoints |
| `util/JwtUtil.java` | Utility | JWT operations |
| `security/SecurityConfig.java` | Config | Spring Security |
| `security/JwtAuthenticationFilter.java` | Filter | JWT validation |
| `security/CustomUserDetailsService.java` | Service | User loading |
| `security/JwtAuthenticationEntryPoint.java` | Handler | Auth errors |
| `exception/EmailAlreadyExistsException.java` | Exception | Email error |
| `exception/InvalidCredentialsException.java` | Exception | Auth error |
| `exception/GlobalExceptionHandler.java` | Handler | Exception mapping |
| `exception/ErrorResponse.java` | DTO | Error format |

### Configuration Files

| File | Purpose |
|------|---------|
| `application.yaml` | Main configuration |
| `application-test.yaml` | Test configuration |
| `pom.xml` | Dependencies |
| `README.md` | Service documentation |

### Test Files

| File | Purpose |
|------|---------|
| `AuthenticationServiceTest.java` | Unit tests |

### Documentation Files (Root)

| File | Purpose |
|------|---------|
| `MICROSERVICES_ARCHITECTURE.md` | System architecture |
| `AUTH_SERVICE_IMPLEMENTATION.md` | Implementation details |

## 🔑 Key Classes & Methods

### AuthenticationService
```java
public AuthResponse register(RegisterRequest request)
public AuthResponse login(LoginRequest request)
public User validateToken(String token)
public User findByEmail(String email)
```

### AuthController
```java
@PostMapping("/register") - Register user
@PostMapping("/login") - Login user
@GetMapping("/me") - Current user info
@GetMapping("/validate") - Validate token
@PostMapping("/logout") - Logout user
```

### JwtUtil
```java
public String generateToken(UserDetails userDetails)
public String extractEmail(String token)
public boolean isTokenValid(String token, UserDetails userDetails)
public Long getExpirationTime()
```

## 🌐 API Endpoints

```
┌─────────────────────────────────────────┐
│ PUBLIC ENDPOINTS (No token required)    │
├─────────────────────────────────────────┤
│ POST   /api/auth/register               │
│ POST   /api/auth/login                  │
│ GET    /api/auth/validate?token=xxx     │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ PROTECTED ENDPOINTS (Token required)    │
├─────────────────────────────────────────┤
│ GET    /api/auth/me                     │
│ POST   /api/auth/logout                 │
└─────────────────────────────────────────┘
```

## 🔐 Authentication Flow

```
1. Register
   POST /api/auth/register
   → Validate data
   → Check email exists
   → Hash password with BCrypt
   → Save user to DB
   → Generate JWT token
   → Return token + user info

2. Login
   POST /api/auth/login
   → Validate login data
   → Find user by email
   → Compare password hash
   → Generate JWT token
   → Return token + user info

3. Use Token
   GET /api/auth/me
   Headers: Authorization: Bearer {token}
   → JwtAuthenticationFilter intercepts
   → Extract & validate token
   → Load user from database
   → Set authentication context
   → Allow request to proceed

4. Validate Token
   GET /api/auth/validate?token={token}
   → Extract email from token
   → Verify signature
   → Check expiration
   → Return user info
```

## 📊 Data Models

### User Entity
```java
Long id
String email (unique)
String password (hashed)
String fullName
Role role (ADMIN, LIBRARIAN, USER)
Boolean enabled
LocalDateTime createdAt
LocalDateTime updatedAt
```

### AuthResponse
```java
Long userId
String email
String fullName
String role
String accessToken (JWT)
String tokenType ("Bearer")
Long expiresIn (milliseconds)
```

## 🔧 Configuration

### JWT Settings
```yaml
jwt:
  secret: ${JWT_SECRET}        # Environment variable
  expiration: 86400000         # 24 hours
```

### Database
```yaml
datasource:
  url: jdbc:postgresql://localhost:5432/auth_db
  username: postgres
  password: postgres
```

### Server
```yaml
server:
  port: 8081
```

## 🚀 Quick Start

### 1. Build
```bash
cd auth-service
mvn clean install
```

### 2. Run
```bash
mvn spring-boot:run
```

### 3. Test - Register
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
  }'
```

### 4. Test - Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 5. Test - Get User (copy token from login response)
```bash
curl -X GET http://localhost:8081/api/auth/me \
  -H "Authorization: Bearer eyJhbGc..."
```

## ⚠️ Important Notes

1. **JWT Secret**: Change in production! Use environment variable
2. **Password**: Minimum 6 characters, hashed with BCrypt
3. **Token Expiration**: 24 hours (configurable)
4. **Database**: PostgreSQL required
5. **Port**: Service runs on port 8081

## 🐛 Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| `Connection refused` | Check PostgreSQL is running |
| `Email already exists` | Use different email |
| `Invalid credentials` | Check email/password |
| `Token expired` | Login again for new token |
| `401 Unauthorized` | Include valid Authorization header |

## 📚 References

- **Spring Security**: https://spring.io/projects/spring-security
- **JWT**: https://jwt.io
- **JJWT**: https://github.com/jwtk/jjwt
- **Spring Boot**: https://spring.io/projects/spring-boot

## 📞 Support

For issues or questions:
1. Check exception message and HTTP status
2. Review log output
3. Verify configuration
4. Check database connectivity

---

**Quick Status Check:**
```bash
# Health check
curl http://localhost:8081/actuator/health

# Database check
psql -h localhost -U postgres -d auth_db -c "SELECT COUNT(*) FROM users;"
```

**Version**: 1.0.0 | **Status**: ✅ Ready for Production
