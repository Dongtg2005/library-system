# BÁO CÁO TỔNG QUÁT DỰ ÁN LIBRARY MANAGEMENT SYSTEM
> **Phiên bản báo cáo:** 1.0 | **Ngày phân tích:** 16/04/2026  
> **Mục tiêu:** Phục vụ tiếp tục phát triển & mở rộng dự án

---

## 1. TỔNG QUAN DỰ ÁN

### Tên dự án & Mục đích chính
- **Tên:** Library Management System (LMS)
- **Group ID:** `com.lms` | **Artifact:** `library-system` | **Version:** `1.0.0`
- **Mục đích:** Hệ thống quản lý thư viện toàn diện — phục vụ độc giả tra cứu/mượn sách và thủ thư/admin quản trị kho sách, người dùng, phiếu mượn.

### Loại ứng dụng
**Full-stack Web Application** — kiến trúc Monolith triển khai bằng Docker Compose (4 container: Backend · Frontend · PostgreSQL · Redis).

### Ngôn ngữ lập trình & Framework

| Tầng | Ngôn ngữ | Framework |
|------|----------|-----------|
| Backend | Java 21 | Spring Boot 3.3.5 |
| Frontend | JavaScript (ES2022+) | React 18.2.0 (CRA) |
| Database | SQL | PostgreSQL 16 |
| Cache | — | Redis 7 |
| Proxy | — | Nginx (Alpine) |

### Các thư viện / Dependency quan trọng

**Backend (pom.xml):**
| Dependency | Phiên bản | Vai trò |
|---|---|---|
| `spring-boot-starter-web` | 3.3.5 | REST API |
| `spring-boot-starter-data-jpa` | 3.3.5 | ORM / Hibernate |
| `spring-boot-starter-data-redis` | 3.3.5 | Cache layer |
| `spring-boot-starter-security` | 3.3.5 | Xác thực & phân quyền |
| `spring-boot-starter-validation` | 3.3.5 | Bean Validation |
| `spring-boot-starter-actuator` | 3.3.5 | Health check / Metrics |
| `jjwt-api/impl/jackson` | 0.12.3 | JWT token |
| `postgresql` | 42.7.3 | JDBC Driver |
| `mapstruct` | 1.5.5.Final | DTO mapping |
| `lombok` | 1.18.34 | Code generation |
| `commons-lang3` | (managed) | Utility |
| `springdoc-openapi-starter-webmvc-ui` | 2.0.2 | Swagger UI |
| `hypersistence-utils-hibernate-63` | 3.7.0 | JSONB support cho Hibernate |

**Frontend (package.json):**
| Dependency | Phiên bản | Vai trò |
|---|---|---|
| `react` / `react-dom` | 18.2.0 | UI framework |
| `react-router-dom` | 6.11.0 | Client-side routing |
| `axios` | 1.4.0 | HTTP client (có nhưng hầu hết dùng fetch) |
| `@headlessui/react` | 2.2.10 | Accessible UI components |
| `@heroicons/react` | 2.2.0 | Icon library |
| `recharts` | 3.8.1 | Biểu đồ thống kê |
| `tailwindcss` | 3.4.13 | CSS framework |

### Môi trường chạy
- **JDK:** Java 21 (LTS)
- **Maven:** 3.x (dùng Maven Wrapper `mvnw`)
- **Node.js:** >= 16.x (CRA — react-scripts 5.0.1)
- **Docker / Docker Compose:** Bắt buộc khi chạy full stack

---

## 2. KIẾN TRÚC HỆ THỐNG

### Mô hình kiến trúc
**Monolithic Layered Architecture (4 tầng rõ ràng)** kết hợp một số nguyên tắc của Domain-Driven Design (DDD).

```
╔══════════════════════════════════════════════════════════╗
║                     CLIENT TIER                          ║
║   React 18 (SPA) ──► Nginx Reverse Proxy                ║
╚═══════════════════════════╦══════════════════════════════╝
                            ║ HTTP/REST (JSON)
╔═══════════════════════════╩══════════════════════════════╗
║              PRESENTATION LAYER (Spring MVC)             ║
║  AuthController · BookController · BorrowController      ║
║  UserController · UnifiedExceptionHandler                ║
╠══════════════════════════════════════════════════════════╣
║              APPLICATION LAYER (Services)                ║
║  AuthenticationService · BookManagementService           ║
║  BookSearchService · BorrowManagementService             ║
║  UserManagementService                                   ║
╠══════════════════════════════════════════════════════════╣
║              DOMAIN LAYER (Entities + Repos)             ║
║  Book · User · BorrowRecord · Fine · Reservation ...     ║
║  BookRepository · BorrowRecordRepository ...             ║
║  BookSpecification (JPA Spec)                            ║
╠══════════════════════════════════════════════════════════╣
║           INFRASTRUCTURE LAYER (Cross-cutting)          ║
║  JwtUtil · JwtAuthenticationFilter                       ║
║  SecurityConfig · CustomUserDetailsService               ║
╚═══════════════════════════╦══════════════════════════════╝
                            ║
           ┌────────────────┼────────────────┐
           ▼                ▼                ▼
      PostgreSQL 16      Redis 7         Spring Actuator
      (Primary DB)      (Cache)         (Metrics / Health)
```

### Cách các module giao tiếp
- **Frontend → Backend:** REST API qua Nginx reverse proxy (path `/api/v1/*` được proxy đến backend port 8080).
- **Controller → Service:** Dependency Injection (Spring IoC).
- **Service → Repository:** Spring Data JPA (Hibernate ORM).
- **Service → Cache:** `@Cacheable` Spring Cache → Redis.
- **Security Filter:** `JwtAuthenticationFilter` chặn mọi request, xác thực Bearer token trước khi đến Controller.

### Design Patterns sử dụng

| Pattern | Nơi áp dụng |
|---|---|
| **Repository Pattern** | `BookRepository`, `BorrowRecordRepository`, ... (Spring Data JPA) |
| **Specification Pattern** | `BookSpecification` — dynamic query cho search & autocomplete |
| **Builder Pattern** | Tất cả Entity (`@Builder` Lombok) và DTO (`BorrowResponse.from()`) |
| **Filter Chain** | `JwtAuthenticationFilter` → `SecurityFilterChain` |
| **Global Exception Handler** | `UnifiedExceptionHandler` (`@RestControllerAdvice`) |
| **Strategy (ngầm)** | `BorrowPolicy` điều chỉnh nghiệp vụ theo loại thành viên |
| **Observer (ngầm)** | `@PrePersist` / `@PreUpdate` hooks trên Entity |

---

## 3. CẤU TRÚC THƯ MỤC CHI TIẾT

```
library-system/                         ← Root project
├── .env.example                        ← Biến môi trường mẫu
├── docker-compose.yaml                 ← Orchestration 4 services
├── Dockerfile                          ← Build image backend (multi-stage)
├── pom.xml                             ← Maven dependencies backend
├── mvnw / mvnw.cmd                     ← Maven Wrapper
├── nginx/                              ← Cấu hình Nginx proxy
│
├── src/main/
│   ├── java/com/lms/library/
│   │   ├── LibrarySystemApplication.java   ← Entry point (main())
│   │   │
│   │   ├── application/
│   │   │   ├── dto/                        ← Request/Response DTOs
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── BookCreateRequest.java
│   │   │   │   ├── BookResponse.java
│   │   │   │   ├── BookUpdateRequest.java
│   │   │   │   ├── BorrowResponse.java
│   │   │   │   ├── CreateBorrowRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── ReturnRequest.java
│   │   │   │   ├── ReturnResponse.java
│   │   │   │   ├── UserCreateRequest.java
│   │   │   │   ├── UserResponse.java
│   │   │   │   └── UserUpdateRequest.java
│   │   │   └── service/                    ← Business logic
│   │   │       ├── AuthenticationService.java
│   │   │       ├── BookManagementService.java
│   │   │       ├── BookSearchService.java
│   │   │       ├── BorrowManagementService.java
│   │   │       └── UserManagementService.java
│   │   │
│   │   ├── domain/
│   │   │   ├── entity/                     ← JPA Entities (domain model)
│   │   │   │   ├── Book.java               ← Sách (với domain methods)
│   │   │   │   ├── BorrowRecord.java       ← Phiếu mượn
│   │   │   │   ├── BorrowPolicy.java       ← Chính sách mượn theo role
│   │   │   │   ├── Fine.java               ← Phí phạt
│   │   │   │   ├── User.java               ← Người dùng
│   │   │   │   ├── Role.java               ← Vai trò
│   │   │   │   ├── UserProfile.java        ← Hồ sơ mở rộng
│   │   │   │   ├── Category.java           ← Thể loại sách
│   │   │   │   ├── Tag.java                ← Nhãn sách
│   │   │   │   ├── Reservation.java        ← Đặt trước sách
│   │   │   │   ├── Notification.java       ← Thông báo
│   │   │   │   ├── NotificationPreference.java
│   │   │   │   ├── BookReview.java         ← Đánh giá sách
│   │   │   │   ├── ReviewVote.java
│   │   │   │   ├── ReadingHistory.java     ← Lịch sử đọc
│   │   │   │   ├── UserActivity.java       ← Audit log
│   │   │   │   ├── UserFavorite.java       ← Sách yêu thích
│   │   │   │   ├── UserWishlist.java       ← Danh sách muốn đọc
│   │   │   │   ├── SystemSetting.java      ← Cài đặt hệ thống
│   │   │   │   ├── AnalyticsEvent.java     ← Sự kiện phân tích
│   │   │   │   ├── PopularBook.java        ← Cache sách phổ biến
│   │   │   │   └── RefreshToken.java
│   │   │   ├── exception/                  ← Domain exceptions (12 loại)
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── BorrowLimitExceededException.java
│   │   │   │   ├── BookNotAvailableException.java
│   │   │   │   ├── BookAlreadyBorrowedException.java
│   │   │   │   ├── OutstandingFineException.java
│   │   │   │   ├── CardExpiredException.java
│   │   │   │   └── ... (6 loại khác)
│   │   │   └── repository/                 ← Spring Data JPA Repos
│   │   │       ├── BookRepository.java
│   │   │       ├── BorrowRecordRepository.java
│   │   │       ├── BorrowPolicyRepository.java
│   │   │       ├── FineRepository.java
│   │   │       ├── UserRepository.java
│   │   │       ├── UserProfileRepository.java
│   │   │       ├── RoleRepository.java
│   │   │       └── spec/
│   │   │           └── BookSpecification.java  ← Dynamic JPA Specs
│   │   │
│   │   ├── infrastructure/security/
│   │   │   ├── SecurityConfig.java         ← Spring Security config
│   │   │   ├── JwtUtil.java                ← JWT generate/validate
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── CustomUserDetailsService.java
│   │   │
│   │   └── presentation/
│   │       ├── controller/
│   │       │   ├── AuthController.java     ← /api/v1/auth/*
│   │       │   ├── BookController.java     ← /api/v1/books/*
│   │       │   ├── BorrowController.java   ← /api/v1/borrows/*
│   │       │   ├── UserController.java     ← /api/v1/users/*
│   │       │   └── ControllerHelper.java   ← Lấy user từ SecurityContext
│   │       └── exception/
│   │           └── UnifiedExceptionHandler.java ← @RestControllerAdvice
│   │
│   └── resources/
│       ├── application.yml                 ← Cấu hình Spring Boot
│       └── db/
│           └── schema.sql                  ← Schema + Seed data đầy đủ
│
└── frontend/
    ├── Dockerfile                          ← Build image frontend (Nginx)
    ├── nginx.conf                          ← Nginx config (proxy + gzip)
    ├── package.json                        ← Dependencies frontend
    ├── tailwind.config.js
    └── src/
        ├── App.js                          ← Root component + Routing
        ├── index.js                        ← ReactDOM.render
        ├── lib/
        │   └── api.js                      ← HTTP client (fetch wrapper)
        ├── context/
        │   ├── AuthContext.js              ← Auth state (JWT, user info)
        │   ├── ThemeContext.js             ← Dark/Light mode
        │   └── ToastContext.js             ← Toast notification
        ├── hooks/
        │   └── useDebounce.js              ← Debounce search input
        ├── components/                     ← Reusable UI components
        │   ├── DashboardLayout.js          ← Layout cho Admin/Librarian
        │   ├── LibraryLayout.js            ← Layout cho User (public)
        │   ├── Sidebar.js · Login.js · Modal.js · Toast.js
        │   ├── BookTable.js · UserTable.js
        │   ├── BorrowManagement.js · BorrowConfirmModal.js
        │   └── RoleBasedDashboard.js
        ├── pages/                          ← Route-level page components
        │   ├── HomePage.js                 ← Trang chủ (public)
        │   ├── BooksPage.js               ← Danh sách sách (public)
        │   ├── BookDetailPage.js           ← Chi tiết sách
        │   ├── AuthPage.js                 ← Login / Register
        │   ├── MyBorrowsPage.js            ← Sách đang mượn (user)
        │   ├── HistoryPage.js              ← Lịch sử mượn
        │   ├── FavoritesPage.js            ← Sách yêu thích
        │   ├── ProfilePage.js              ← Trang hồ sơ cá nhân
        │   ├── ReviewsPage.js              ← Đánh giá sách
        │   ├── BorrowBookPage.js           ← Form mượn sách
        │   ├── CategoriesPage.js           ← Thể loại (public)
        │   ├── AdminDashboard.js · LibrarianDashboard.js
        │   ├── BookPage.js                 ← Admin: quản lý sách
        │   ├── BorrowPage.js               ← Admin/Librarian: duyệt mượn
        │   └── UserPage.js                 ← Admin: quản lý user
        └── data/
            └── mockData.js                 ← Dữ liệu mẫu (dùng cho dev)
```

---

## 4. TÍNH NĂNG ĐÃ IMPLEMENT

### 4.1 Quản lý danh mục
| Tính năng | Trạng thái | Ghi chú |
|---|---|---|
| Thêm sách (Admin/Librarian) | ✅ Hoàn chỉnh | `BookController.createBook()` + validate ISBN |
| Sửa sách | ✅ Hoàn chỉnh | `updateBook()` — lưu ý: category update đang bị comment TODO |
| Xóa sách (soft delete) | ✅ Hoàn chỉnh | Archive (`book.archive()`) — không xóa vật lý |
| Tìm kiếm sách (keyword) | ✅ Hoàn chỉnh | `BookSearchService.advancedSearch()` + JPA Spec |
| Lọc theo thể loại, trạng thái | ✅ Hoàn chỉnh | Lọc theo `category`, `status`, `isbn` |
| Autocomplete search | ✅ Hoàn chỉnh | `/api/v1/books/autocomplete` — top 5, prefix match |
| Quản lý Tác giả/NXB riêng biệt | ❌ Chưa có | Author và Publisher là các trường văn bản thuần, không có entity riêng |
| Quản lý thể loại phân cấp | ⚠️ Một phần | Bảng `categories` có `parent_id`, nhưng chưa có API quản lý CRUD |
| Quản lý kho / vị trí kệ sách | ❌ Chưa có | Không có trường shelf/location |
| Quản lý bản sao sách riêng lẻ | ⚠️ Một phần | `book_copy_id` trong `borrow_records` nhưng không có entity `BookCopy` |
| Upload ảnh bìa sách | ✅ Hoàn chỉnh | Đã có FileStorageService, API upload file/URL và xóa ảnh bìa (`cover_image_url`) |

### 4.2 Quản lý độc giả / thành viên
| Tính năng | Trạng thái | Ghi chú |
|---|---|---|
| Đăng ký tài khoản | ✅ Hoàn chỉnh | `AuthController.register()` — tự động gán role USER |
| Admin tạo user | ✅ Hoàn chỉnh | `UserController.createUser()` |
| Cập nhật thông tin user | ✅ Hoàn chỉnh | `UserController.updateUser()` |
| Phân quyền (role) | ✅ Hoàn chỉnh | GUEST, USER, LIBRARIAN, ADMIN |
| Khóa/mở tài khoản | ✅ Hoàn chỉnh | Trường `status`: ACTIVE / INACTIVE / SUSPENDED |
| Lịch sử mượn trả | ✅ Hoàn chỉnh | `BorrowController.getMemberBorrowHistory()` |
| Thẻ thư viện / mã độc giả | ⚠️ Một phần | Có `card_expiry_date` và `membership_level` trong `user_profiles`, không có mã thẻ vật lý / QR |
| Hồ sơ mở rộng (UserProfile) | ✅ Hoàn chỉnh | Điểm thưởng, mức thành viên, fines tracking |
| Email xác thực | ⚠️ Một phần | Trường `email_verified` tồn tại nhưng logic xác thực qua email chưa implement |

### 4.3 Nghiệp vụ mượn / trả
| Tính năng | Trạng thái | Ghi chú |
|---|---|---|
| Tạo phiếu mượn | ✅ Hoàn chỉnh | `BorrowManagementService.createBorrowing()` — trạng thái PENDING_APPROVAL |
| Phê duyệt / từ chối phiếu mượn | ✅ Hoàn chỉnh | Librarian: `approveBorrowRequest()` / `rejectBorrowRequest()` |
| Xử lý trả sách | ✅ Hoàn chỉnh | `processReturn()` — tự động tính phí phạt |
| Tính phí phạt quá hạn | ✅ Hoàn chỉnh | `calculateOverdueFine()` — theo chính sách mượn |
| Gia hạn mượn sách | ✅ Hoàn chỉnh | `extendLoan()` — mặc định +7 ngày, giới hạn số lần |
| Đặt trước / giữ chỗ sách | ⚠️ Một phần | Bảng `reservations` có schema đầy đủ, nhưng **API chưa implement** |
| Kiểm tra phí phạt trước mượn | ✅ Hoàn chỉnh | Validate `OutstandingFineException` |
| Kiểm tra thẻ hết hạn | ✅ Hoàn chỉnh | Validate `CardExpiredException` |
| Kiểm tra giới hạn mượn | ✅ Hoàn chỉnh | Theo `BorrowPolicy.maxBooksAllowed` |
| Thanh toán phí phạt | ⚠️ Một phần | Bảng `fines` hoàn chỉnh, nhưng API thanh toán chưa có |

### 4.4 Tìm kiếm & Tra cứu
| Tính năng | Trạng thái | Ghi chú |
|---|---|---|
| Tìm theo tên sách / tác giả | ✅ Hoàn chỉnh | JPA Spec `LIKE %keyword%` |
| Tìm theo ISBN | ✅ Hoàn chỉnh | Exact match |
| Lọc theo thể loại | ✅ Hoàn chỉnh | Join bảng categories |
| Lọc theo trạng thái | ✅ Hoàn chỉnh | AVAILABLE / OUT_OF_STOCK / ARCHIVED |
| Autocomplete dropdown | ✅ Hoàn chỉnh | Prefix match, top 5, cached Redis |
| Kiểm tra sách có sẵn | ✅ Hoàn chỉnh | `book.isAvailable()` |
| Phân trang + sắp xếp | ✅ Hoàn chỉnh | Spring Pageable, mặc định sort title ASC |
| Tìm kiếm Full-text (GIN index) | ⚠️ Một phần | Index GIN đã khai báo trong SQL, chưa dùng PostgreSQL FTS trong code |

### 4.5 Báo cáo & Thống kê
| Tính năng | Trạng thái | Ghi chú |
|---|---|---|
| Dashboard biểu đồ (Frontend) | ⚠️ Một phần | `recharts` được cài, có `AdminDashboard.js` và `LibrarianDashboard.js` nhưng dùng `mockData.js` — chưa gọi API thực |
| Sách mượn nhiều nhất | ⚠️ Một phần | Bảng `popular_books` có schema; chưa có API |
| Độc giả nợ quá hạn | ⚠️ Một phần | Có thể tính được từ `borrow_records` nhưng chưa có API report |
| Thống kê theo khoảng thời gian | ❌ Chưa có | Không có API nào hỗ trợ date-range reporting |
| Xuất PDF / Excel | ❌ Chưa có | Không có dependency xuất file |

### 4.6 Phân quyền & Xác thực
| Tính năng | Trạng thái | Ghi chú |
|---|---|---|
| Đăng nhập JWT | ✅ Hoàn chỉnh | Access token 7 ngày; BCrypt hash |
| Đăng xuất | ✅ Hoàn chỉnh | Clear SecurityContext (stateless) |
| Refresh Token | ⚠️ Một phần | Bảng `refresh_tokens` trong DB, chưa có API `/api/v1/auth/refresh` |
| Role-based Access (RBAC) | ✅ Hoàn chỉnh | `@PreAuthorize` theo method; `RequireRole` trên Frontend |
| Dark/Light mode | ✅ Hoàn chỉnh | `ThemeContext` + Tailwind dark class |
| Khóa tài khoản sau đăng nhập sai | ⚠️ Một phần | Trường `failed_login_attempts` và `locked_until` trong DB, logic chưa implement trong Service |
| Xem thông tin tự mình | ✅ Hoàn chỉnh | `GET /api/v1/auth/me` |

---

## 5. LUỒNG XỬ LÝ NGHIỆP VỤ CHI TIẾT

### 5.1 Luồng MƯỢN SÁCH

```
[User] → BorrowBookPage.js (Frontend)
    │ POST /api/v1/borrows  (body: {bookId, conditionOnBorrow, notes})
    │ Headers: Authorization: Bearer <token>
    ▼
[JwtAuthenticationFilter.java]
    │ Xác thực JWT token, load SecurityContext
    ▼
[BorrowController.java → createBorrowing()]
    │ Lấy userId từ SecurityContext qua ControllerHelper
    │ Nhận memberType từ query param (default: USER)
    ▼
[BorrowManagementService.java → createBorrowing()]
    ├─ 1. Tìm UserProfile → userProfileRepository.findByUserId()
    │      → Nếu không tìm thấy: ResourceNotFoundException
    ├─ 2. Tìm Book → bookRepository.findById(request.getBookId())
    │      → Nếu không tìm thấy: ResourceNotFoundException
    ├─ 3. Tìm BorrowPolicy → borrowPolicyRepository.findAllByMemberType()
    │      → Nếu không có policy: PolicyNotFoundException
    ├─ 4. Validate thẻ hết hạn → userProfile.isCardExpired()
    │      → Nếu hết hạn: CardExpiredException
    ├─ 5. Validate phí phạt tồn đọng → userProfile.hasOutstandingFines()
    │      → Nếu có fine: OutstandingFineException
    ├─ 6. Validate giới hạn mượn → borrowRecordRepository.countByMemberIdAndStatus()
    │      → Nếu vượt giới hạn: BorrowLimitExceededException
    ├─ 7. Validate sách còn hàng → book.isAvailable()
    │      → Nếu hết hàng: BookNotAvailableException
    ├─ 8. Validate chưa mượn cuốn này → borrowRecordRepository.findByMemberIdAndBookId()
    │      → Nếu đang mượn: BookAlreadyBorrowedException
    ├─ 9. Tạo BorrowRecord (status = PENDING_APPROVAL)
    │      dueDate = now + policy.loanPeriodDays
    │      → borrowRecordRepository.save()
    ├─ 10. Giảm availableQty → book.borrowBook() → bookRepository.save()
    ├─ 11. Tăng currentBorrowedCount → userProfile.incrementBorrowedCount()
    └─ 12. Tính phí phạt quá hạn hiện có (calculateOverdueFines)
           Return: BorrowResponse (HTTP 201)

[Librarian/Admin] → BorrowPage.js → PUT /api/v1/borrows/{id}/approve
[BorrowManagementService.java → approveBorrowRequest()]
    → Đổi status: PENDING_APPROVAL → ACTIVE
    → Gán librarianId
```

### 5.2 Luồng TRẢ SÁCH & TÍNH PHÍ PHẠT

```
[User/Librarian] → POST /api/v1/borrows/return
    body: {borrowRecordId, conditionOnReturn, returnNotes}
    ▼
[BorrowController.java → processReturn()]
    │ Lấy memberId từ SecurityContext
    ▼
[BorrowManagementService.java → processReturn()]
    ├─ 1. Tìm BorrowRecord → borrowRecordRepository.findById()
    ├─ 2. Kiểm tra record thuộc về member → record.getMemberId().equals(memberId)
    │      → Nếu không khớp: ForbiddenOperationException
    ├─ 3. Tính phí phạt quá hạn → calculateOverdueFine(record)
    │      overdueDays = ChronoUnit.DAYS.between(dueDate, now)
    │      fineAmount = policy.finePerDay × overdueDays (có max_fine cap)
    ├─ 4. Trả sách về kho → book.returnBook() → bookRepository.save()
    │      availableQty++ ; borrowedQuantity-- ; status → AVAILABLE nếu cần
    ├─ 5. Giảm currentBorrowedCount → userProfile.decrementBorrowedCount()
    ├─ 6. Đánh dấu trả → record.returnBook(conditionOnReturn)
    │      returnDate = now ; borrowStatus = RETURNED
    └─ 7. Nếu có fine:
           → Tạo Fine record (status = PENDING) → fineRepository.save()
           → Tăng userProfile.outstandingFines
    Return: ReturnResponse {borrowRecordId, returnDate, overdueFine}
```

### 5.3 Luồng ĐĂNG KÝ ĐỘC GIẢ MỚI

```
[Guest] → AuthPage.js (initialMode="register")
    │ POST /api/v1/auth/register
    │ body: {email, password, fullName}
    ▼
[AuthController.java → register()]
    ▼
[AuthenticationService.java → register()]
    ├─ 1. Kiểm tra email đã tồn tại → userRepository.existsByEmail()
    │      → Nếu đã có: EmailAlreadyExistsException (HTTP 409)
    ├─ 2. Hash password → passwordEncoder.encode() (BCrypt)
    ├─ 3. Resolve role USER → userManagementService.resolveRole("USER")
    │      → Tìm trong RoleRepository, nếu chưa có tự tạo mới
    ├─ 4. Lưu User → userRepository.save()
    ├─ 5. Tạo JWT token → jwtUtil.generateToken(savedUser)
    └─ 6. Return AuthResponse {token, user info, expiresAt}

[Frontend AuthContext.js]
    → Lưu token vào localStorage
    → Redirect về trang chủ (/)

⚠️ LƯU Ý: UserProfile KHÔNG được tạo tự động khi register
   → Cần tạo thêm logic tạo UserProfile đi kèm với User mới
```

### 5.4 Luồng TÌM KIẾM & ĐẶT TRƯỚC SÁCH

```
[User] → BooksPage.js → Gõ vào search bar (debounce 300ms)
    │
    ├─ Autocomplete: GET /api/v1/books/autocomplete?q={keyword}
    │   [BookSearchService.java → autocomplete()]
    │   → Nếu keyword < 2 ký tự: return []
    │   → BookSpecification.autocomplete() → prefix LIKE match
    │   → Lấy top 5, cached Redis key="autocompleteBooks::{keyword}"
    │
    └─ Full search: GET /api/v1/books/search?q={keyword}&category=&status=
        [BookSearchService.java → advancedSearch()]
        → BookSpecification.search() → dynamic predicates
        → Cached Redis key="{keyword,isbn,category,status,page,size,sort}"
        → Return Page<BookResponse>

[Đặt trước sách - CHƯA IMPLEMENT]
    ⚠️ Bảng reservations đã có đầy đủ trong schema.sql
    ⚠️ Entity Reservation.java đã có
    ⚠️ Nhưng KHÔNG có ReservationController, ReservationService, ReservationRepository
    → Cần implement đầy đủ tầng API cho tính năng này
```

---

## 6. CƠ SỞ DỮ LIỆU

### Hệ quản trị CSDL
**PostgreSQL 16** — chạy trên Docker, encoding UTF-8, max_connections=200.

### Danh sách bảng / Collection

| # | Bảng | Mô tả |
|---|---|---|
| 1 | `users` | Tài khoản người dùng (auth info) |
| 2 | `roles` | Vai trò: GUEST, USER, LIBRARIAN, ADMIN |
| 3 | `user_roles` | Mapping N-N giữa users và roles |
| 4 | `refresh_tokens` | JWT refresh token |
| 5 | `books` | Thông tin sách (metadata + số lượng) |
| 6 | `categories` | Thể loại sách (cây phân cấp) |
| 7 | `book_categories` | Mapping N-N sách–thể loại |
| 8 | `tags` | Nhãn sách (Bestseller, New Release...) |
| 9 | `book_tags` | Mapping N-N sách–nhãn |
| 10 | `borrow_records` | Phiếu mượn sách |
| 11 | `borrow_policies` | Chính sách mượn theo loại thành viên |
| 12 | `reservations` | Đặt trước sách |
| 13 | `fines` | Phí phạt quá hạn |
| 14 | `user_profiles` | Hồ sơ mở rộng (điểm, hạng, fine tồn đọng) |
| 15 | `reading_history` | Lịch sử đọc sách |
| 16 | `user_activities` | Audit log hoạt động người dùng |
| 17 | `book_reviews` | Đánh giá sách (rating 1-5, nội dung) |
| 18 | `review_votes` | Vote "hữu ích" cho review |
| 19 | `user_favorites` | Sách yêu thích |
| 20 | `user_wishlist` | Danh sách muốn đọc |
| 21 | `notifications` | Thông báo hệ thống |
| 22 | `notification_preferences` | Tuỳ chọn nhận thông báo |
| 23 | `system_settings` | Cài đặt hệ thống (key-value) |
| 24 | `analytics_events` | Sự kiện phân tích hành vi |
| 25 | `popular_books` | Cache sách phổ biến |

### Các trường quan trọng

**`books`:** `id (UUID PK)`, `isbn (UNIQUE)`, `title`, `author`, `publisher`, `category (text)`, `total_quantity`, `available_qty`, `borrowed_quantity`, `status (AVAILABLE/OUT_OF_STOCK/ARCHIVED/DAMAGED)`, `average_rating`, `format (PHYSICAL/EBOOK/AUDIOBOOK)`

**`users`:** `id (BIGSERIAL PK)`, `email (UNIQUE)`, `password_hash`, `status (ACTIVE/INACTIVE/SUSPENDED)`, `failed_login_attempts`, `locked_until`

**`borrow_records`:** `id (UUID PK)`, `member_id (FK users)`, `book_id (FK books)`, `borrow_date`, `due_date`, `return_date`, `borrow_status (ACTIVE/RETURNED/OVERDUE/PENDING_APPROVAL/CANCELLED)`, `fine_amount`, `extension_count`, `max_extensions`, `librarian_id (FK users)`

**`borrow_policies`:** `member_type (GUEST/USER/LIBRARIAN/ADMIN)`, `max_books_allowed`, `loan_period_days`, `fine_per_day`, `max_fine`, `grace_period_days`

**`user_profiles`:** `card_expiry_date`, `membership_level (BRONZE/SILVER/GOLD/PLATINUM)`, `outstanding_fines`, `current_books_borrowed`, `points`

### Sơ đồ ERD (dạng text)

```
[users] ──< [user_roles] >── [roles]
   │
   ├──< [refresh_tokens]
   ├──< [borrow_records] >── [books]
   │         │
   │         └──< [fines]
   ├──< [reservations] >── [books]
   ├──< [user_profiles]
   ├──< [user_favorites] >── [books]
   ├──< [user_wishlist] >── [books]
   ├──< [book_reviews] >── [books]
   ├──< [reading_history] >── [books]
   ├──< [user_activities]
   ├──< [notifications]
   └──< [notification_preferences]

[books] ──< [book_categories] >── [categories]
[books] ──< [book_tags] >── [tags]
[categories] ──< [categories] (self-reference: parent_id)

[borrow_policies] (standalone lookup table)
[system_settings] (key-value store)
[analytics_events] → [users] (nullable)
[popular_books] → [books] (1-1)
```

### Indexes & Constraints đặc biệt
- `idx_books_title` — GIN index full-text search (chưa dùng trong code)
- `idx_books_available` — Partial index: `WHERE available_qty > 0`
- `idx_borrow_records_due_date` — Partial index: `WHERE borrow_status = 'ACTIVE'`
- `UNIQUE(user_id, book_id)` trên `book_reviews`, `user_wishlist`
- Tất cả ID UUID dùng `uuid-ossp` extension

---

## 7. API / GIAO DIỆN

### 7.1 REST API Endpoints

**Base URL:** `http://localhost:8080`  
**Format:** JSON  
**Auth:** Bearer JWT token (Header: `Authorization: Bearer <token>`)  
**Docs:** Swagger UI tại `/swagger-ui/index.html`

#### Nhóm Authentication — `/api/v1/auth`
| Method | Path | Mô tả | Auth |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Đăng ký tài khoản mới | Không |
| POST | `/api/v1/auth/login` | Đăng nhập, nhận JWT | Không |
| GET | `/api/v1/auth/me` | Lấy thông tin user hiện tại | ✅ |
| GET | `/api/v1/auth/validate` | Validate JWT token (header) | Không |
| POST | `/api/v1/auth/logout` | Đăng xuất | ✅ |

#### Nhóm Books — `/api/v1/books`
| Method | Path | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/v1/books` | Danh sách sách (phân trang) | Không |
| POST | `/api/v1/books` | Tạo sách mới | ADMIN/LIBRARIAN |
| GET | `/api/v1/books/search` | Tìm kiếm sách (q, category, status, isbn) | Không |
| GET | `/api/v1/books/autocomplete?q=` | Gợi ý tìm kiếm (top 5) | Không |
| GET | `/api/v1/books/{id}` | Chi tiết sách | Không |
| PUT | `/api/v1/books/{id}` | Cập nhật sách | ADMIN/LIBRARIAN |
| DELETE | `/api/v1/books/{id}` | Archive sách | ADMIN/LIBRARIAN |
| POST | `/api/v1/books/{id}/borrow` | Đánh dấu mượn (nhanh) | ADMIN/LIBRARIAN |
| POST | `/api/v1/books/{id}/return` | Đánh dấu trả (nhanh) | ADMIN/LIBRARIAN |

#### Nhóm Borrows — `/api/v1/borrows`
| Method | Path | Mô tả | Auth |
|---|---|---|---|
| POST | `/api/v1/borrows` | Tạo phiếu mượn | USER/LIBRARIAN/ADMIN |
| POST | `/api/v1/borrows/return` | Trả sách + tính phạt | USER/LIBRARIAN/ADMIN |
| POST | `/api/v1/borrows/{id}/extend` | Gia hạn mượn | USER/LIBRARIAN/ADMIN |
| GET | `/api/v1/borrows/history` | Lịch sử mượn của user hiện tại | USER/LIBRARIAN/ADMIN |
| GET | `/api/v1/borrows/admin/{memberId}/history` | Lịch sử mượn theo user (admin) | ADMIN/LIBRARIAN |
| PUT | `/api/v1/borrows/{id}/approve` | Phê duyệt phiếu mượn | ADMIN/LIBRARIAN |
| PUT | `/api/v1/borrows/{id}/reject` | Từ chối phiếu mượn | ADMIN/LIBRARIAN |

#### Nhóm Users — `/api/v1/users`
| Method | Path | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/v1/users` | Danh sách user (query filter) | ADMIN |
| GET | `/api/v1/users/{id}` | Thông tin user | ADMIN |
| POST | `/api/v1/users` | Tạo user mới (admin) | ADMIN |
| PUT | `/api/v1/users/{id}` | Cập nhật user | ADMIN |
| DELETE | `/api/v1/users/{id}` | Xóa user | ADMIN |

#### Monitoring — Spring Actuator
| Path | Mô tả |
|---|---|
| `/actuator/health` | Health check (dùng cho Docker healthcheck) |
| `/actuator/metrics` | Metrics |
| `/actuator/info` | Thông tin ứng dụng |

### 7.2 Giao diện người dùng (Frontend)

#### Danh sách các trang chính

| Đường dẫn | Component | Vai trò | Auth |
|---|---|---|---|
| `/` | `HomePage.js` | Trang chủ — banner, sách nổi bật | Không |
| `/books` | `BooksPage.js` | Danh sách + tìm kiếm sách | Không |
| `/books/:id` | `BookDetailPage.js` | Chi tiết sách | Không |
| `/categories` | `CategoriesPage.js` | Duyệt theo thể loại | Không |
| `/login` | `AuthPage.js` (login mode) | Đăng nhập | Không (redirect nếu đã đăng nhập) |
| `/register` | `AuthPage.js` (register mode) | Đăng ký | Không |
| `/my-books` | `MyBorrowsPage.js` | Sách đang mượn | USER+ |
| `/history` | `HistoryPage.js` | Lịch sử mượn | USER+ |
| `/favorites` | `FavoritesPage.js` | Sách yêu thích | USER+ |
| `/reviews` | `ReviewsPage.js` | Đánh giá của tôi | USER+ |
| `/profile` | `ProfilePage.js` | Hồ sơ cá nhân | USER+ |
| `/borrow/:bookId` | `BorrowBookPage.js` | Form mượn sách | USER+ |
| `/dashboard` | `RoleBasedDashboard.js` | Dashboard Admin/Librarian | ADMIN/LIBRARIAN |
| `/admin/books` | `BookPage.js` | Quản lý sách (CRUD) | ADMIN/LIBRARIAN |
| `/admin/borrow` | `BorrowPage.js` | Quản lý phiếu mượn | ADMIN/LIBRARIAN |
| `/users` | `UserPage.js` | Quản lý người dùng | ADMIN only |

#### Luồng điều hướng (Navigation Flow)
```
Guest:
  HomePage → BooksPage → BookDetailPage
  → /login (nếu bấm Mượn khi chưa đăng nhập)

User:
  HomePage → BooksPage → BookDetailPage → /borrow/:bookId
  → /my-books (xem đang mượn)
  → /history · /favorites · /profile · /reviews

Admin/Librarian:
  → /dashboard (RoleBasedDashboard)
  → /admin/books (CRUD sách)
  → /admin/borrow (duyệt phiếu mượn)
  → /users (Admin only — quản lý user)
```

#### Thư viện UI đang dùng
- **Tailwind CSS 3.4** — utility-first CSS
- **@headlessui/react 2.2** — dialog, dropdown, tabs (accessible)
- **@heroicons/react 2.2** — icon SVG
- **recharts 3.8** — biểu đồ (đang dùng mockData)

---

## 8. HƯỚNG DẪN SETUP & CHẠY DỰ ÁN

### 8.1 Yêu cầu môi trường

| Tool | Phiên bản tối thiểu | Ghi chú |
|---|---|---|
| Docker Desktop | 24.x+ | Bao gồm Docker Compose v2 |
| Java JDK | 21 (LTS) | Chỉ cần khi chạy native (không Docker) |
| Node.js | 16.x+ | Chỉ cần khi develop frontend |
| Maven | 3.9+ | Có Maven Wrapper (`./mvnw`) đi kèm |
| Git | Bất kỳ | |

### 8.2 Các bước cài đặt

```bash
# 1. Clone project
git clone <repository-url>
cd library-system

# 2. Sao chép file cấu hình môi trường
cp .env.example .env
# Chỉnh sửa .env nếu cần đổi port hoặc password

# 3. Chạy toàn bộ stack (Docker Compose)
docker compose up -d

# Hoặc build lại image khi code thay đổi
docker compose up -d --build
```

### 8.3 Cấu hình Database
```bash
# Schema + Seed data được tự động khởi tạo khi khởi động PostgreSQL container
# File: ./src/main/resources/db/schema.sql
# (mount vào docker-entrypoint-initdb.d)

# Kết nối trực tiếp DB (debug):
docker exec -it library-postgres psql -U postgres -d library_db
```

**Tài khoản mặc định (seed data):**
| Email | Mật khẩu | Vai trò |
|---|---|---|
| `admin@library.com` | `password` | ADMIN |
| `librarian1@library.com` | `librarian123` | LIBRARIAN |
| `user1@library.com` | `user123` | USER |

### 8.4 Khởi chạy ứng dụng

**Chạy bằng Docker Compose (khuyến nghị):**
```bash
docker compose up -d
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui/index.html
# Health Check: http://localhost:8080/actuator/health
```

**Chạy Native (dev mode):**
```bash
# Backend (cần PostgreSQL + Redis đang chạy)
./mvnw spring-boot:run

# Frontend (terminal khác)
cd frontend
npm install
npm start
# Frontend: http://localhost:3000
```

**Biến môi trường quan trọng (`.env`):**
```env
DB_NAME=library_db
DB_USER=postgres
DB_PASSWORD=postgres
DB_PORT=5432
REDIS_PORT=6379
JWT_SECRET=your-secure-secret-key
BACKEND_PORT=8080
FRONTEND_PORT=3000
```

### 8.5 Các lỗi thường gặp khi setup

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| `Connection refused` khi backend start | PostgreSQL/Redis chưa sẵn sàng | Docker healthcheck sẽ tự retry. Chờ ~30s hoặc `docker compose logs backend` |
| `BeanCreationException: Redis` | Thiếu Redis container | Đảm bảo `docker compose up` khởi động đủ 4 services |
| `ddl-auto: create` xóa dữ liệu | Hibernate tạo lại schema mỗi lần start | Đổi `ddl-auto: validate` hoặc `update` trong `application.yml` sau khi setup xong |
| `CORS error` trên Frontend | Backend port không khớp | Kiểm tra `nginx.conf` — proxy_pass đến `backend:8080` |
| `401 Unauthorized` sau đăng nhập | Token không được gửi trong header | Kiểm tra `AuthContext.js` — token lưu vào `localStorage` |
| UserProfile not found khi mượn sách | User mới đăng ký chưa có UserProfile | Bug đã biết — cần thêm logic tạo UserProfile trong `register()` |

---

## 9. ĐÁNH GIÁ CHẤT LƯỢNG CODE

### ✅ Điểm mạnh

1. **Kiến trúc phân tầng rõ ràng** — Tách biệt Controller/Service/Repository/Entity hoàn toàn. Dễ hiểu và bảo trì.
2. **Domain Exceptions phong phú** — 12 loại exception có tên nghĩa, được map đúng HTTP status bởi `UnifiedExceptionHandler`.
3. **Business logic trong Entity (Rich Domain Model)** — `Book.borrowBook()`, `BorrowRecord.extendLoan()`, `canExtend()` — tốt hơn anemic domain.
4. **JPA Specification Pattern** — `BookSpecification` xây dynamic query linh hoạt, không hardcode.
5. **Redis Cache đúng chỗ** — Cache search (`sync=true` chống stampede), autocomplete (không cache keyword < 2 ký tự).
6. **Global Exception Handler** — Mọi exception đều được bắt và trả về JSON nhất quán với timestamp, status, message, path.
7. **Swagger/OpenAPI** — Tích hợp `springdoc-openapi`, có annotation mô tả trên các Controller quan trọng.
8. **Connection pooling (HikariCP)** — Cấu hình max-pool-size=20, min-idle=10; Docker giới hạn memory 2GB cho backend.
9. **Stateless JWT** — Không lưu session server-side; phù hợp scale ngang.
10. **Seed data đầy đủ** — `schema.sql` có sẵn dữ liệu mẫu để dev/test ngay.

### ⚠️ Vấn đề tiềm ẩn / Nợ kỹ thuật

1. **`ddl-auto: create` trong production** — **Nghiêm trọng!** Mỗi lần restart backend, Hibernate sẽ drop và tạo lại toàn bộ schema → mất dữ liệu. Cần đổi sang `validate` hoặc dùng Flyway/Liquibase.
2. **UserProfile không được tạo tự động khi register** — `AuthenticationService.register()` tạo User nhưng không tạo `UserProfile`, dẫn đến lỗi `ResourceNotFoundException` khi user mới cố mượn sách.
3. **Bug trong `AuthController.java` dòng 50** — Biến `user` không được khai báo trong method `getCurrentUser()`:
   ```java
   AuthResponse response = ControllerHelper.buildAuthResponse(user); // user chưa được tìm
   ```
4. **Fine được tạo lặp lại** — `calculateOverdueFines()` được gọi khi tạo phiếu mượn mới, có thể tạo nhiều bản ghi `Fine` cho cùng một `BorrowRecord` nếu không kiểm tra trùng lặp.
5. **Magic number 7 ngày gia hạn** — `record.extendLoan(7)` hardcode trong `BorrowController`, không lấy từ `BorrowPolicy`.
6. **Dashboard dùng mockData** — `AdminDashboard.js` và `LibrarianDashboard.js` render dữ liệu giả, không gọi API thực.
7. **Không có Flyway/Liquibase** — Schema migration thủ công qua `schema.sql`. Khó quản lý version schema khi team nhiều người.
8. **Thiếu Rate Limiting** — Không có giới hạn request; endpoint đăng nhập dễ bị brute-force.
9. **Logging level quá chi tiết trên production** — `DEBUG` cho SQL, `TRACE` cho Hibernate binder sẽ ảnh hưởng performance.
10. **`category` trong `books` table là text** — Thiết kế mâu thuẫn: có bảng `categories` nhưng `books.category` vẫn là cột text riêng biệt. Gây khó join và không nhất quán.
11. **Thiếu unit test và integration test** — Chỉ có `spring-boot-starter-test` trong dependency; không có file test nào được tìm thấy trong dự án.

### Unit Test / Integration Test
❌ **Chưa có** — Không có file `*Test.java` nào trong dự án. Đây là nợ kỹ thuật lớn nhất về chất lượng.

### Xử lý lỗi (Error Handling)
✅ **Tốt** ở tầng HTTP (UnifiedExceptionHandler bắt tất cả).  
⚠️ **Một phần** ở tầng application — một số nơi dùng `IllegalStateException` thay vì custom exception có nghĩa.

---

## 10. SO SÁNH VỚI CHUẨN THƯ VIỆN THỰC TẾ

| Tiêu chí | Chuẩn thực tế (Koha/PMB) | Dự án hiện tại | Mức độ |
|---|---|---|---|
| **Quản lý kho & vị trí kệ sách** | Có (shelf location, sublocation) | ❌ Chưa có | Thiếu |
| **Quản lý bản sao sách (copy)** | Mỗi bản có mã riêng, barcode | ⚠️ Một phần (field book_copy_id nhưng chưa có entity) | Thiếu |
| **Chuẩn biên mục MARC21** | Có đầy đủ | ❌ Không có | Thiếu |
| **Chuẩn Dublin Core** | Có | ❌ Không có | Thiếu |
| **Mã vạch / QR code** | Có (barcode per copy) | ❌ Không có | Thiếu |
| **Gửi email nhắc nhở quá hạn** | Có (scheduled job) | ❌ Chưa có (entity Notification có nhưng chưa có email sender) | Thiếu |
| **Xuất báo cáo PDF/Excel** | Có | ❌ Không có | Thiếu |
| **Giao diện tra cứu độc giả (OPAC)** | Có (public catalog) | ✅ Có (BooksPage, BookDetailPage — public) | Đạt |
| **Đặt trước / giữ chỗ sách (Hold)** | Có đầy đủ | ⚠️ Schema có, API chưa có | Thiếu |
| **Quản lý nhiều chi nhánh thư viện** | Có | ❌ Không có | Thiếu |
| **Đa ngôn ngữ (i18n)** | Thường có | ❌ Chưa có | Thiếu |
| **Tìm kiếm full-text nâng cao** | Có (GIN/Elasticsearch) | ⚠️ Index GIN có trong SQL nhưng chưa dùng | Một phần |
| **Backup & restore dữ liệu** | Có | ⚠️ Docker volume (postgres_data), chưa có automated backup | Một phần |
| **Audit log hoạt động** | Có | ✅ Có bảng `user_activities` (chưa ghi log đầy đủ) | Một phần |
| **Thống kê & báo cáo nâng cao** | Có | ⚠️ Biểu đồ UI nhưng dùng mockData | Một phần |
| **Tích hợp API bên ngoài (Google Books)** | Một số hệ thống có | ❌ Không có | Thiếu |
| **Hệ thống điểm thưởng / gamification** | Ít phổ biến | ✅ Có (membership_level, points) | Vượt chuẩn |
| **Ebook / Audiobook** | Một số hệ thống | ✅ Có enum BookFormat | Vượt chuẩn |
| **Wishlist / Yêu thích** | Ít phổ biến | ✅ Có entity đầy đủ | Vượt chuẩn |
| **Phân quyền chi tiết theo chức năng** | Có (RBAC) | ✅ Có 4 roles + @PreAuthorize | Đạt |
| **Chính sách mượn theo loại thành viên** | Có | ✅ Bảng `borrow_policies` đầy đủ | Đạt |
| **Cache tìm kiếm (Redis)** | Ít phổ biến | ✅ Có với Spring Cache | Vượt chuẩn |

---

## 11. LỘ TRÌNH PHÁT TRIỂN ĐỀ XUẤT

### 🔴 Ưu tiên cao — Cần làm ngay để hệ thống hoạt động ổn định

**1. Sửa bug `ddl-auto: create` → `validate` và tích hợp Flyway**
- File cần sửa: `src/main/resources/application.yml`
- Thêm dependency: `flyway-core` vào `pom.xml`
- Tạo thư mục: `src/main/resources/db/migration/V1__init.sql`
- Độ phức tạp: **Thấp**
- Ảnh hưởng: Toàn bộ database — **Rất quan trọng cho production**

**2. Sửa bug không tạo UserProfile khi register**
- File cần sửa: `AuthenticationService.java → register()`
- Thêm: `UserProfileRepository.save(new UserProfile(savedUser))`
- Độ phức tạp: **Thấp**
- Ảnh hưởng: Tính năng mượn sách của user mới đăng ký

**3. Sửa bug CompileError trong `AuthController.java` dòng 50 (biến `user` undefined)**
- File cần sửa: `AuthController.java → getCurrentUser()`
- Thêm: Gọi `authenticationService.findByEmail(email)` để lấy `user`
- Độ phức tạp: **Thấp**
- Ảnh hưởng: API `GET /api/v1/auth/me` bị lỗi runtime

**4. Sửa logic tính phí phạt lặp — thêm idempotency check**
- File cần sửa: `BorrowManagementService.java → calculateOverdueFines()`
- Thêm kiểm tra: Nếu Fine với `borrowRecordId` và status `PENDING` đã tồn tại thì không tạo thêm
- Độ phức tạp: **Thấp**
- Ảnh hưởng: Bảng `fines` bị duplicate records

**5. Implement API Reservations (Đặt trước sách)**
- Tạo mới: `ReservationRepository.java`, `ReservationService.java`, `ReservationController.java`
- API cần: `POST /api/v1/reservations`, `DELETE /api/v1/reservations/{id}`, `GET /api/v1/reservations/my`
- Độ phức tạp: **Trung bình**
- Ảnh hưởng: Tính năng mới, không ảnh hưởng code hiện tại

**6. Đưa Dashboard lên dữ liệu thực (bỏ mockData)**
- File cần sửa: `AdminDashboard.js`, `LibrarianDashboard.js`
- Tạo API: `GET /api/v1/admin/stats` trả về tổng số sách, user, phiếu mượn
- Độ phức tạp: **Trung bình**
- Ảnh hưởng: UI hiển thị — không ảnh hưởng backend nghiệp vụ

---

### 🟡 Ưu tiên trung bình — Nâng cao chất lượng

**7. Xử lý tặng điểm thưởng khi trả sách đúng hạn**
- File cần sửa: `BorrowManagementService.java → processReturn()`
- Thêm: Cộng points vào `user_profiles.points` nếu trả đúng hạn
- Độ phức tạp: **Thấp**

**8. Implement API thanh toán phí phạt**
- Tạo: `FineController.java`, `FineService.java`
- API: `POST /api/v1/fines/{id}/pay`
- Độ phức tạp: **Thấp**

**9. Giải quyết mâu thuẫn `books.category` vs bảng `categories`**
- Xóa cột `category` (text) khỏi entity `Book`, dùng quan hệ `@ManyToMany` với `Category`
- Sửa: `BookManagementService.updateBook()` — dòng đang comment TODO
- Độ phức tạp: **Trung bình** — cần migration data

**10. Thêm Email notification (nhắc nhở sắp đến hạn)**
- Thêm dependency: `spring-boot-starter-mail`
- Tạo: `EmailService.java`, Scheduled task `@Scheduled`
- File mới: `src/main/resources/templates/email/overdue-reminder.html`
- Độ phức tạp: **Trung bình**

**11. Viết Unit Test & Integration Test**
- Tạo: `src/test/java/com/lms/library/` với các test cho Service layer
- Ưu tiên: `BorrowManagementServiceTest`, `BookManagementServiceTest`, `AuthenticationServiceTest`
- Độ phức tạp: **Trung bình**

**12. Implement Refresh Token API**
- Tạo: `POST /api/v1/auth/refresh` sử dụng bảng `refresh_tokens`
- Độ phức tạp: **Thấp**

**13. Implement brute-force protection (khóa tài khoản)**
- File cần sửa: `AuthenticationService.java → login()`
- Dùng fields: `failed_login_attempts`, `locked_until`
- Độ phức tạp: **Thấp**

**14. Lấy extension_days từ BorrowPolicy thay vì hardcode 7 ngày**
- File cần sửa: `BorrowController.java / BorrowManagementService.java`
- Độ phức tạp: **Rất thấp**

---

### 🟢 Ưu tiên thấp — Tính năng nâng cao / Nice-to-have

**15. Tích hợp Book API bên ngoài (Google Books API)**
- Tự động điền metadata sách khi thêm mới theo ISBN
- Tạo: `GoogleBooksIntegrationService.java`
- Độ phức tạp: **Trung bình**

**16. Mã QR Code cho từng bản sao sách**
- Thêm entity: `BookCopy.java` (barcode, shelf_location)
- Tạo API: `GET /api/v1/books/{id}/copies`
- Thêm dependency: `zxing` (QR generator)
- Độ phức tạp: **Cao**

**17. Xuất PDF / Excel báo cáo**
- Thêm dependency: `itext7` (PDF) / `apache-poi` (Excel)
- API: `GET /api/v1/reports/borrows?format=pdf&from=&to=`
- Độ phức tạp: **Trung bình**

**18. Tìm kiếm Full-text với PostgreSQL GIN index**
- Thay LIKE bằng `to_tsvector` / `to_tsquery` trong `BookSpecification`
- GIN index đã có sẵn trong schema.sql
- Độ phức tạp: **Trung bình**

**19. Tích hợp Elasticsearch cho tìm kiếm nâng cao**
- Thêm `spring-data-elasticsearch`
- Đồng bộ dữ liệu Book → Elasticsearch index
- Độ phức tạp: **Cao**

**20. Đa ngôn ngữ (i18n) — Tiếng Việt / Tiếng Anh**
- Frontend: `react-i18next`
- Backend: `MessageSource` với properties `messages_vi.properties`
- Độ phức tạp: **Trung bình**

---

*Báo cáo được tạo bởi phân tích tự động toàn bộ source code — Phiên bản: 1.0*
