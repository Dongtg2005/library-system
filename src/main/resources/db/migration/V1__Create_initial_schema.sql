-- Library Management System - Initial Schema
-- Version: 1.0
-- Description: Complete database schema for enterprise library management

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================================
-- 1. AUTHENTICATION & AUTHORIZATION
-- ========================================

-- Users table (extends current User entity)
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

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL CHECK (name IN ('GUEST', 'USER', 'LIBRARIAN', 'ADMIN')),
    description TEXT,
    permissions JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles junction table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT REFERENCES users(id),
    PRIMARY KEY (user_id, role_id)
);

-- Refresh tokens for JWT
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

-- Books table (extends current Book entity)
ALTER TABLE books ADD COLUMN IF NOT EXISTS (
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
    borrowed_quantity INTEGER DEFAULT 0,
    reserved_quantity INTEGER DEFAULT 0,
    average_rating DECIMAL(3,2) DEFAULT 0.0,
    rating_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0
);

-- Categories
CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    parent_id INTEGER REFERENCES categories(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Book categories junction
CREATE TABLE IF NOT EXISTS book_categories (
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, category_id)
);

-- Tags for better search
CREATE TABLE IF NOT EXISTS tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    color VARCHAR(7) DEFAULT '#007bff'
);

-- Book tags junction
CREATE TABLE IF NOT EXISTS book_tags (
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    tag_id INTEGER REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, tag_id)
);

-- ========================================
-- 3. BORROW MANAGEMENT
-- ========================================

-- Extend current borrow_records table
ALTER TABLE borrow_records ADD COLUMN IF NOT EXISTS (
    reservation_id UUID,
    fine_amount DECIMAL(10,2) DEFAULT 0.00,
    fine_paid BOOLEAN DEFAULT FALSE,
    fine_paid_at TIMESTAMP,
    notes TEXT,
    librarian_id BIGINT REFERENCES users(id),
    return_librarian_id BIGINT REFERENCES users(id),
    renewal_count INTEGER DEFAULT 0,
    max_renewals INTEGER DEFAULT 3
);

-- Reservations system
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

-- ========================================
-- 4. USER PROFILES & ACTIVITY
-- ========================================

-- Extend current user_profiles table
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS (
    bio TEXT,
    favorite_genres TEXT[],
    reading_preferences JSONB,
    notification_settings JSONB,
    privacy_settings JSONB,
    total_books_read INTEGER DEFAULT 0,
    total_pages_read BIGINT DEFAULT 0,
    membership_level VARCHAR(20) DEFAULT 'BRONZE' CHECK (membership_level IN ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM')),
    points INTEGER DEFAULT 0
);

-- Reading history
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

-- User activity log
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

-- Book reviews
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

-- Review helpful votes
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

-- User favorites
CREATE TABLE IF NOT EXISTS user_favorites (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, book_id)
);

-- User wishlist
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

-- Notifications
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

-- Notification preferences
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

-- System settings
CREATE TABLE IF NOT EXISTS system_settings (
    key VARCHAR(100) PRIMARY KEY,
    value TEXT,
    description TEXT,
    data_type VARCHAR(20) DEFAULT 'STRING' CHECK (data_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'JSON')),
    updated_by BIGINT REFERENCES users(id),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Analytics data
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

-- Popular books cache
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

-- User-related indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- Book-related indexes
CREATE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn);
CREATE INDEX IF NOT EXISTS idx_books_title ON books USING gin(to_tsvector('english', title));
CREATE INDEX IF NOT EXISTS idx_books_author ON books(author);
CREATE INDEX IF NOT EXISTS idx_books_status ON books(status);
CREATE INDEX IF NOT EXISTS idx_books_available ON books(available_quantity) WHERE available_quantity > 0;
CREATE INDEX IF NOT EXISTS idx_books_rating ON books(average_rating DESC, rating_count DESC);

-- Borrow-related indexes
CREATE INDEX IF NOT EXISTS idx_borrow_records_user ON borrow_records(member_id);
CREATE INDEX IF NOT EXISTS idx_borrow_records_book ON borrow_records(book_id);
CREATE INDEX IF NOT EXISTS idx_borrow_records_status ON borrow_records(borrow_status);
CREATE INDEX IF NOT EXISTS idx_borrow_records_due_date ON borrow_records(due_date) WHERE borrow_status = 'ACTIVE';

-- Review indexes
CREATE INDEX IF NOT EXISTS idx_book_reviews_book ON book_reviews(book_id);
CREATE INDEX IF NOT EXISTS idx_book_reviews_rating ON book_reviews(rating DESC);
CREATE INDEX IF NOT EXISTS idx_book_reviews_created ON book_reviews(created_at DESC);

-- Notification indexes
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id, read);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type, created_at);

-- ========================================
-- 10. INITIAL DATA
-- ========================================

-- Insert default roles
INSERT INTO roles (name, description, permissions) VALUES
('GUEST', 'Guest user with limited access', '{"READ_BOOKS": true, "VIEW_BOOK_DETAILS": true}'),
('USER', 'Regular user', '{"READ_BOOKS": true, "VIEW_BOOK_DETAILS": true, "BORROW_BOOKS": true, "RETURN_BOOKS": true, "RATE_BOOKS": true, "MANAGE_PROFILE": true, "VIEW_HISTORY": true}'),
('LIBRARIAN', 'Library staff', '{"READ_BOOKS": true, "VIEW_BOOK_DETAILS": true, "BORROW_BOOKS": true, "RETURN_BOOKS": true, "RATE_BOOKS": true, "MANAGE_PROFILE": true, "VIEW_HISTORY": true, "MANAGE_BOOKS": true, "MANAGE_USERS": true, "APPROVE_BORROWS": true, "VIEW_REPORTS": true}'),
('ADMIN', 'System administrator', '{"ALL_PERMISSIONS": true}')
ON CONFLICT (name) DO NOTHING;

-- Insert default categories
INSERT INTO categories (name, description) VALUES
('Fiction', 'Fictional literature and stories'),
('Non-Fiction', 'Non-fictional works including biographies, history, science'),
('Science Fiction', 'Science fiction and fantasy works'),
('Romance', 'Romantic novels and stories'),
('Mystery', 'Mystery and thriller books'),
('Biography', 'Life stories and autobiographies'),
('History', 'Historical accounts and documentaries'),
('Science', 'Scientific and technical books'),
('Technology', 'Computer science and technology books'),
('Business', 'Business, finance, and self-help books'),
('Children', "Children's books and young adult literature")
ON CONFLICT (name) DO NOTHING;

-- Insert default tags
INSERT INTO tags (name, color) VALUES
('Bestseller', '#ff6b6b'),
('New Release', '#4caf50'),
('Award Winner', '#ff9800'),
('Classic', '#9c27b0'),
('Recommended', '#2196f3'),
('Trending', '#ff5722'),
('Educational', '#00bcd4')
ON CONFLICT (name) DO NOTHING;

-- Insert system settings
INSERT INTO system_settings (key, value, description, data_type) VALUES
('max_borrow_days', '14', 'Maximum days for book borrowing', 'NUMBER'),
('max_borrow_limit', '5', 'Maximum books per user', 'NUMBER'),
('fine_per_day', '1000.00', 'Fine amount per day in VND', 'NUMBER'),
('max_fine', '50000.00', 'Maximum fine amount in VND', 'NUMBER'),
('reservation_expiry_hours', '24', 'Reservation expiry in hours', 'NUMBER'),
('enable_notifications', 'true', 'Enable system notifications', 'BOOLEAN'),
('enable_reviews', 'true', 'Enable book reviews', 'BOOLEAN'),
('enable_ratings', 'true', 'Enable book ratings', 'BOOLEAN')
ON CONFLICT (key) DO NOTHING;
