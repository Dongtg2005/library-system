-- Library Management System - Complete Database Schema & Seed Data
-- PostgreSQL 16+

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================================
-- 1. AUTHENTICATION & AUTHORIZATION
-- ========================================

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    email_verified BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL CHECK (name IN ('GUEST', 'USER', 'LIBRARIAN', 'ADMIN')),
    description TEXT,
    permissions JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT REFERENCES users(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- 2. BOOK MANAGEMENT
-- ========================================

CREATE TABLE IF NOT EXISTS books (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    isbn VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    subtitle VARCHAR(500),
    description TEXT,
    publisher VARCHAR(255),
    publication_date DATE,
    language VARCHAR(10) DEFAULT 'vi',
    pages INTEGER,
    format VARCHAR(20) DEFAULT 'PHYSICAL' CHECK (format IN ('PHYSICAL', 'EBOOK', 'AUDIOBOOK')),
    file_url VARCHAR(500),
    file_size BIGINT,
    cover_image_url VARCHAR(500),
    total_quantity INTEGER DEFAULT 1 NOT NULL,
    available_qty INTEGER DEFAULT 1 NOT NULL,
    borrowed_quantity INTEGER DEFAULT 0,
    reserved_quantity INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    average_rating NUMERIC(3,2) DEFAULT 0.0,
    rating_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    parent_id INTEGER REFERENCES categories(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS book_categories (
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, category_id)
);

CREATE TABLE IF NOT EXISTS tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    color VARCHAR(7) DEFAULT '#007bff'
);

CREATE TABLE IF NOT EXISTS book_tags (
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    tag_id INTEGER REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, tag_id)
);

-- ========================================
-- 3. BORROW MANAGEMENT
-- ========================================

CREATE TABLE IF NOT EXISTS borrow_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    member_id BIGINT NOT NULL,
    book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    book_copy_id UUID,
    borrow_date DATE,
    borrow_time TIMESTAMP WITH TIME ZONE,
    due_date DATE,
    return_date DATE,
    return_time TIMESTAMP WITH TIME ZONE,
    extension_count INTEGER DEFAULT 0,
    max_extensions INTEGER DEFAULT 2,
    last_extension_date DATE,
    borrow_status VARCHAR(20) DEFAULT 'ACTIVE',
    condition_on_borrow VARCHAR(20),
    condition_on_return VARCHAR(20),
    reservation_id UUID,
    fine_amount DECIMAL(10,2) DEFAULT 0.00,
    fine_paid BOOLEAN DEFAULT FALSE,
    fine_paid_at TIMESTAMP,
    notes TEXT,
    return_notes TEXT,
    librarian_id BIGINT REFERENCES users(id),
    return_librarian_id BIGINT REFERENCES users(id),
    renewal_count INTEGER DEFAULT 0,
    max_renewals INTEGER DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS borrow_policies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    member_type VARCHAR(20) NOT NULL,
    max_books_allowed INTEGER DEFAULT 5,
    loan_period_days INTEGER DEFAULT 14,
    max_extensions INTEGER DEFAULT 2,
    fine_per_day DECIMAL(10,2) DEFAULT 1000.00,
    max_fine DECIMAL(10,2),
    grace_period_days INTEGER DEFAULT 0,
    effective_from DATE,
    effective_to DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reservations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    reserved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'FULFILLED', 'CANCELLED', 'EXPIRED')),
    priority INTEGER DEFAULT 1 CHECK (priority BETWEEN 1 AND 3),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    borrow_record_id UUID NOT NULL REFERENCES borrow_records(id),
    member_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    fine_type VARCHAR(20) DEFAULT 'OVERDUE',
    status VARCHAR(20) DEFAULT 'PENDING',
    reason TEXT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- 4. USER PROFILES & ACTIVITY
-- ========================================

CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    bio TEXT,
    favorite_genres TEXT[],
    reading_preferences JSONB,
    notification_settings JSONB,
    privacy_settings JSONB,
    member_status VARCHAR(20) DEFAULT 'ACTIVE',
    total_books_borrowed INTEGER DEFAULT 0,
    current_books_borrowed INTEGER DEFAULT 0,
    total_books_read INTEGER DEFAULT 0,
    total_pages_read BIGINT DEFAULT 0,
    total_fines DECIMAL(10,2) DEFAULT 0.00,
    outstanding_fines DECIMAL(10,2) DEFAULT 0.00,
    membership_level VARCHAR(20) DEFAULT 'BRONZE' CHECK (membership_level IN ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM')),
    points INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reading_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    pages_read INTEGER,
    rating_given INTEGER CHECK (rating_given >= 1 AND rating_given <= 5),
    review_given BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_activities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id UUID,
    description TEXT,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- 5. REVIEWS & RATINGS
-- ========================================

CREATE TABLE IF NOT EXISTS book_reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(255),
    content TEXT NOT NULL,
    helpful_count INTEGER DEFAULT 0,
    verified_purchase BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'PUBLISHED' CHECK (status IN ('PUBLISHED', 'HIDDEN', 'DELETED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, book_id)
);

CREATE TABLE IF NOT EXISTS review_votes (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT REFERENCES book_reviews(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    helpful BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(review_id, user_id)
);

-- ========================================
-- 6. FAVORITES & WISHLIST
-- ========================================

CREATE TABLE IF NOT EXISTS user_favorites (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, book_id)
);

CREATE TABLE IF NOT EXISTS user_wishlist (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    priority INTEGER DEFAULT 1 CHECK (priority BETWEEN 1 AND 3),
    notification_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, book_id)
);

-- ========================================
-- 7. NOTIFICATIONS
-- ========================================

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    related_resource_type VARCHAR(50),
    related_resource_id UUID,
    read BOOLEAN DEFAULT FALSE,
    email_sent BOOLEAN DEFAULT FALSE,
    push_sent BOOLEAN DEFAULT FALSE,
    scheduled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notification_preferences (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    notification_type VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    channel VARCHAR(20) DEFAULT 'EMAIL' CHECK (channel IN ('EMAIL', 'PUSH', 'SMS', 'IN_APP')),
    PRIMARY KEY (user_id, notification_type)
);

-- ========================================
-- 8. SYSTEM CONFIGURATION & ANALYTICS
-- ========================================

CREATE TABLE IF NOT EXISTS system_settings (
    key VARCHAR(100) PRIMARY KEY,
    value TEXT,
    description TEXT,
    data_type VARCHAR(20) DEFAULT 'STRING' CHECK (data_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'JSON')),
    updated_by BIGINT REFERENCES users(id),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS analytics_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(255),
    properties JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS popular_books (
    book_id UUID PRIMARY KEY REFERENCES books(id) ON DELETE CASCADE,
    borrow_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    search_count INTEGER DEFAULT 0,
    last_calculated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- 9. INDEXES FOR PERFORMANCE
-- ========================================

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

CREATE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn);
CREATE INDEX IF NOT EXISTS idx_books_title ON books USING gin(to_tsvector('english', title));
CREATE INDEX IF NOT EXISTS idx_books_author ON books(author);
CREATE INDEX IF NOT EXISTS idx_books_status ON books(status);
CREATE INDEX IF NOT EXISTS idx_books_available ON books(available_qty) WHERE available_qty > 0;
CREATE INDEX IF NOT EXISTS idx_books_rating ON books(average_rating DESC, rating_count DESC);

CREATE INDEX IF NOT EXISTS idx_borrow_records_user ON borrow_records(member_id);
CREATE INDEX IF NOT EXISTS idx_borrow_records_book ON borrow_records(book_id);
CREATE INDEX IF NOT EXISTS idx_borrow_records_status ON borrow_records(borrow_status);
CREATE INDEX IF NOT EXISTS idx_borrow_records_due_date ON borrow_records(due_date) WHERE borrow_status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_book_reviews_book ON book_reviews(book_id);
CREATE INDEX IF NOT EXISTS idx_book_reviews_rating ON book_reviews(rating DESC);
CREATE INDEX IF NOT EXISTS idx_book_reviews_created ON book_reviews(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id, read);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type, created_at);

-- ========================================
-- 10. SEED DATA
-- ========================================

-- Test Accounts Credentials (see CREDENTIALS.md):
-- admin@library.com: password
-- librarian@library.com: librarian123
-- user@library.com: user123
-- Password hashing: BCrypt with cost factor 10

INSERT INTO roles (name, description, permissions) VALUES
('GUEST', 'Guest user with limited access', '{"READ_BOOKS": true, "VIEW_BOOK_DETAILS": true}'),
('USER', 'Regular user', '{"READ_BOOKS": true, "VIEW_BOOK_DETAILS": true, "BORROW_BOOKS": true, "RETURN_BOOKS": true, "RATE_BOOKS": true, "MANAGE_PROFILE": true, "VIEW_HISTORY": true}'),
('LIBRARIAN', 'Library staff', '{"READ_BOOKS": true, "VIEW_BOOK_DETAILS": true, "BORROW_BOOKS": true, "RETURN_BOOKS": true, "RATE_BOOKS": true, "MANAGE_PROFILE": true, "VIEW_HISTORY": true, "MANAGE_BOOKS": true, "MANAGE_USERS": true, "APPROVE_BORROWS": true, "VIEW_REPORTS": true}'),
('ADMIN', 'System administrator', '{"ALL_PERMISSIONS": true}')
ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, description) VALUES
('Fiction', 'Fictional literature and stories'),
('Non-Fiction', 'Non-fictional works'),
('Science Fiction', 'Science fiction and fantasy works'),
('Romance', 'Romantic novels'),
('Mystery', 'Mystery and thriller books'),
('Biography', 'Life stories and autobiographies'),
('History', 'Historical accounts'),
('Science', 'Scientific books'),
('Technology', 'Computer science and technology'),
('Business', 'Business and self-help'),
('Children', 'Children''s books and young adult literature')
ON CONFLICT (name) DO NOTHING;

INSERT INTO tags (name, color) VALUES
('Bestseller', '#ff6b6b'),
('New Release', '#4caf50'),
('Award Winner', '#ff9800'),
('Classic', '#9c27b0'),
('Recommended', '#2196f3'),
('Trending', '#ff5722'),
('Educational', '#00bcd4')
ON CONFLICT (name) DO NOTHING;

INSERT INTO system_settings (key, value, description, data_type) VALUES
('max_borrow_days', '14', 'Maximum days for book borrowing', 'NUMBER'),
('max_borrow_limit', '5', 'Maximum books per user', 'NUMBER'),
('fine_per_day', '1000.00', 'Fine amount per day in VND', 'NUMBER'),
('max_fine', '50000.00', 'Maximum fine amount in VND', 'NUMBER'),
('grace_period_days', '1', 'Grace period for overdue books', 'NUMBER'),
('enable_notifications', 'true', 'Enable system notifications', 'BOOLEAN'),
('enable_reviews', 'true', 'Enable book reviews', 'BOOLEAN'),
('enable_ratings', 'true', 'Enable book ratings', 'BOOLEAN')
ON CONFLICT (key) DO NOTHING;

INSERT INTO borrow_policies (name, member_type, max_books_allowed, loan_period_days, max_extensions, fine_per_day, max_fine, grace_period_days) VALUES
('Guest Policy', 'GUEST', 2, 7, 0, 2000.00, 10000.00, 0),
('User Policy', 'USER', 5, 14, 3, 1000.00, 50000.00, 1),
('Librarian Policy', 'LIBRARIAN', 10, 30, 5, 500.00, 100000.00, 2),
('Admin Policy', 'ADMIN', 20, 60, 10, 0.00, 0.00, 0)
ON CONFLICT DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, status) VALUES
('admin@library.com', '$2a$10$dXJ0a1pHQVpiUnAwQVpFMeNzYt5cjKMQ0t8y0Wt8KqDKNjkjDJH62', 'System Administrator', '0900000000', 'ACTIVE'),
('librarian1@library.com', '$2a$10$qN2yI4yKzwI4yI4yI4yI4O1N9P5Q8R3S6T1U2V7W4X5Y2Z3A4CvQy', 'Nguyễn Văn A', '0901234567', 'ACTIVE'),
('librarian2@library.com', '$2a$10$qN2yI4yKzwI4yI4yI4yI4O1N9P5Q8R3S6T1U2V7W4X5Y2Z3A4CvQy', 'Trần Thị B', '0902345678', 'ACTIVE'),
('user1@library.com', '$2a$10$BcZnVd5pQ9K8M4L7J3H6G1E2F5I8K1M0P9S2V5Y8B1E4R7U0X3A6', 'Lê Văn C', '0903456789', 'ACTIVE'),
('user2@library.com', '$2a$10$BcZnVd5pQ9K8M4L7J3H6G1E2F5I8K1M0P9S2V5Y8B1E4R7U0X3A6', 'Phạm Thị D', '0904567890', 'ACTIVE'),
('user3@library.com', '$2a$10$BcZnVd5pQ9K8M4L7J3H6G1E2F5I8K1M0P9S2V5Y8B1E4R7U0X3A6', 'Hoàng Văn E', '0905678901', 'ACTIVE'),
('user4@library.com', '$2a$10$BcZnVd5pQ9K8M4L7J3H6G1E2F5I8K1M0P9S2V5Y8B1E4R7U0X3A6', 'Nguyễn Thị F', '0906789012', 'ACTIVE'),
('user5@library.com', '$2a$10$BcZnVd5pQ9K8M4L7J3H6G1E2F5I8K1M0P9S2V5Y8B1E4R7U0X3A6', 'Trần Văn G', '0907890123', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 4), (2, 3), (3, 3), (4, 2), (5, 2), (6, 2), (7, 2), (8, 2)
ON CONFLICT DO NOTHING;

INSERT INTO books (isbn, title, author, publisher, publication_date, language, pages, format, total_quantity, available_qty, average_rating, rating_count, view_count, category) VALUES
('978-3-16-148410-0', 'Dế Mèn Là Vợ Tôi', 'Nguyễn Nhật Ánh', 'NXB Văn Học', '2020-05-15', 'vi', 320, 'PHYSICAL', 10, 10, 4.5, 12, 156, 'Fiction'),
('978-3-16-148411-0', 'Số Đỏ', 'Bùi Anh Tấn', 'NXB Văn Học', '2019-08-20', 'vi', 280, 'PHYSICAL', 8, 8, 4.2, 8, 234, 'Fiction'),
('978-3-16-148412-0', 'Chí Phèo', 'Nam Cao', 'NXB Văn Học', '2018-03-10', 'vi', 250, 'PHYSICAL', 15, 15, 4.8, 25, 189, 'Fiction'),
('978-3-16-148413-0', 'Lão Hạc', 'Nam Cao', 'NXB Văn Học', '2017-11-25', 'vi', 300, 'PHYSICAL', 12, 12, 4.6, 18, 167, 'Fiction'),
('978-3-16-148414-0', 'Vợ Nhặt', 'Kim Dung', 'NXB Văn Học', '2021-02-14', 'vi', 180, 'PHYSICAL', 20, 20, 4.3, 15, 145, 'Fiction'),
('978-3-16-148415-0', 'Nhà Giả Kim', 'Nguyễn Tuân', 'NXB Văn Học', '2016-06-30', 'vi', 400, 'PHYSICAL', 8, 8, 4.7, 22, 198, 'Fiction'),
('978-3-16-148416-0', 'Tắt Đèn', 'Ngô Tất Văn', 'NXB Văn Học', '2015-09-20', 'vi', 220, 'PHYSICAL', 25, 25, 4.4, 31, 176, 'Fiction'),
('978-3-16-148417-0', 'Con Chó', 'Vũ Trọng Phụng', 'NXB Văn Học', '2014-04-25', 'vi', 350, 'PHYSICAL', 18, 18, 4.9, 28, 234, 'Fiction'),
('978-3-16-148418-0', 'Cho Tôi Xem Tay', 'Trần Duy Tân', 'NXB Trẻ', '2022-07-15', 'vi', 150, 'PHYSICAL', 30, 30, 4.1, 19, 156, 'Fiction'),
('978-602-02103-0', 'Harry Potter and the Philosopher''s Stone', 'J.K. Rowling', 'Bloomsbury', '1997-06-26', 'en', 223, 'PHYSICAL', 5, 5, 4.8, 89, 267, 'Science Fiction'),
('978-0-7432-8421-9', 'The Hobbit', 'J.R.R. Tolkien', 'George Allen & Unwin', '1937-09-21', 'en', 310, 'PHYSICAL', 12, 12, 4.6, 45, 189, 'Science Fiction'),
('978-0-06-181965-4', 'Dune', 'Frank Herbert', 'Chilton Books', '1965-08-01', 'en', 412, 'PHYSICAL', 8, 8, 4.3, 67, 234, 'Science Fiction')
ON CONFLICT (isbn) DO NOTHING;

INSERT INTO user_profiles (user_id, full_name, email, bio, membership_level, points) VALUES
(4, 'Lê Văn C', 'user1@library.com', 'Thủ thư với 10 năm kinh nghiệm', 'SILVER', 250),
(5, 'Phạm Thị D', 'user2@library.com', 'Sinh viên năm 4', 'BRONZE', 120),
(6, 'Hoàng Văn E', 'user3@library.com', 'Người đi làm công ty', 'GOLD', 580),
(7, 'Nguyễn Thị F', 'user4@library.com', 'Học sinh cấp 3', 'SILVER', 340),
(8, 'Trần Văn G', 'user5@library.com', 'Gia đình', 'PLATINUM', 890)
ON CONFLICT (user_id) DO NOTHING;
