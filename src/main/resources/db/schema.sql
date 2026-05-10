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
    rejection_reason TEXT,
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
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ON_HOLD', 'FULFILLED', 'CANCELLED', 'EXPIRED')),
    priority INTEGER DEFAULT 1 CHECK (priority BETWEEN 1 AND 3),
    notification_sent BOOLEAN DEFAULT FALSE,
    fulfilled_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    on_hold_at TIMESTAMP,
    hold_expires_at TIMESTAMP,
    notes TEXT,
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
    card_expiry_date DATE,
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
    reviewer_name VARCHAR(255) NOT NULL DEFAULT '',
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(255),
    content TEXT NOT NULL,
    helpful_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PUBLISHED' CHECK (status IN ('PUBLISHED', 'HIDDEN', 'DELETED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, book_id)
);

-- Migration: add reviewer_name if missing, drop verified_purchase if present (for existing DBs)
ALTER TABLE book_reviews ADD COLUMN IF NOT EXISTS reviewer_name VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE book_reviews DROP COLUMN IF EXISTS verified_purchase;

CREATE TABLE IF NOT EXISTS review_votes (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT REFERENCES book_reviews(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    vote_type VARCHAR(10) NOT NULL CHECK (vote_type IN ('LIKE', 'DISLIKE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(review_id, user_id)
);

CREATE TABLE IF NOT EXISTS review_comments (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT REFERENCES book_reviews(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    commenter_name VARCHAR(255) NOT NULL,
    parent_id BIGINT REFERENCES review_comments(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    like_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PUBLISHED' CHECK (status IN ('PUBLISHED', 'HIDDEN', 'DELETED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_review_comments_review_id ON review_comments(review_id);
CREATE INDEX IF NOT EXISTS idx_review_comments_parent_id ON review_comments(parent_id);
CREATE INDEX IF NOT EXISTS idx_review_votes_review_id ON review_votes(review_id);

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
('admin@library.com', '$2a$10$yokV23U/FbikNLBJWVeb5OzIWempu27HD8D7Fybqjb54f.gcPx.oG', 'System Administrator', '0900000000', 'ACTIVE'),
('librarian1@library.com', '$2a$10$QMCzByGVtR2rJVRndC0Woee2dwQvG2bSa6uEpeqaD.1bHoYszZHJO', 'Nguyen Van A', '0901234567', 'ACTIVE'),
('librarian2@library.com', '$2a$10$QMCzByGVtR2rJVRndC0Woee2dwQvG2bSa6uEpeqaD.1bHoYszZHJO', 'Tran Thi B', '0902345678', 'ACTIVE'),
('user1@library.com', '$2a$10$jkMxBiQJTtA7F2MITBwCYuUHhkAYRLE/xaE7MOKDHbsGfpr5pBnyC', 'Le Van C', '0903456789', 'ACTIVE'),
('user2@library.com', '$2a$10$jkMxBiQJTtA7F2MITBwCYuUHhkAYRLE/xaE7MOKDHbsGfpr5pBnyC', 'Pham Thi D', '0904567890', 'ACTIVE'),
('user3@library.com', '$2a$10$jkMxBiQJTtA7F2MITBwCYuUHhkAYRLE/xaE7MOKDHbsGfpr5pBnyC', 'Hoang Van E', '0905678901', 'ACTIVE'),
('user4@library.com', '$2a$10$jkMxBiQJTtA7F2MITBwCYuUHhkAYRLE/xaE7MOKDHbsGfpr5pBnyC', 'Nguyen Thi F', '0906789012', 'ACTIVE'),
('user5@library.com', '$2a$10$jkMxBiQJTtA7F2MITBwCYuUHhkAYRLE/xaE7MOKDHbsGfpr5pBnyC', 'Tran Van G', '0907890123', 'ACTIVE'),
('user@library.com', '$2a$10$jkMxBiQJTtA7F2MITBwCYuUHhkAYRLE/xaE7MOKDHbsGfpr5pBnyC', 'Test User', '0908000000', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 4), (2, 3), (3, 3), (4, 2), (5, 2), (6, 2), (7, 2), (8, 2), (9, 2)
ON CONFLICT DO NOTHING;

INSERT INTO categories (name, description) VALUES
('Văn học Việt Nam', 'Tác phẩm văn học của các tác giả Việt Nam'),
('Văn học nước ngoài', 'Tác phẩm văn học dịch từ nước ngoài'),
('Khoa học - Công nghệ', 'Sách về khoa học và công nghệ thông tin'),
('Kinh tế - Kinh doanh', 'Sách kinh tế, quản trị và kinh doanh'),
('Lịch sử - Địa lý', 'Lịch sử Việt Nam và thế giới'),
('Tâm lý - Kỹ năng sống', 'Sách phát triển bản thân và kỹ năng sống'),
('Thiếu nhi', 'Sách dành cho trẻ em và thiếu niên'),
('Giáo khoa - Tham khảo', 'Sách giáo khoa và tài liệu học tập')
ON CONFLICT (name) DO NOTHING;

INSERT INTO books (isbn, title, author, publisher, publication_date, language, pages, format, total_quantity, available_qty, average_rating, rating_count, view_count, category, description) VALUES
-- Văn học Việt Nam
('978-604-2-27401-1', 'Chí Phèo', 'Nam Cao', 'NXB Văn Học', '2018-03-10', 'vi', 250, 'PHYSICAL', 15, 13, 4.80, 25, 389, 'Văn học Việt Nam', 'Kiệt tác của Nam Cao về số phận người nông dân Việt Nam trước Cách mạng, qua nhân vật Chí Phèo bị tha hóa bởi xã hội phong kiến.'),
('978-604-2-27402-2', 'Lão Hạc', 'Nam Cao', 'NXB Văn Học', '2017-11-25', 'vi', 200, 'PHYSICAL', 12, 10, 4.60, 18, 267, 'Văn học Việt Nam', 'Truyện ngắn cảm động về tình phụ tử và nhân cách cao đẹp của người nông dân nghèo trong xã hội cũ.'),
('978-604-2-27403-3', 'Số Đỏ', 'Vũ Trọng Phụng', 'NXB Văn Học', '2019-08-20', 'vi', 320, 'PHYSICAL', 8, 7, 4.70, 30, 445, 'Văn học Việt Nam', 'Tiểu thuyết trào phúng xuất sắc của Vũ Trọng Phụng, phê phán xã hội thực dân nửa phong kiến Việt Nam qua nhân vật Xuân Tóc Đỏ.'),
('978-604-2-27404-4', 'Tắt Đèn', 'Ngô Tất Tố', 'NXB Văn Học', '2015-09-20', 'vi', 280, 'PHYSICAL', 20, 18, 4.50, 22, 312, 'Văn học Việt Nam', 'Tiểu thuyết hiện thực phê phán về cuộc sống cùng khổ của người nông dân Việt Nam dưới ách thực dân phong kiến.'),
('978-604-2-27405-5', 'Vợ Nhặt', 'Kim Lân', 'NXB Văn Học', '2021-02-14', 'vi', 180, 'PHYSICAL', 18, 16, 4.40, 20, 278, 'Văn học Việt Nam', 'Truyện ngắn đặc sắc về nạn đói 1945, thể hiện tinh thần lạc quan và khát vọng sống của con người Việt Nam.'),
('978-604-2-27406-6', 'Dế Mèn Phiêu Lưu Ký', 'Tô Hoài', 'NXB Kim Đồng', '2020-05-15', 'vi', 320, 'PHYSICAL', 25, 23, 4.80, 45, 567, 'Văn học Việt Nam', 'Tác phẩm thiếu nhi kinh điển của Tô Hoài, kể về chuyến phiêu lưu của chú Dế Mèn qua thế giới côn trùng đầy màu sắc.'),
('978-604-2-27407-7', 'Cho Tôi Xin Một Vé Đi Tuổi Thơ', 'Nguyễn Nhật Ánh', 'NXB Trẻ', '2022-07-15', 'vi', 250, 'PHYSICAL', 30, 28, 4.70, 52, 623, 'Văn học Việt Nam', 'Cuốn sách nhẹ nhàng và cảm xúc của Nguyễn Nhật Ánh về ký ức tuổi thơ hồn nhiên và những kỷ niệm đẹp đẽ.'),
('978-604-2-27408-8', 'Mắt Biếc', 'Nguyễn Nhật Ánh', 'NXB Trẻ', '2016-06-30', 'vi', 290, 'PHYSICAL', 20, 18, 4.90, 67, 789, 'Văn học Việt Nam', 'Câu chuyện tình yêu đẹp và buồn của Ngạn và Hà Lan từ thời thơ ấu đến trưởng thành, đầy tiếc nuối và xúc động.'),
('978-604-2-27409-9', 'Đắc Nhân Tâm', 'Dale Carnegie (dịch)', 'NXB Tổng hợp TP.HCM', '2014-04-25', 'vi', 350, 'PHYSICAL', 15, 13, 4.60, 88, 934, 'Tâm lý - Kỹ năng sống', 'Cuốn sách kinh điển về nghệ thuật ứng xử và giao tiếp, giúp bạn chiếm được cảm tình của mọi người.'),
-- Văn học nước ngoài
('978-0-7432-8421-9', 'The Hobbit', 'J.R.R. Tolkien', 'George Allen & Unwin', '1937-09-21', 'en', 310, 'PHYSICAL', 12, 10, 4.60, 45, 489, 'Văn học nước ngoài', 'Classic fantasy novel following the journey of Bilbo Baggins, a hobbit who embarks on an unexpected adventure.'),
('978-0-06-181965-4', 'Dune', 'Frank Herbert', 'Chilton Books', '1965-08-01', 'en', 412, 'PHYSICAL', 8, 7, 4.50, 67, 534, 'Văn học nước ngoài', 'Epic science fiction saga set in the far future amidst a feudal interstellar society. A landmark of speculative fiction.'),
('978-0-14-028329-7', 'Animal Farm', 'George Orwell', 'Secker & Warburg', '1945-08-17', 'en', 112, 'PHYSICAL', 20, 18, 4.40, 55, 412, 'Văn học nước ngoài', 'A satirical allegorical novella reflecting events leading up to the Russian Revolution of 1917 and the Stalinist era.'),
('978-0-7432-7356-5', 'The Alchemist', 'Paulo Coelho', 'HarperOne', '1988-01-01', 'en', 208, 'PHYSICAL', 15, 13, 4.30, 72, 678, 'Văn học nước ngoài', 'A philosophical novel about a young Andalusian shepherd who dreams of discovering a worldly treasure located near the Egyptian pyramids.'),
('978-0-385-33348-1', 'The Kite Runner', 'Khaled Hosseini', 'Riverhead Books', '2003-05-29', 'en', 372, 'PHYSICAL', 10, 9, 4.70, 48, 523, 'Văn học nước ngoài', 'A story of friendship, betrayal, guilt, and redemption set against the backdrop of Afghanistan from the 1970s to the early 2000s.'),
-- Khoa học - Công nghệ
('978-0-13-235088-4', 'Clean Code', 'Robert C. Martin', 'Prentice Hall', '2008-08-01', 'en', 431, 'PHYSICAL', 10, 9, 4.80, 93, 867, 'Khoa học - Công nghệ', 'A handbook of agile software craftsmanship teaching programmers how to write clean, readable, and maintainable code.'),
('978-0-201-63361-0', 'Design Patterns', 'Gang of Four', 'Addison-Wesley', '1994-11-10', 'en', 395, 'PHYSICAL', 8, 7, 4.50, 61, 645, 'Khoa học - Công nghệ', 'The classic software engineering book describing 23 common software design patterns with reusable object-oriented solutions.'),
('978-604-2-27410-0', 'Lập Trình Python Cơ Bản', 'Nguyễn Văn Hiệp', 'NXB Thông tin và Truyền thông', '2021-03-15', 'vi', 380, 'PHYSICAL', 20, 18, 4.20, 34, 456, 'Khoa học - Công nghệ', 'Hướng dẫn lập trình Python từ cơ bản đến nâng cao với nhiều bài tập thực hành và ví dụ minh họa chi tiết.'),
-- Kinh tế - Kinh doanh
('978-0-06-096985-8', 'Rich Dad Poor Dad', 'Robert T. Kiyosaki', 'Warner Books', '1997-04-01', 'en', 207, 'PHYSICAL', 15, 14, 4.40, 76, 812, 'Kinh tế - Kinh doanh', 'Personal finance classic that challenges conventional thinking about money, investing, and building wealth.'),
('978-0-385-49376-5', 'The 7 Habits of Highly Effective People', 'Stephen R. Covey', 'Free Press', '1989-08-15', 'en', 372, 'PHYSICAL', 12, 11, 4.50, 58, 723, 'Kinh tế - Kinh doanh', 'A business and self-help book that presents an approach to being effective in attaining goals by aligning oneself to universal principles.'),
('978-604-2-27411-1', 'Khởi Nghiệp Tinh Gọn', 'Eric Ries (dịch)', 'NXB Lao Động', '2019-06-10', 'vi', 290, 'PHYSICAL', 10, 9, 4.30, 27, 389, 'Kinh tế - Kinh doanh', 'Phương pháp tiếp cận hiện đại cho việc xây dựng startup, tập trung vào chu kỳ build-measure-learn để giảm thiểu lãng phí.'),
-- Lịch sử - Địa lý
('978-604-2-27412-2', 'Lịch Sử Việt Nam', 'Nguyễn Khắc Viện', 'NXB Thế Giới', '2017-09-02', 'vi', 520, 'PHYSICAL', 10, 9, 4.60, 31, 378, 'Lịch sử - Địa lý', 'Tổng hợp toàn diện lịch sử Việt Nam từ thời dựng nước đến hiện đại, được viết bởi nhà sử học uy tín.'),
('978-0-14-028428-7', 'Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', 'Harper', '2015-02-10', 'en', 443, 'PHYSICAL', 8, 7, 4.70, 84, 934, 'Lịch sử - Địa lý', 'A sweeping narrative of humanity''s creation and evolution that explores how biology and history have defined us and enhanced our understanding of what it means to be human.'),
-- Tâm lý - Kỹ năng sống
('978-0-7432-3705-8', 'Think and Grow Rich', 'Napoleon Hill', 'The Ralston Society', '1937-03-26', 'en', 238, 'PHYSICAL', 12, 11, 4.30, 49, 567, 'Tâm lý - Kỹ năng sống', 'Classic self-help book examining the psychological power of thought and the brain in the process of furthering your career for both monetary and personal satisfaction.'),
('978-604-2-27413-3', 'Tuổi Trẻ Đáng Giá Bao Nhiêu', 'Rosie Nguyễn', 'NXB Hội Nhà Văn', '2018-01-20', 'vi', 220, 'PHYSICAL', 25, 23, 4.10, 43, 512, 'Tâm lý - Kỹ năng sống', 'Cuốn sách truyền cảm hứng cho giới trẻ về việc xây dựng bản thân, theo đuổi ước mơ và sống có ý nghĩa.'),
-- Thiếu nhi
('978-0-590-35340-3', 'Harry Potter and the Philosopher''s Stone', 'J.K. Rowling', 'Bloomsbury', '1997-06-26', 'en', 223, 'PHYSICAL', 10, 9, 4.90, 112, 1023, 'Thiếu nhi', 'The first book in the Harry Potter series, following the young wizard Harry Potter as he discovers his magical heritage and begins his education at Hogwarts.'),
('978-604-2-27414-4', 'Doraemon - Tập 1', 'Fujiko F. Fujio', 'NXB Kim Đồng', '2010-01-01', 'vi', 180, 'PHYSICAL', 30, 28, 4.80, 78, 834, 'Thiếu nhi', 'Bộ truyện tranh nổi tiếng về chú mèo máy Doraemon đến từ tương lai giúp đỡ cậu bé Nobita.')
ON CONFLICT (isbn) DO NOTHING;

-- Book categories mapping
INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b JOIN categories c ON c.name = 'Văn học Việt Nam'
WHERE b.isbn IN ('978-604-2-27401-1','978-604-2-27402-2','978-604-2-27403-3','978-604-2-27404-4','978-604-2-27405-5','978-604-2-27406-6','978-604-2-27407-7','978-604-2-27408-8')
ON CONFLICT DO NOTHING;

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b JOIN categories c ON c.name = 'Văn học nước ngoài'
WHERE b.isbn IN ('978-0-7432-8421-9','978-0-06-181965-4','978-0-14-028329-7','978-0-7432-7356-5','978-0-385-33348-1')
ON CONFLICT DO NOTHING;

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b JOIN categories c ON c.name = 'Khoa học - Công nghệ'
WHERE b.isbn IN ('978-0-13-235088-4','978-0-201-63361-0','978-604-2-27410-0')
ON CONFLICT DO NOTHING;

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b JOIN categories c ON c.name = 'Kinh tế - Kinh doanh'
WHERE b.isbn IN ('978-0-06-096985-8','978-0-385-49376-5','978-604-2-27411-1')
ON CONFLICT DO NOTHING;

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b JOIN categories c ON c.name = 'Lịch sử - Địa lý'
WHERE b.isbn IN ('978-604-2-27412-2','978-0-14-028428-7')
ON CONFLICT DO NOTHING;

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b JOIN categories c ON c.name = 'Tâm lý - Kỹ năng sống'
WHERE b.isbn IN ('978-604-2-27409-9','978-0-7432-3705-8','978-604-2-27413-3')
ON CONFLICT DO NOTHING;

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b JOIN categories c ON c.name = 'Thiếu nhi'
WHERE b.isbn IN ('978-0-590-35340-3','978-604-2-27414-4','978-604-2-27406-6')
ON CONFLICT DO NOTHING;

-- Additional cross-category for Fiction overlap
INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b JOIN categories c ON c.name = 'Fiction'
WHERE b.isbn IN ('978-604-2-27401-1','978-604-2-27403-3','978-604-2-27408-8','978-0-7432-8421-9','978-0-385-33348-1')
ON CONFLICT DO NOTHING;

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b JOIN categories c ON c.name = 'Science Fiction'
WHERE b.isbn IN ('978-0-06-181965-4','978-0-590-35340-3')
ON CONFLICT DO NOTHING;

-- Book tags
INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id FROM books b JOIN tags t ON t.name = 'Classic'
WHERE b.isbn IN ('978-604-2-27401-1','978-604-2-27402-2','978-604-2-27403-3','978-604-2-27404-4','978-0-7432-8421-9','978-0-06-181965-4','978-0-14-028329-7')
ON CONFLICT DO NOTHING;

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id FROM books b JOIN tags t ON t.name = 'Bestseller'
WHERE b.isbn IN ('978-604-2-27407-7','978-604-2-27408-8','978-604-2-27409-9','978-0-7432-7356-5','978-0-590-35340-3','978-0-06-096985-8','978-0-14-028428-7')
ON CONFLICT DO NOTHING;

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id FROM books b JOIN tags t ON t.name = 'Recommended'
WHERE b.isbn IN ('978-0-13-235088-4','978-0-201-63361-0','978-0-385-49376-5','978-604-2-27412-2','978-0-14-028428-7')
ON CONFLICT DO NOTHING;

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id FROM books b JOIN tags t ON t.name = 'Educational'
WHERE b.isbn IN ('978-0-13-235088-4','978-0-201-63361-0','978-604-2-27410-0','978-604-2-27412-2')
ON CONFLICT DO NOTHING;

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id FROM books b JOIN tags t ON t.name = 'Award Winner'
WHERE b.isbn IN ('978-0-06-181965-4','978-0-385-33348-1','978-0-590-35340-3','978-604-2-27408-8')
ON CONFLICT DO NOTHING;

-- Book Reviews & Ratings
INSERT INTO book_reviews (user_id, book_id, reviewer_name, rating, title, content, helpful_count, status, created_at)
SELECT 4, b.id, (SELECT full_name FROM users WHERE id = 4), 5, 'Kiệt tác văn học Việt Nam', 'Chí Phèo là một trong những tác phẩm xuất sắc nhất của Nam Cao. Câu chuyện về sự tha hóa của con người trong xã hội cũ khiến tôi không thể ngừng đọc. Ngôn ngữ sắc bén, tâm lý nhân vật được khắc họa rất chân thực.', 12, 'PUBLISHED', NOW() - INTERVAL '30 days'
FROM books b WHERE b.isbn = '978-604-2-27401-1'
ON CONFLICT (user_id, book_id) DO NOTHING;

INSERT INTO book_reviews (user_id, book_id, reviewer_name, rating, title, content, helpful_count, status, created_at)
SELECT 5, b.id, (SELECT full_name FROM users WHERE id = 5), 4, 'Hay nhưng khá nặng nề', 'Truyện phản ánh thực tế xã hội rất tốt nhưng đôi khi đọc thấy nặng nề. Tuy nhiên đây là tác phẩm bắt buộc phải đọc để hiểu văn học Việt Nam.', 5, 'PUBLISHED', NOW() - INTERVAL '25 days'
FROM books b WHERE b.isbn = '978-604-2-27401-1'
ON CONFLICT (user_id, book_id) DO NOTHING;

INSERT INTO book_reviews (user_id, book_id, reviewer_name, rating, title, content, helpful_count, status, created_at)
SELECT 6, b.id, (SELECT full_name FROM users WHERE id = 6), 5, 'Sách đã thay đổi cách nhìn của tôi', 'Mắt Biếc là cuốn sách đầu tiên tôi đọc của Nguyễn Nhật Ánh và tôi đã khóc từ chương 3. Tình yêu trong sáng nhưng đầy bi kịch. Highly recommend cho ai yêu văn học Việt.', 18, 'PUBLISHED', NOW() - INTERVAL '20 days'
FROM books b WHERE b.isbn = '978-604-2-27408-8'
ON CONFLICT (user_id, book_id) DO NOTHING;

INSERT INTO book_reviews (user_id, book_id, reviewer_name, rating, title, content, helpful_count, status, created_at)
SELECT 7, b.id, (SELECT full_name FROM users WHERE id = 7), 5, 'Xúc động và chân thực', 'Nguyễn Nhật Ánh viết rất hay về tuổi thơ. Đọc Mắt Biếc như được sống lại những năm tháng trong sáng nhất của cuộc đời. Cốt truyện đẹp, ngôn ngữ trong sáng.', 9, 'PUBLISHED', NOW() - INTERVAL '15 days'
FROM books b WHERE b.isbn = '978-604-2-27408-8'
ON CONFLICT (user_id, book_id) DO NOTHING;

INSERT INTO book_reviews (user_id, book_id, reviewer_name, rating, title, content, helpful_count, status, created_at)
SELECT 4, b.id, (SELECT full_name FROM users WHERE id = 4), 5, 'Must-read for every developer', 'Clean Code is essential for any serious programmer. Robert Martin explains clearly how to write readable, maintainable code. This book changed how I write code every day.', 24, 'PUBLISHED', NOW() - INTERVAL '45 days'
FROM books b WHERE b.isbn = '978-0-13-235088-4'
ON CONFLICT (user_id, book_id) DO NOTHING;

INSERT INTO book_reviews (user_id, book_id, reviewer_name, rating, title, content, helpful_count, status, created_at)
SELECT 5, b.id, (SELECT full_name FROM users WHERE id = 5), 4, 'Great book with practical examples', 'Very well written with lots of real-world examples. Some chapters feel repetitive but the overall message is clear and valuable. Recommend for mid-level developers.', 11, 'PUBLISHED', NOW() - INTERVAL '40 days'
FROM books b WHERE b.isbn = '978-0-13-235088-4'
ON CONFLICT (user_id, book_id) DO NOTHING;

INSERT INTO book_reviews (user_id, book_id, reviewer_name, rating, title, content, helpful_count, status, created_at)
SELECT 6, b.id, (SELECT full_name FROM users WHERE id = 6), 5, 'Một trong những cuốn hay nhất tôi từng đọc', 'Sapiens mở ra cho tôi một cái nhìn hoàn toàn mới về lịch sử loài người. Tác giả viết rất hấp dẫn, dễ hiểu. Đây là cuốn sách tôi sẽ đọc lại nhiều lần.', 15, 'PUBLISHED', NOW() - INTERVAL '35 days'
FROM books b WHERE b.isbn = '978-0-14-028428-7'
ON CONFLICT (user_id, book_id) DO NOTHING;

INSERT INTO book_reviews (user_id, book_id, reviewer_name, rating, title, content, helpful_count, status, created_at)
SELECT 8, b.id, (SELECT full_name FROM users WHERE id = 8), 4, 'Insightful and thought-provoking', 'Harari challenges many assumptions we take for granted about human civilization. Some theories are debatable but the book makes you think deeply about our species'' journey.', 8, 'PUBLISHED', NOW() - INTERVAL '28 days'
FROM books b WHERE b.isbn = '978-0-14-028428-7'
ON CONFLICT (user_id, book_id) DO NOTHING;

INSERT INTO book_reviews (user_id, book_id, reviewer_name, rating, title, content, helpful_count, status, created_at)
SELECT 7, b.id, (SELECT full_name FROM users WHERE id = 7), 5, 'Tuổi thơ của tôi gắn liền với Doraemon', 'Doraemon là người bạn thân nhất của tôi hồi nhỏ. Mỗi câu chuyện đều có bài học ý nghĩa. Rất vui khi thư viện có bộ truyện này để các em nhỏ được đọc.', 20, 'PUBLISHED', NOW() - INTERVAL '18 days'
FROM books b WHERE b.isbn = '978-604-2-27414-4'
ON CONFLICT (user_id, book_id) DO NOTHING;

INSERT INTO book_reviews (user_id, book_id, reviewer_name, rating, title, content, helpful_count, status, created_at)
SELECT 8, b.id, (SELECT full_name FROM users WHERE id = 8), 5, 'Magical and timeless', 'Harry Potter never gets old! The story of a young boy discovering a magical world is enchanting for readers of all ages. Perfect for both children and adults who want to rediscover wonder.', 31, 'PUBLISHED', NOW() - INTERVAL '22 days'
FROM books b WHERE b.isbn = '978-0-590-35340-3'
ON CONFLICT (user_id, book_id) DO NOTHING;

INSERT INTO book_reviews (user_id, book_id, reviewer_name, rating, title, content, helpful_count, status, created_at)
SELECT 9, b.id, (SELECT full_name FROM users WHERE id = 9), 5, 'Changed my perspective on money', 'Rich Dad Poor Dad fundamentally changed how I think about money and investing. Simple concepts explained clearly. A must-read for anyone who wants financial freedom.', 14, 'PUBLISHED', NOW() - INTERVAL '50 days'
FROM books b WHERE b.isbn = '978-0-06-096985-8'
ON CONFLICT (user_id, book_id) DO NOTHING;

INSERT INTO book_reviews (user_id, book_id, reviewer_name, rating, title, content, helpful_count, status, created_at)
SELECT 8, b.id, (SELECT full_name FROM users WHERE id = 8), 4, 'Đắc Nhân Tâm thực sự hiệu quả', 'Áp dụng những nguyên tắc trong sách vào công việc thực sự thấy sự thay đổi. Mọi người xung quanh phản ứng tích cực hơn. Sách nên đọc đi đọc lại nhiều lần.', 16, 'PUBLISHED', NOW() - INTERVAL '42 days'
FROM books b WHERE b.isbn = '978-604-2-27409-9'
ON CONFLICT (user_id, book_id) DO NOTHING;

INSERT INTO book_reviews (user_id, book_id, reviewer_name, rating, title, content, helpful_count, status, created_at)
SELECT 6, b.id, (SELECT full_name FROM users WHERE id = 6), 5, 'Dế Mèn - ký ức không bao giờ phai', 'Đọc lại Dế Mèn Phiêu Lưu Ký khi đã lớn vẫn cảm thấy thú vị như hồi nhỏ. Tô Hoài đã tạo nên một thế giới côn trùng sống động và đầy màu sắc. Tuyệt vời!', 22, 'PUBLISHED', NOW() - INTERVAL '12 days'
FROM books b WHERE b.isbn = '978-604-2-27406-6'
ON CONFLICT (user_id, book_id) DO NOTHING;

-- Review votes (helpful votes)
INSERT INTO review_votes (review_id, user_id, vote_type)
SELECT r.id, 5, 'LIKE' FROM book_reviews r
JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-604-2-27401-1' AND r.user_id = 4
ON CONFLICT (review_id, user_id) DO NOTHING;

INSERT INTO review_votes (review_id, user_id, vote_type)
SELECT r.id, 6, 'LIKE' FROM book_reviews r
JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-604-2-27401-1' AND r.user_id = 4
ON CONFLICT (review_id, user_id) DO NOTHING;

INSERT INTO review_votes (review_id, user_id, vote_type)
SELECT r.id, 7, 'LIKE' FROM book_reviews r
JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-604-2-27408-8' AND r.user_id = 6
ON CONFLICT (review_id, user_id) DO NOTHING;

INSERT INTO review_votes (review_id, user_id, vote_type)
SELECT r.id, 8, 'LIKE' FROM book_reviews r
JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-0-13-235088-4' AND r.user_id = 4
ON CONFLICT (review_id, user_id) DO NOTHING;

INSERT INTO review_votes (review_id, user_id, vote_type)
SELECT r.id, 4, 'LIKE' FROM book_reviews r
JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-0-590-35340-3' AND r.user_id = 8
ON CONFLICT (review_id, user_id) DO NOTHING;

INSERT INTO review_votes (review_id, user_id, vote_type)
SELECT r.id, 9, 'LIKE' FROM book_reviews r
JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-0-06-096985-8' AND r.user_id = 9
ON CONFLICT (review_id, user_id) DO NOTHING;

-- Review Comments seed data
INSERT INTO review_comments (review_id, user_id, commenter_name, content, like_count, status, created_at)
SELECT r.id, 7, (SELECT full_name FROM users WHERE id = 7),
  'Tôi cũng cảm thấy như vậy! Chí Phèo là tác phẩm không thể bỏ qua trong văn học Việt Nam.', 3, 'PUBLISHED', NOW() - INTERVAL '28 days'
FROM book_reviews r JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-604-2-27401-1' AND r.user_id = 4;

INSERT INTO review_comments (review_id, user_id, commenter_name, content, like_count, status, created_at)
SELECT r.id, 8, (SELECT full_name FROM users WHERE id = 8),
  'Đúng vậy, đây là kiệt tác của Nam Cao. Ngôn ngữ và tâm lý nhân vật rất sâu sắc.', 2, 'PUBLISHED', NOW() - INTERVAL '26 days'
FROM book_reviews r JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-604-2-27401-1' AND r.user_id = 4;

INSERT INTO review_comments (review_id, user_id, commenter_name, content, like_count, status, created_at)
SELECT r.id, 9, (SELECT full_name FROM users WHERE id = 9),
  'Mắt Biếc làm tôi khóc từ đầu đến cuối. Nguyễn Nhật Ánh viết về tình yêu tuổi học trò thật xúc động.', 5, 'PUBLISHED', NOW() - INTERVAL '18 days'
FROM book_reviews r JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-604-2-27408-8' AND r.user_id = 6;

INSERT INTO review_comments (review_id, user_id, commenter_name, content, like_count, status, created_at)
SELECT r.id, 4, (SELECT full_name FROM users WHERE id = 4),
  'Đồng ý! Câu chuyện Ngạn và Hà Lan thật sự để lại nhiều tiếc nuối. Một trong những sách hay nhất của NNA.', 4, 'PUBLISHED', NOW() - INTERVAL '14 days'
FROM book_reviews r JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-604-2-27408-8' AND r.user_id = 7;

INSERT INTO review_comments (review_id, user_id, commenter_name, content, like_count, status, created_at)
SELECT r.id, 6, (SELECT full_name FROM users WHERE id = 6),
  'Clean Code changed my career! I apply its principles every day. Every developer should read this.', 7, 'PUBLISHED', NOW() - INTERVAL '42 days'
FROM book_reviews r JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-0-13-235088-4' AND r.user_id = 4;

INSERT INTO review_comments (review_id, user_id, commenter_name, content, like_count, status, created_at)
SELECT r.id, 7, (SELECT full_name FROM users WHERE id = 7),
  'Agreed! Some examples are Java-centric but the principles are universal. A must for any serious developer.', 3, 'PUBLISHED', NOW() - INTERVAL '38 days'
FROM book_reviews r JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-0-13-235088-4' AND r.user_id = 5;

INSERT INTO review_comments (review_id, user_id, commenter_name, content, like_count, status, created_at)
SELECT r.id, 5, (SELECT full_name FROM users WHERE id = 5),
  'Sapiens là cuốn sách mở mang tầm nhìn nhất tôi từng đọc. Harari giải thích mọi thứ rất dễ hiểu.', 6, 'PUBLISHED', NOW() - INTERVAL '32 days'
FROM book_reviews r JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-0-14-028428-7' AND r.user_id = 6;

INSERT INTO review_comments (review_id, user_id, commenter_name, content, like_count, status, created_at)
SELECT r.id, 9, (SELECT full_name FROM users WHERE id = 9),
  'Harry Potter never gets old! I grew up reading this series and it still feels magical every time.', 8, 'PUBLISHED', NOW() - INTERVAL '20 days'
FROM book_reviews r JOIN books b ON b.id = r.book_id
WHERE b.isbn = '978-0-590-35340-3' AND r.user_id = 8;

-- Update average_rating and rating_count from seeded reviews
UPDATE books SET
    rating_count = (SELECT COUNT(*) FROM book_reviews br WHERE br.book_id = books.id AND br.status = 'PUBLISHED'),
    average_rating = COALESCE((SELECT ROUND(AVG(br.rating)::numeric, 2) FROM book_reviews br WHERE br.book_id = books.id AND br.status = 'PUBLISHED'), average_rating)
WHERE id IN (SELECT DISTINCT book_id FROM book_reviews);

INSERT INTO user_profiles (user_id, full_name, email, bio, membership_level, points) VALUES
(1, 'System Administrator', 'admin@library.com', 'System Administrator', 'PLATINUM', 9999),
(2, 'Nguyen Van A', 'librarian1@library.com', 'Librarian', 'GOLD', 500),
(3, 'Tran Thi B', 'librarian2@library.com', 'Librarian', 'GOLD', 500),
(4, 'Le Van C', 'user1@library.com', 'Regular reader', 'SILVER', 250),
(5, 'Pham Thi D', 'user2@library.com', 'Student', 'BRONZE', 120),
(6, 'Hoang Van E', 'user3@library.com', 'Working professional', 'GOLD', 580),
(7, 'Nguyen Thi F', 'user4@library.com', 'High school student', 'SILVER', 340),
(8, 'Tran Van G', 'user5@library.com', 'Family member', 'PLATINUM', 890),
(9, 'Test User', 'user@library.com', 'Test account', 'BRONZE', 0)
ON CONFLICT (user_id) DO NOTHING;
