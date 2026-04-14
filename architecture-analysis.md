# Báo cáo phân tích kiến trúc (evidence-based) — Library System

> Phạm vi: **chỉ dựa trên những gì thực sự tồn tại trong source code** trong workspace này.  
> Không chạy ứng dụng, không suy đoán hành vi runtime ngoài những gì code/cấu hình thể hiện.

## 1. Tổng quan nhanh

- Kiến trúc tổng thể: **Monolith Spring Boot (backend) + React SPA (frontend)**.
- CSDL: PostgreSQL (khởi tạo bằng `src/main/resources/db/schema.sql` khi chạy qua Docker Compose), Redis có cấu hình nhưng **không thấy code sử dụng cache**.
- Security: Spring Security **stateless JWT (HS256)** với filter `JwtAuthenticationFilter`.
- API prefix chính: backend dùng `/api/v1/*`.

### 1.1. Sơ đồ triển khai (Docker Compose)

```mermaid
flowchart LR
  U[User Browser] -->|HTTP :3000| FE[frontend: nginx (container)]
  FE -->|proxy /api/*| BE[backend: Spring Boot :8080]
  BE --> PG[(postgres:5432)]
  BE --> R[(redis:6379)]
```

Nguồn bằng chứng:
- `docker-compose.yaml` định nghĩa `postgres`, `backend`, `redis`, `frontend`.
- Frontend nginx proxy `/api/` -> `backend:8080` (file `frontend/nginx.conf`).

## 2. Cấu trúc repo & công nghệ

### 2.1. Backend (Java/Spring Boot)

- Maven + Spring Boot `3.3.5`, Java `21` (`pom.xml`).
- Các starter: Web, Data JPA, Data Redis, Security, Actuator, Validation (`pom.xml`).
- JWT: `io.jsonwebtoken:jjwt-* 0.12.3`.
- OpenAPI UI: `springdoc-openapi-starter-webmvc-ui`.
- Hypersistence JSONB: `hypersistence-utils-hibernate-63` (được dùng cho `Role.permissions`, `UserProfile` các trường JSONB).

**Không tìm thấy trong code**:
- Sử dụng MapStruct (`pom.xml` có dependency) nhưng **không có `@Mapper`** trong `src/main/java`.

### 2.2. Frontend (React)

- React 18 + react-router-dom 6 (`frontend/package.json`).
- UI libs: headlessui, heroicons, recharts.
- API calls chủ yếu qua `fetch` (một phần tập trung trong `frontend/src/lib/api.js`).

## 3. Cấu hình runtime & infra

### 3.1. `application.yml`

- Datasource: PostgreSQL qua env `DB_HOST/DB_PORT/DB_NAME/...`.
- JPA: `spring.jpa.hibernate.ddl-auto: update`.
- SQL init: `spring.sql.init.mode: never` (Spring không tự chạy schema init).
- JWT config:
  - `spring.security.jwt.secret`
  - `spring.security.jwt.expiration: 604800 # 7 days`
  - `spring.security.jwt.refresh-expiration: 2592000` (refresh token)
- Redis: cấu hình host/port/password + `spring.cache.type: redis`.

**Không tìm thấy trong code**:
- Refresh token flow: có config `refresh-expiration` nhưng **không có endpoint/service/repository** cho refresh token.
- Cache/Redis usage: không thấy `@Cacheable`, `RedisTemplate`, `CacheManager` trong `src/main/java`.

### 3.2. Nginx

Có 2 config nginx:
- `frontend/nginx.conf`: nginx trong container frontend.
- `nginx/nginx.conf`: một nginx reverse proxy “full stack” (có rate-limit, HTTPS server block).

Điểm đáng chú ý:
- `nginx/nginx.conf` có location rate limit riêng cho `/api/auth/login` nhưng frontend/backend thực tế dùng `/api/v1/auth/login` ⇒ rate-limit riêng này **không áp dụng** cho login hiện tại (vì prefix khác).
- `nginx/nginx.conf` server 443 `include /etc/nginx/conf.d/common.conf`.
  - **Không tìm thấy file `common.conf` trong repo** ⇒ nếu dùng config này “as-is” sẽ phụ thuộc artifact bên ngoài image.

## 4. Database: schema.sql vs JPA hiện tại

### 4.1. Schema nguồn

Docker Compose mount `src/main/resources/db/schema.sql` vào `/docker-entrypoint-initdb.d/schema.sql` của Postgres. Schema này tạo **nhiều bảng**: auth, books, categories/tags, borrow, reservations, fines, profiles, reviews, favorites/wishlist, notifications, settings, analytics…

Lưu ý về seed data (quan sát trực tiếp từ `schema.sql`):
- Phần `INSERT INTO users ...` trong file hiển thị 8 dòng user (admin + 2 librarian + 5 user).
- Phần `INSERT INTO user_profiles ...` có một dòng `user_id = 9` (email `user@library.com`).
- Trong schema, `user_profiles.user_id` có ràng buộc foreign key tới `users(id)`.

Vì vậy, với một database “fresh” chỉ được init bằng chính file này, seed `user_profiles` có **nguy cơ vi phạm foreign key** (do `users` không có id 9 theo đúng các insert hiển thị). Đây là rủi ro cấu hình/seed; báo cáo này không chạy DB để xác nhận runtime.

### 4.2. JPA Entities (thực sự có `@Entity`)

Các class có `@Entity` trong `src/main/java/com/lms/library/domain/entity`:
- `User` → table `users`
- `Role` → table `roles` (có `permissions` JSONB)
- `Book` → table `books` (có Many-to-Many `categories` qua `book_categories`, `tags` qua `book_tags`)
- `Category` → table `categories` (self-reference parent/children)
- `Tag` → table `tags`
- `BorrowRecord` → table `borrow_records`
- `BorrowPolicy` → table `borrow_policies`
- `Fine` → table `fines`
- `UserProfile` → table `user_profiles` (nhiều trường JSONB)

### 4.3. Domain classes tồn tại nhưng **không** là JPA entity

Trong cùng thư mục entity có nhiều class chỉ là POJO (không có `@Entity` / không có repository):
- `RefreshToken`, `Reservation`, `BookReview`, `ReviewVote`, `Notification`, `NotificationPreference`, `UserFavorite`, `UserWishlist`, `UserActivity`, `AnalyticsEvent`, `PopularBook`, `ReadingHistory`, `SystemSetting`.

Ý nghĩa thực tế:
- DB schema có các bảng tương ứng (ví dụ `refresh_tokens`, `reservations`, `book_reviews`, `notifications`…), nhưng backend hiện tại **không có persistence layer + API layer** cho các subsystems đó.

### 4.4. Mapping nhanh “Schema table → implemented?”

| Schema table | JPA @Entity | Repository | API/Controller | Ghi chú |
|---|---:|---:|---:|---|
| `users` | ✅ `User` | ✅ `UserRepository` | ✅ `/api/v1/auth/*` (một phần) | Không có API CRUD user quản trị |
| `roles` | ✅ `Role` | ❌ | ❌ | Role dùng qua `User.roles`; không có endpoint quản lý role |
| `user_roles` | (join) | (join) | ❌ | map Many-to-Many EAGER |
| `refresh_tokens` | ❌ (POJO `RefreshToken`) | ❌ | ❌ | Có config refresh-expiration nhưng chưa implement |
| `books` | ✅ `Book` | ✅ `BookRepository` | ✅ `/api/v1/books/*` | Seed data dùng cột `books.category` nhưng entity không map cột này |
| `categories` | ✅ `Category` | ❌ | ❌ | Có entity nhưng không có repository/controller |
| `book_categories` | join | ❌ | ❌ | `BookResponse.category` lấy “category đầu tiên” từ `Book.categories` |
| `tags` | ✅ `Tag` | ❌ | ❌ | Có entity nhưng không có repository/controller |
| `book_tags` | join | ❌ | ❌ | |
| `borrow_records` | ✅ `BorrowRecord` | ✅ `BorrowRecordRepository` | ✅ `/api/v1/borrows/*` (một phần) | Không có endpoint list/filter theo status |
| `borrow_policies` | ✅ `BorrowPolicy` | ✅ `BorrowPolicyRepository` | ❌ | BorrowPolicy được dùng nội bộ khi tạo borrow |
| `reservations` | ❌ (POJO `Reservation`) | ❌ | ❌ | |
| `fines` | ✅ `Fine` | ✅ `FineRepository` | ❌ | Fine tạo trong `BorrowManagementService`; chưa có endpoint thanh toán |
| `user_profiles` | ✅ `UserProfile` | ✅ `UserProfileRepository` | ❌ | Dùng để validate borrow (card expiry, outstanding fines) |
| `reading_history` | ❌ (POJO) | ❌ | ❌ | |
| `user_activities` | ❌ (POJO) | ❌ | ❌ | |
| `book_reviews` | ❌ (POJO `BookReview`) | ❌ | ❌ | Frontend có page Reviews nhưng ghi rõ backend chưa expose |
| `review_votes` | ❌ (POJO) | ❌ | ❌ | |
| `user_favorites` | ❌ (POJO) | ❌ | ❌ | Frontend favorites hiện dùng localStorage |
| `user_wishlist` | ❌ (POJO) | ❌ | ❌ | |
| `notifications` | ❌ (POJO) | ❌ | ❌ | |
| `notification_preferences` | ❌ (POJO) | ❌ | ❌ | |
| `system_settings` | ❌ (POJO `SystemSetting`) | ❌ | ❌ | |
| `analytics_events` | ❌ (POJO `AnalyticsEvent`) | ❌ | ❌ | |
| `popular_books` | ❌ (POJO) | ❌ | ❌ | |

## 5. Backend: layer-by-layer

Backend package layout (theo code):
- `com.lms.library.domain.*`: entities, repositories, domain exceptions
- `com.lms.library.application.*`: DTOs, services
- `com.lms.library.infrastructure.*`: security (JWT)
- `com.lms.library.presentation.*`: controllers, exception handler

### 5.1. Service layer (business logic)

#### 5.1.1. `AuthenticationService`

- `register(RegisterRequest)`:
  - check `existsByEmail`
  - tạo `User` (chưa gán role mặc định trong code)
  - generate JWT bằng email làm subject
  - trả `AuthResponse` gồm `accessToken` + `expiresAt`.
- `login(LoginRequest)`:
  - check password BCrypt
  - check user active
  - generate token
- `validateToken(token)`:
  - extract email từ JWT (parser verify signature)
  - load user theo email

**Gaps/rủi ro quan sát được**:
- Token expiry time có nguy cơ không nhất quán với cấu hình (xem phần JWT bên dưới).
- `register` tạo User nhưng **không gán `roles`** (list roles default trong entity builder là `new ArrayList<>()`, nhưng ở `register` không set roles). Việc user mới có role nào phụ thuộc vào DB seed/logic khác (không thấy trong code).
- `register` **không tạo `UserProfile`**. Trong khi đó, `BorrowManagementService.createBorrowing(...)` yêu cầu `UserProfile` phải tồn tại (nếu không sẽ ném `ResourceNotFoundException`). Điều này có thể làm user vừa đăng ký **không borrow được** nếu DB không có sẵn `user_profiles` cho user đó.

#### 5.1.2. `BookManagementService`

- `createBook(BookCreateRequest)`:
  - chỉ set isbn/title/author/quantity/status.
  - **Không set category/subtitle/description/publisher/...** dù entity có các field và request có `category`.
- `searchBooks(title, author, category, status)`:
  - category filter chạy qua query `EXISTS (SELECT 1 FROM b.categories c WHERE c.name = :category)`.
  - status parse từ string.
- `deleteBook(UUID)`:
  - không xóa bản ghi mà gọi `archive()` (set status ARCHIVED) và save.

**Mismatch với schema/seed**:
- Seed trong `schema.sql` insert `books.category` (VARCHAR) nhưng entity `Book` không có field `category`.
- `BookResponse.category` lấy từ `Book.categories[0].name`. Nếu DB không có row trong `book_categories`, category trả về sẽ **null**.

#### 5.1.3. `BorrowManagementService`

Luồng `createBorrowing(memberId, memberType, CreateBorrowRequest)`:
1) Load `UserProfile` (bắt buộc phải tồn tại) từ `UserProfileRepository.findByUserId`.
2) Load `Book`.
3) Load `BorrowPolicy` theo `memberType` (lấy policy mới nhất theo createdAt).
4) Validation:
   - card expiry (`userProfile.isCardExpired()`)
   - outstanding fines (`userProfile.hasOutstandingFines()`)
   - borrow limit: count borrow records status ACTIVE/PENDING_APPROVAL
   - book availability (`book.isAvailable()`)
   - không được borrow trùng bookId trong ACTIVE/PENDING_APPROVAL
5) Tạo `BorrowRecord` với status **PENDING_APPROVAL**.
6) **Ngay lập tức giảm tồn kho book** (`book.borrowBook()`), update user profile borrow count.

Luồng approve/reject:
- `approveBorrowRequest`: chỉ đổi status PENDING_APPROVAL → ACTIVE, set librarianId.
- `rejectBorrowRequest`: trả book về inventory, giảm count user profile, set status CANCELLED.

Luồng return:
- `processReturn(memberId, ReturnRequest)`:
  - check borrow record thuộc member
  - tính fine nếu overdue (dựa trên policy USER mặc định trong code)
  - trả book về inventory, decrement borrow count
  - gọi `record.returnBook(condition)` (set returnDate/returnTime/status)
  - nếu fine > 0 tạo `Fine` và cộng vào `UserProfile.outstandingFines`

Luồng extend:
- `extendLoan`: hardcode `record.extendLoan(7)` (7 ngày), không lấy từ policy.

**Gaps/mismatch quan sát được**:
- Return request có `returnNotes`, borrow record có `returnNotes`, return response có `returnNotes` nhưng `processReturn` **không set returnNotes** vào record/response.
- Fine calculation dùng `BorrowPolicy.MemberType.USER` cố định (không dùng memberType thực tế của user).
- Inventory bị giảm ngay từ lúc request PENDING_APPROVAL.

### 5.2. REST Controllers & API surface

#### 5.2.1. Auth API — `AuthController` (`/api/v1/auth`)

- `POST /api/v1/auth/register` → `RegisterRequest` → `AuthResponse` (201)
- `POST /api/v1/auth/login` → `LoginRequest` → `AuthResponse`
- `GET /api/v1/auth/me` → `AuthResponse`
- `GET /api/v1/auth/validate?token=...` → `AuthResponse` hoặc 401 `{valid:false,...}`
- `POST /api/v1/auth/logout` → 204

#### 5.2.2. Book API — `BookController` (`/api/v1/books`)

- `GET /api/v1/books` → `Page<BookResponse>`
- `GET /api/v1/books/search` → `Page<BookResponse>` với query `title/author/category/status`
- `GET /api/v1/books/{id}` → `BookResponse`

Quản trị (role ADMIN/LIBRARIAN):
- `POST /api/v1/books`
- `PUT /api/v1/books/{id}`
- `DELETE /api/v1/books/{id}` (thực tế archive)
- `POST /api/v1/books/{id}/borrow`
- `POST /api/v1/books/{id}/return`

#### 5.2.3. Borrow API — `BorrowController` (`/api/v1/borrows`)

- `POST /api/v1/borrows` (body `CreateBorrowRequest`, query `memberType` default USER) → `BorrowResponse` (201)
- `POST /api/v1/borrows/return` (body `ReturnRequest`) → `ReturnResponse`
- `POST /api/v1/borrows/{borrowRecordId}/extend` → `BorrowResponse`
- `GET /api/v1/borrows/history` → `List<BorrowResponse>`

Admin/Librarian:
- `GET /api/v1/borrows/admin/{memberId}/history` → `List<BorrowResponse>`
- `PUT /api/v1/borrows/{borrowRecordId}/approve` → `BorrowResponse`
- `PUT /api/v1/borrows/{borrowRecordId}/reject?reason=...` → `BorrowResponse`

**Không tìm thấy trong code**:
- `GET /api/v1/borrows?status=...` (frontend librarian dashboard đang gọi).
- `PUT /api/v1/borrows/{id}/return` (frontend librarian dashboard đang gọi).

### 5.3. Exception handling

- Có `@RestControllerAdvice` global: `presentation/exception/UnifiedExceptionHandler`.
- Bọc nhiều domain exceptions → JSON map gồm `timestamp/status/error/message/path`.

## 6. Security/JWT (backend)

### 6.1. Luồng JWT filter

- Client gửi `Authorization: Bearer <jwt>`.
- `JwtAuthenticationFilter`:
  - nếu header không có/không bắt đầu bằng `Bearer ` → bỏ qua.
  - extract subject bằng `jwtUtil.extractEmail`.
  - load userDetails bằng `CustomUserDetailsService.loadUserByUsername(email)`.
  - validate token: subject match + chưa expired.
  - set `SecurityContextHolder`.

### 6.2. Route authorization

`SecurityConfig`:
- PermitAll:
  - `/api/v1/auth/**`
  - `/actuator/**`
  - `/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`
  - `GET /api/v1/books/**`
- Authenticated:
  - non-GET `/api/v1/books/**`
  - `/api/v1/borrows/**`
  - các request khác

### 6.3. Vai trò (roles)

- `CustomUserDetailsService` chỉ map **role đầu tiên** trong `User.roles` thành authority `ROLE_<roleName>`.
- `ControllerHelper.buildAuthResponse` cũng lấy role đầu tiên.

### 6.4. Rủi ro / vấn đề thấy rõ trong code

1) **Không nhất quán đơn vị expiration**
- `JwtUtil.buildToken`: `new Date(System.currentTimeMillis() + expiration)` ⇒ `expiration` được dùng như **milliseconds**.
- `application.yml`: `expiration: 604800 # 7 days` (giá trị 604800 thường là **seconds**).
- `JwtUtil.getExpirationTime`: `LocalDateTime.now().plusSeconds(jwtExpiration)` ⇒ dùng như **seconds**.

Hệ quả có thể xảy ra (theo code):
- JWT có thể hết hạn sớm (nếu 604800 bị hiểu là ms ≈ 10 phút) trong khi `expiresAt` trả về lại là 7 ngày.

2) **`GET /api/v1/auth/me` được permitAll nhưng code assume đã có Authentication**
- `AuthController.getCurrentUser()` gọi `ControllerHelper.getCurrentUserEmail()`.
- `ControllerHelper.getCurrentUserEmail()` gọi `SecurityContextHolder.getContext().getAuthentication().getName()` **không null-check**.
- Vì `/api/v1/auth/**` được permitAll, request không token vẫn vào controller ⇒ có nguy cơ NPE → 500.

## 7. Frontend: routes, state, API usage

### 7.1. Router map (`frontend/src/App.js`)

Public routes (bọc bởi `LibraryLayout`):
- `/` → `HomePage`
- `/books` → `BooksPage`
- `/books/:id` → `BookDetailPage`
- `/categories` → `CategoriesPage`

Auth pages:
- `/login` → `AuthPage` (PublicOnly)
- `/register` → `AuthPage` (PublicOnly)

Routes yêu cầu đăng nhập (RequireAuth + `LibraryLayout`):
- `/my-books` → `MyBorrowsPage`
- `/favorites` → `FavoritesPage`
- `/history` → `HistoryPage`
- `/profile` → `ProfilePage`
- `/reviews` → `ReviewsPage`
- `/borrow/:bookId` → `BorrowBookPage`

Dashboard routes (RequireAuth + `DashboardLayout`):
- `/dashboard` → `RoleBasedDashboard`
- `/users` → `UserPage`
- `/admin/books` → `BookPage`
- `/admin/borrow` → `BorrowPage`

### 7.2. Auth state (`frontend/src/context/AuthContext.js`)

- Token lưu ở `localStorage['token']`.
- Khi mount hoặc token đổi: gọi `GET /api/v1/auth/validate?token=<token>`.
- `login`: `POST /api/v1/auth/login` → lưu `accessToken`.
- `register`: `POST /api/v1/auth/register` → lưu `accessToken`.
- `logout`: `POST /api/v1/auth/logout` rồi clear localStorage.

### 7.3. Central API helper (`frontend/src/lib/api.js`)

Các call chính:
- Books:
  - `GET /api/v1/books` (pagination)
  - `GET /api/v1/books/search`
  - `GET /api/v1/books/{id}`
- Auth:
  - `GET /api/v1/auth/me`
- Borrows:
  - `GET /api/v1/borrows/history`
  - `POST /api/v1/borrows`
  - `POST /api/v1/borrows/{id}/extend`
  - `POST /api/v1/borrows/return`

### 7.4. Những page/component có API thật vs mock

API thật:
- `HomePage`: fetch books + borrow history.
- `BooksPage`: list + filter bằng `/books` + `/books/search`.
- `BookDetailPage`: fetch book by id; borrow via `createBorrow`.
- `BorrowBookPage`: fetch book by id; borrow.
- `MyBorrowsPage` + `HistoryPage`: dùng `/borrows/history` và fetch book detail theo `bookId`.
- `FavoritesPage`: favorite IDs lưu localStorage, nhưng fetch book detail từ API.

Mock / UI-only (không gọi backend):
- `BookTable`, `UserTable`, `BorrowManagement`, `DashboardCards`: dữ liệu từ `frontend/src/data/mockData.js`.

## 8. Ma trận mismatch Frontend ↔ Backend

### 8.1. Frontend gọi nhưng backend không có

- `GET /api/v1/borrows?status=...` trong `frontend/src/pages/LibrarianDashboard.js`.
  - Backend hiện chỉ có `/api/v1/borrows/history` và `/api/v1/borrows/admin/{memberId}/history`.
- `PUT /api/v1/borrows/{borrowId}/return` trong `LibrarianDashboard.js`.
  - Backend hiện dùng `POST /api/v1/borrows/return` (body `ReturnRequest`).

### 8.2. Backend có nhưng frontend chưa thấy dùng

- `POST /api/v1/books/{id}/borrow` và `POST /api/v1/books/{id}/return` (chỉ admin/librarian).
- `GET /api/v1/borrows/admin/{memberId}/history`.

## 9. Danh sách “chưa implement / thiếu” theo code

- CRUD Users (admin): **không có**.
- CRUD Roles/Permissions: **không có**.
- Categories/Tags management API: **không có repository/controller**.
- Borrow listing/filter/pagination cho librarian dashboard: **không có**.
- Reservations subsystem: schema có nhưng backend chưa có persistence/API.
- Reviews/Ratings: schema có, domain POJO có, frontend page có placeholder, nhưng backend chưa expose.
- Favorites/Wishlist: schema có, backend chưa implement; frontend currently dùng localStorage.
- Notifications + preferences: schema có, backend chưa implement.
- System settings: schema có, backend chưa implement.
- Analytics events: schema có, backend chưa implement.
- Refresh tokens: schema + config có, backend chưa implement.
- Redis caching: dependency + config có, backend chưa dùng.

## 10. Rủi ro kỹ thuật (chỉ dựa trên code)

1) JWT expiration unit mismatch (ms vs seconds) ⇒ token có thể hết hạn sai.
2) `/api/v1/auth/me` permitAll + null Authentication ⇒ dễ lỗi 500.
3) Role mapping “first role only” ⇒ user có nhiều roles sẽ bị bỏ qua quyền.
4) Category/seed mismatch:
   - seed dùng `books.category`
   - backend search/filter & response dùng `book_categories` Many-to-Many
   ⇒ UI filter category có thể không hoạt động, category trả về null.
5) Borrow “PENDING_APPROVAL” nhưng đã giảm inventory và tăng borrowed count.
6) Return notes không được persist/return dù DTO có field.
7) User mới đăng ký có thể không borrow được vì thiếu `user_profiles` (backend yêu cầu profile tồn tại).

---

## Phụ lục A — File index (những file chính đã đọc)

Backend:
- `src/main/java/com/lms/library/infrastructure/security/*`
- `src/main/java/com/lms/library/presentation/controller/*`
- `src/main/java/com/lms/library/application/service/*`
- `src/main/java/com/lms/library/domain/entity/*`
- `src/main/java/com/lms/library/domain/repository/*`
- `src/main/java/com/lms/library/presentation/exception/UnifiedExceptionHandler.java`

Frontend:
- `frontend/src/App.js`
- `frontend/src/context/*`
- `frontend/src/lib/api.js`
- `frontend/src/pages/*`
- `frontend/src/components/*`

Infra:
- `docker-compose.yaml`
- `src/main/resources/application.yml`
- `src/main/resources/db/schema.sql`
- `Dockerfile`, `frontend/Dockerfile`
- `nginx/nginx.conf`, `frontend/nginx.conf`
