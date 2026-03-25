# Frontend - Hệ Thống Quản Lý Thư Viện

React frontend application cho Hệ Thống Quản Lý Thư Viện.

## 🎯 Tính Năng

### ✅ Đã Triển Khai

- **Đăng Ký & Đăng Nhập** - Xác thực người dùng với JWT
- **Dashboard** - Trang chủ hiển thị thông tin người dùng
- **Quản Lý Tài Khoản** - Cập nhật thông tin & đổi mật khẩu
- **Navigation** - Thanh điều hương responsive
- **Protected Routes** - Bảo vệ các trang yêu cầu xác thực
- **API Integration** - Kết nối với backend microservices

### 🚀 Sắp Thêm

- [ ] Danh sách sách (Books)
- [ ] Chi tiết sách
- [ ] Mượn/Trả sách
- [ ] Lịch sử mượn
- [ ] Tìm kiếm sách

## 📁 Cấu Trúc Dự Án

```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── Navigation.js
│   │   ├── Navigation.css
│   │   ├── ProtectedRoute.js
│   ├── context/
│   │   └── AuthContext.js
│   ├── pages/
│   │   ├── Login.js
│   │   ├── Register.js
│   │   ├── Dashboard.js
│   │   ├── Account.js
│   │   ├── Auth.css
│   │   ├── Dashboard.css
│   │   └── Account.css
│   ├── services/
│   │   └── api.js
│   ├── App.js
│   ├── App.css
│   ├── index.js
│   └── index.css
├── .env.example
├── package.json
└── README.md
```

## 🛠️ Cài Đặt

### Yêu Cầu

- Node.js 16+
- npm hoặc yarn

### Bước 1: Cài Đặt Dependencies

```bash
cd frontend
npm install
```

### Bước 2: Tạo .env File

```bash
cp .env.example .env
```

### Bước 3: Chỉnh Sửa .env (Nếu Cần)

```env
REACT_APP_API_URL=http://localhost:8080/api
```

### Bước 4: Chạy Development Server

```bash
npm start
```

Ứng dụng sẽ mở tại `http://localhost:3000`

## 🔑 Các Trang Chính

### 1. **Login** (`/login`)
- Đăng nhập bằng email và mật khẩu
- Lưu JWT token vào localStorage
- Redirect tới dashboard sau khi đăng nhập thành công

### 2. **Register** (`/register`)
- Đăng ký tài khoản mới
- Xác thực mật khẩu
- Tự động đăng nhập sau khi đăng ký thành công

### 3. **Dashboard** (`/dashboard`) - Protected
- Hiển thị thông tin người dùng
- Danh sách sách đang mượn
- Liên kết nhanh tới các tính năng

### 4. **Account** (`/account`) - Protected
- Cập nhật thông tin cá nhân
- Đổi mật khẩu
- Đăng xuất

## 🌐 API Integration

### Authentication Service

```javascript
// utils/api.js
authAPI.register(data)      // POST /auth/register
authAPI.login(data)         // POST /auth/login
authAPI.getCurrentUser()    // GET /auth/me
authAPI.validateToken(token) // GET /auth/validate
authAPI.logout()            // POST /auth/logout
```

### User Service (Sắp thêm)

```javascript
userAPI.getProfile()        // GET /users/me
userAPI.updateProfile(data) // PUT /users/me
userAPI.changePassword(data) // POST /users/change-password
```

### Book Service (Sắp thêm)

```javascript
bookAPI.getBooks(params)    // GET /books
bookAPI.searchBooks(query)  // GET /books/search
bookAPI.getBookDetails(id)  // GET /books/{id}
```

### Borrow Service (Sắp thêm)

```javascript
borrowAPI.getBorrowedBooks() // GET /borrows/my-books
borrowAPI.borrowBook(bookId) // POST /borrows
borrowAPI.returnBook(borrowId) // POST /borrows/{id}/return
borrowAPI.getBorrowHistory() // GET /borrows/history
```

## 🔐 Xác Thực (Authentication)

### JWT Token Flow

```
1. User logs in
   └─ POST /api/auth/login
      └─ Nhận accessToken

2. Token lưu vào localStorage
   └─ localStorage.setItem('access_token', token)

3. Mỗi request sẽ gửi token
   └─ Authorization: Bearer {token}

4. Backend xác thực token
   └─ Nếu hợp lệ: cho phép request
   └─ Nếu hết hạn: redirect tới login
```

### Protected Routes

Tất cả routes bắt đầu bằng `/dashboard`, `/account`, etc. được bảo vệ bởi `<ProtectedRoute>` component.

```javascript
<ProtectedRoute>
  <Dashboard />
</ProtectedRoute>
```

## 🎨 Styling

Dự án sử dụng CSS vanilla với gradient colors:

- **Primary Color**: `#667eea`
- **Secondary Color**: `#764ba2`
- **Success Color**: `#d4edda`
- **Error Color**: `#f8d7da`

## 📦 Dependencies

```json
{
  "react": "^18.2.0",
  "react-dom": "^18.2.0",
  "react-router-dom": "^6.8.0",
  "axios": "^1.3.0",
  "react-scripts": "5.0.1"
}
```

## 🧪 Testing

```bash
# Run tests
npm test
```

## 🏗️ Build Production

```bash
# Build for production
npm run build
```

Output sẽ lưu trong thư mục `build/`

## 🐛 Troubleshooting

### Issue: CORS Error

**Solution**: Đảm bảo backend running trên `http://localhost:8080` và đã bật CORS

### Issue: Token Expired

**Solution**: Token sẽ tự động xóa và user sẽ được redirect tới login page

### Issue: Blank Page

**Solution**: Kiểm tra browser console cho errors, đảm bảo `public/index.html` có `<div id="root"></div>`

## 📞 Support

Cho vấn đề hoặc câu hỏi, vui lòng tạo issue trên GitHub.

## 📝 Changelog

### v1.0.0
- ✅ Authentication (Login/Register)
- ✅ Dashboard
- ✅ Account Management
- ✅ Protected Routes
- ✅ API Integration

---

**Status**: 🔄 In Development  
**Last Updated**: January 2024
