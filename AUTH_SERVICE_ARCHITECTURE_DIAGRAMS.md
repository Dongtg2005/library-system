# Auth Service - Architecture Diagrams

## System Overview

```
┌──────────────────────────────────────────────────────────────────────────┐
│                            CLIENT APPLICATION                             │
│                         (Web / Mobile Browser)                            │
└───────────────────────────────────┬──────────────────────────────────────┘
                                    │ HTTP/REST
                                    │ (Port 8080)
                    ┌───────────────▼────────────────┐
                    │    API GATEWAY (Zuul/Spring)   │
                    │   - Request Routing            │
                    │   - Load Balancing             │
                    │   - Rate Limiting              │
                    │   - Circuit Breaker            │
                    └───┬─────────────────────────┬──┘
                        │                         │
        ┌───────────────┴─────────────┬──────────┴─────────────────┬──────────┐
        │                             │                            │          │
        │                             │                            │          │
        ▼                             ▼                            ▼          ▼
    ┌────────┐              ┌──────────────┐           ┌────────────┐   ┌────────┐
    │Auth    │              │User Service  │           │Book Service│   │Borrow  │
    │Service │              │ Port 8082    │           │ Port 8083  │   │Service │
    │Port    │              │              │           │            │   │Port    │
    │8081    │              │              │           │            │   │8084    │
    └────┬───┘              └──────────────┘           └────────────┘   └────────┘
         │
         │ (All services use JWT from Auth Service)
         │
         └───────────────────────────────┐
                                         │
         ┌─────────────────────────────────────────────────────────┐
         │                     PostgreSQL Database                  │
         │  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐ │
         │  │  auth_db     │  │  users_db    │  │  books_db     │ │
         │  │              │  │              │  │               │ │
         │  │ - users      │  │ - users      │  │ - books       │ │
         │  │              │  │ - profile    │  │ - authors     │ │
         │  │              │  │ - history    │  │ - categories  │ │
         │  └──────────────┘  └──────────────┘  └───────────────┘ │
         │                                                         │
         └─────────────────────────────────────────────────────────┘
```

## Auth Service Internal Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          AUTH SERVICE (Port 8081)                        │
│                                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                         REST CONTROLLER                          │  │
│  │                      AuthController.java                         │  │
│  │                                                                  │  │
│  │  POST   /api/auth/register        POST  /api/auth/login        │  │
│  │  GET    /api/auth/me              POST  /api/auth/logout       │  │
│  │  GET    /api/auth/validate                                      │  │
│  └──────────┬───────────────────────────────────────────────┬──────┘  │
│             │                                               │           │
│  ┌──────────▼────────────────────────────────────────────────▼──────┐  │
│  │                    SERVICE LAYER                                 │  │
│  │              AuthenticationService.java                          │  │
│  │                                                                  │  │
│  │  - register(RegisterRequest)                                    │  │
│  │  - login(LoginRequest)                                          │  │
│  │  - validateToken(String)                                        │  │
│  │  - findByEmail(String)                                          │  │
│  └──────────┬───────────────────────────────────────────────┬──────┘  │
│             │                                               │           │
│  ┌──────────▼──────────────┐                 ┌──────────────▼──────┐  │
│  │   REPOSITORY LAYER      │                 │   UTILITY LAYER     │  │
│  │                         │                 │                     │  │
│  │ UserRepository.java     │                 │   JwtUtil.java      │  │
│  │                         │                 │                     │  │
│  │ - findByEmail()         │                 │ - generateToken()   │  │
│  │ - existsByEmail()       │                 │ - extractEmail()    │  │
│  │ - save()                │                 │ - isTokenValid()    │  │
│  │ - deleteAll()           │                 │ - extractClaim()    │  │
│  └────────────────────────┘                 └─────────────────────┘  │
│             │                                                         │
│  ┌──────────▼──────────────────────────────────────────────────────┐ │
│  │                       ENTITY LAYER                              │ │
│  │                                                                  │ │
│  │  User.java (JPA Entity)                                         │ │
│  │  ├── id: Long                                                   │ │
│  │  ├── email: String (unique)                                    │ │
│  │  ├── password: String (hashed)                                 │ │
│  │  ├── fullName: String                                          │ │
│  │  ├── role: Role (ADMIN, LIBRARIAN, USER)                      │ │
│  │  ├── enabled: Boolean                                          │ │
│  │  ├── createdAt: LocalDateTime                                  │ │
│  │  └── updatedAt: LocalDateTime                                  │ │
│  │                                                                  │ │
│  │  Implements: UserDetails, Serializable                          │ │
│  └──────────┬───────────────────────────────────────────────────────┘ │
│             │                                                         │
│  ┌──────────▼──────────────────────────────────────────────────────┐ │
│  │                 SECURITY LAYER (Filters)                        │ │
│  │                                                                  │ │
│  │  ┌──────────────────────────────────────────────────────────┐  │ │
│  │  │         JwtAuthenticationFilter.java                    │  │ │
│  │  │  (OncePerRequestFilter)                                │  │ │
│  │  │                                                        │  │ │
│  │  │  1. Extract token from Authorization header           │  │ │
│  │  │  2. Validate token signature & expiration            │  │ │
│  │  │  3. Load user from database                          │  │ │
│  │  │  4. Set SecurityContext                              │  │ │
│  │  └──────────────────────────────────────────────────────┘  │ │
│  │                                                              │ │
│  │  ┌──────────────────────────────────────────────────────┐  │ │
│  │  │        SecurityConfig.java                          │  │ │
│  │  │  ├── Enable Web Security                            │  │ │
│  │  │  ├── Configure HTTP Security                        │  │ │
│  │  │  ├── Register JWT Filter                            │  │ │
│  │  │  ├── Set Authorization Rules                        │  │ │
│  │  │  └── Password Encoder (BCrypt)                      │  │ │
│  │  └──────────────────────────────────────────────────────┘  │ │
│  │                                                              │ │
│  │  ┌──────────────────────────────────────────────────────┐  │ │
│  │  │   CustomUserDetailsService + EntryPoint             │  │ │
│  │  └──────────────────────────────────────────────────────┘  │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │              EXCEPTION HANDLING LAYER                        │ │
│  │                                                              │ │
│  │  GlobalExceptionHandler.java                               │ │
│  │  ├── EmailAlreadyExistsException (409)                    │ │
│  │  ├── InvalidCredentialsException (401)                    │ │
│  │  ├── MethodArgumentNotValidException (400)                │ │
│  │  └── Generic Exception (500)                              │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │              DATA TRANSFER OBJECTS (DTOs)                    │ │
│  │                                                              │ │
│  │  ├── RegisterRequest                                        │ │
│  │  ├── LoginRequest                                           │ │
│  │  └── AuthResponse                                           │ │
│  └──────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
                              │
                              │
                    ┌─────────▼──────────┐
                    │   PostgreSQL DB   │
                    │   (auth_db)       │
                    │                   │
                    │  users table      │
                    └───────────────────┘
```

## Request/Response Flow - Registration

```
┌──────────────┐
│    CLIENT    │
└──────┬───────┘
       │
       │ POST /api/auth/register
       │ {email, password, fullName}
       │
       ▼
┌────────────────────────────┐
│   AuthController.register  │
│                            │
│ - @Valid annotation check  │
└────────┬───────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ AuthenticationService.register()     │
│                                     │
│ 1. Check email exists               │
│    └─ throw EmailAlreadyExistsEx   │
│                                     │
│ 2. Create new User entity           │
│    └─ password = encode(password)   │
│                                     │
│ 3. Save user to database            │
│    └─ userRepository.save()        │
│                                     │
│ 4. Generate JWT token               │
│    └─ jwtUtil.generateToken()      │
│                                     │
│ 5. Build AuthResponse               │
│    └─ userId, email, token, etc   │
└────────┬────────────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│  Return AuthResponse (201)   │
│                              │
│ {                            │
│   "userId": 1,              │
│   "email": "user@ex.com",  │
│   "accessToken": "...",    │
│   "expiresIn": 86400000    │
│ }                            │
└──────────────────────────────┘
         │
         ▼
┌──────────────┐
│    CLIENT    │
└──────────────┘
```

## Request/Response Flow - Login & Token Use

```
LOGIN FLOW:
┌──────────────┐
│    CLIENT    │
└──────┬───────┘
       │
       │ POST /api/auth/login
       │ {email, password}
       │
       ▼
┌────────────────────────┐
│ AuthenticationService  │
│    .login()            │
│                        │
│ 1. Find user by email  │
│ 2. Verify password     │
│ 3. Generate JWT token  │
│ 4. Return token        │
└────┬───────────────────┘
     │
     ▼
┌──────────────────────┐
│  AuthResponse (200)  │
│  accessToken: "..."  │
└──────────────────────┘
     │                    Stored in client (localStorage/sessionStorage)
     │
     │
     └──────────┬────────────────────► [Stored in Browser]

SUBSEQUENT REQUESTS WITH TOKEN:
┌──────────────────────────────┐
│  CLIENT                      │
│  GET /api/auth/me            │
│  Authorization: Bearer {JWT} │
└──────────┬───────────────────┘
           │
           ▼
    ┌──────────────────────────────┐
    │RequestContext in application │
    │                              │
    │ Extract "Bearer ___"         │
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  JwtAuthenticationFilter     │
    │                              │
    │ 1. Extract JWT               │
    │ 2. Validate signature        │
    │ 3. Check expiration          │
    │ 4. Extract claims            │
    │ 5. Load user from DB         │
    │ 6. Set SecurityContext       │
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  AuthController.getCurrentUser
    │                              │
    │ Get from SecurityContext     │
    │ Return user info             │
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  Return User (200)           │
    │  {userId, email, role, ...}  │
    └──────────────────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │      CLIENT                  │
    │  Process user data           │
    └──────────────────────────────┘
```

## Database Schema

```
┌─────────────────────────────────────────────────────────────┐
│                        USERS TABLE                           │
│                                                              │
│ Column Name    │ Type              │ Constraints            │
├────────────────┼───────────────────┼────────────────────────┤
│ id             │ BIGSERIAL         │ PRIMARY KEY            │
│ email          │ VARCHAR(255)      │ NOT NULL, UNIQUE       │
│ password       │ VARCHAR(255)      │ NOT NULL (hashed)      │
│ full_name      │ VARCHAR(255)      │ NOT NULL               │
│ role           │ VARCHAR(50)       │ NOT NULL               │
│                │                   │ (ADMIN/LIBRARIAN/USER) │
│ enabled        │ BOOLEAN           │ NOT NULL, DEFAULT TRUE │
│ created_at     │ TIMESTAMP         │ DEFAULT NOW()          │
│ updated_at     │ TIMESTAMP         │ DEFAULT NULL           │
│                                                              │
│ INDEXES:                            │
│ - idx_users_email (email)                                  │
│ - PRIMARY KEY (id)                                         │
└─────────────────────────────────────────────────────────────┘
```

## JWT Token Structure

```
┌──────────────────────────────────────────────────────────────┐
│                    JWT TOKEN (3 parts)                       │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  Header                                                      │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ {                                                      │ │
│  │   "alg": "HS256",    /* Algorithm */                  │ │
│  │   "typ": "JWT"       /* Token Type */                 │ │
│  │ }                                                      │ │
│  └────────────────────────────────────────────────────────┘ │
│                          •                                  │
│  Payload                                                     │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ {                                                      │ │
│  │   "sub": "user@example.com",  /* Subject */           │ │
│  │   "iat": 1704067200,           /* Issued At */        │ │
│  │   "exp": 1704153600            /* Expiration */       │ │
│  │ }                                                      │ │
│  └────────────────────────────────────────────────────────┘ │
│                          •                                  │
│  Signature                                                   │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ HMACSHA256(                                            │ │
│  │   base64url(header) + "." + base64url(payload),       │ │
│  │   secret_key                                          │ │
│  │ )                                                      │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  Final Token:                                               │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.                 │ │
│  │ eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzA0MDY │ │
│  │ 3MjAwLCJleHAiOjE3MDQxNTM2MDB9.                         │ │
│  │ FsE1Qc7mKNqCQxZxT-Hl6eF09KQTg9JzK3FxfzKjVVs         │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

## Error Handling Flow

```
┌──────────────┐
│    REQUEST   │
└──────┬───────┘
       │
       ▼
┌─────────────────────────────┐
│  Exception occurs during     │
│  processing                  │
└──────┬──────────────────────┘
       │
       ▼
┌──────────────────────────────────────────┐
│  Caught by GlobalExceptionHandler        │
│                                          │
│  @ExceptionHandler annotated methods     │
└──────┬───────────────┬────────────────────┘
       │               │
       │               │
   ┌───▼────┐      ┌───▼──────────────┐
   │ Email  │      │Invalid           │
   │Exists  │      │Credentials       │
   │409     │      │401               │
   └────────┘      └──────────────────┘
       │                   │
       │                   │
   ┌───▼───────────────────▼──────────────┐
   │                                      │
   │ Build ErrorResponse                  │
   │ {                                    │
   │   status: 409,                       │
   │   error: "Email Already Exists",     │
   │   message: "...",                    │
   │   path: "/api/auth/register"         │
   │ }                                    │
   └──────┬───────────────────────────────┘
          │
          ▼
   Return HTTP Response
   with JSON body
```

---

**Diagrams Version**: 1.0 | **Last Updated**: January 2024
