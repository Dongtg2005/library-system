# Library Management System - Database Schema Design

## Architecture Overview
- **Database**: PostgreSQL 15+
- **ORM**: Spring Data JPA with Hibernate
- **Migration**: Flyway for version control
- **Connection Pool**: HikariCP
- **Caching**: Redis for frequently accessed data

## Core Tables

### 1. Authentication & Authorization

```sql
-- Users table (extends current User entity)
CREATE TABLE users (
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
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, SUSPENDED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL, -- GUEST, USER, LIBRARIAN, ADMIN
    description TEXT,
    permissions JSONB, -- Store permissions as JSON
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles junction table
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT REFERENCES users(id),
    PRIMARY KEY (user_id, role_id)
);

-- Refresh tokens for JWT
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2. Book Management

```sql
-- Books table (extends current Book entity)
CREATE TABLE books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    isbn VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(500) NOT NULL,
    subtitle VARCHAR(500),
    description TEXT,
    author VARCHAR(255) NOT NULL,
    publisher VARCHAR(255),
    publication_date DATE,
    language VARCHAR(10) DEFAULT 'vi',
    pages INTEGER,
    format VARCHAR(20) DEFAULT 'PHYSICAL', -- PHYSICAL, EBOOK, AUDIOBOOK
    file_url VARCHAR(500), -- For ebooks
    file_size BIGINT, -- For ebooks in bytes
    cover_image_url VARCHAR(500),
    total_quantity INTEGER DEFAULT 1 NOT NULL,
    available_quantity INTEGER DEFAULT 1 NOT NULL,
    borrowed_quantity INTEGER DEFAULT 0,
    reserved_quantity INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'AVAILABLE', -- AVAILABLE, OUT_OF_STOCK, ARCHIVED, DAMAGED
    average_rating DECIMAL(3,2) DEFAULT 0.0,
    rating_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categories
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    parent_id INTEGER REFERENCES categories(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Book categories junction
CREATE TABLE book_categories (
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, category_id)
);

-- Tags for better search
CREATE TABLE tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    color VARCHAR(7) DEFAULT '#007bff' -- Hex color for UI
);

-- Book tags junction
CREATE TABLE book_tags (
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    tag_id INTEGER REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, tag_id)
);
```

### 3. Borrow Management

```sql
-- Extend current borrow_records table
ALTER TABLE borrow_records ADD COLUMN IF NOT EXISTS (
    reservation_id UUID,
    fine_amount DECIMAL(10,2) DEFAULT 0.00,
    fine_paid BOOLEAN DEFAULT FALSE,
    fine_paid_at TIMESTAMP,
    notes TEXT,
    librarian_id BIGINT REFERENCES users(id), -- Who processed the borrow
    return_librarian_id BIGINT REFERENCES users(id), -- Who processed the return
    renewal_count INTEGER DEFAULT 0,
    max_renewals INTEGER DEFAULT 3
);

-- Reservations system
CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    reserved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL, -- Reservation expires after 24 hours
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, FULFILLED, CANCELLED, EXPIRED
    priority INTEGER DEFAULT 1, -- 1=Normal, 2=High, 3=Urgent
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Fine rules (extends current borrow_policies)
ALTER TABLE borrow_policies ADD COLUMN IF NOT EXISTS (
    fine_per_day DECIMAL(10,2) DEFAULT 1000.00, -- VND per day
    max_fine DECIMAL(10,2) DEFAULT 50000.00, -- Maximum fine amount
    grace_period_days INTEGER DEFAULT 0 -- Days before fine starts
);
```

### 4. User Profiles & Activity

```sql
-- User profiles (extends current user_profiles)
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS (
    bio TEXT,
    favorite_genres TEXT[], -- PostgreSQL array
    reading_preferences JSONB, -- Store reading preferences
    notification_settings JSONB,
    privacy_settings JSONB,
    total_books_read INTEGER DEFAULT 0,
    total_pages_read BIGINT DEFAULT 0,
    membership_level VARCHAR(20) DEFAULT 'BRONZE', -- BRONZE, SILVER, GOLD, PLATINUM
    points INTEGER DEFAULT 0, -- Loyalty points
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Reading history
CREATE TABLE reading_history (
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
CREATE TABLE user_activities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL, -- LOGIN, BORROW, RETURN, REVIEW, etc.
    resource_type VARCHAR(50), -- BOOK, USER, etc.
    resource_id UUID,
    description TEXT,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 5. Reviews & Ratings

```sql
-- Book reviews
CREATE TABLE book_reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(255),
    content TEXT NOT NULL,
    helpful_count INTEGER DEFAULT 0,
    verified_purchase BOOLEAN DEFAULT FALSE, -- For verified purchase reviews
    status VARCHAR(20) DEFAULT 'PUBLISHED', -- PUBLISHED, HIDDEN, DELETED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, book_id) -- One review per user per book
);

-- Review helpful votes
CREATE TABLE review_votes (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT REFERENCES book_reviews(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    helpful BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(review_id, user_id)
);
```

### 6. Favorites & Wishlist

```sql
-- User favorites
CREATE TABLE user_favorites (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, book_id)
);

-- User wishlist
CREATE TABLE user_wishlist (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    priority INTEGER DEFAULT 1, -- 1=Low, 2=Medium, 3=High
    notification_sent BOOLEAN DEFAULT FALSE, -- Notify when available
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, book_id)
);
```

### 7. Notifications

```sql
-- Notifications
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL, -- DUE_SOON, OVERDUE, AVAILABLE, etc.
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
CREATE TABLE notification_preferences (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    notification_type VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    channel VARCHAR(20) DEFAULT 'EMAIL', -- EMAIL, PUSH, SMS, IN_APP
    PRIMARY KEY (user_id, notification_type)
);
```

### 8. System Configuration & Analytics

```sql
-- System settings
CREATE TABLE system_settings (
    key VARCHAR(100) PRIMARY KEY,
    value TEXT,
    description TEXT,
    data_type VARCHAR(20) DEFAULT 'STRING', -- STRING, NUMBER, BOOLEAN, JSON
    updated_by BIGINT REFERENCES users(id),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Analytics data
CREATE TABLE analytics_events (
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
CREATE TABLE popular_books (
    book_id UUID PRIMARY KEY REFERENCES books(id) ON DELETE CASCADE,
    borrow_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    search_count INTEGER DEFAULT 0,
    last_calculated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Indexes for Performance

```sql
-- User-related indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Book-related indexes
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_title ON books USING gin(to_tsvector('english', title));
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_status ON books(status);
CREATE INDEX idx_books_available ON books(available_quantity) WHERE available_quantity > 0;
CREATE INDEX idx_books_rating ON books(average_rating DESC, rating_count DESC);

-- Borrow-related indexes
CREATE INDEX idx_borrow_records_user ON borrow_records(member_id);
CREATE INDEX idx_borrow_records_book ON borrow_records(book_id);
CREATE INDEX idx_borrow_records_status ON borrow_records(borrow_status);
CREATE INDEX idx_borrow_records_due_date ON borrow_records(due_date) WHERE borrow_status = 'ACTIVE';

-- Review indexes
CREATE INDEX idx_book_reviews_book ON book_reviews(book_id);
CREATE INDEX idx_book_reviews_rating ON book_reviews(rating DESC);
CREATE INDEX idx_book_reviews_created ON book_reviews(created_at DESC);

-- Notification indexes
CREATE INDEX idx_notifications_user ON notifications(user_id, read);
CREATE INDEX idx_notifications_type ON notifications(type, created_at);
```

## Data Migration Strategy

### Phase 1: Core Tables (Week 1)
1. Extend existing tables with new columns
2. Create new core tables (roles, user_roles, refresh_tokens)
3. Migrate existing user data to new structure

### Phase 2: Book Enhancement (Week 2)
1. Add book categories and tags
2. Create book reviews and ratings
3. Migrate existing book data

### Phase 3: Advanced Features (Week 3-4)
1. Implement reservations system
2. Add notifications and analytics
3. Create favorites and wishlist
4. Implement reading history

## Performance Considerations

### Caching Strategy
- **Redis Cache**: User sessions, popular books, categories
- **Application Cache**: Book details, user profiles
- **Database Cache**: Query results for frequently accessed data

### Partitioning Strategy
- **borrow_records**: Partition by year for large datasets
- **analytics_events**: Partition by month for automated cleanup

### Backup Strategy
- **Daily**: Full backup with point-in-time recovery
- **Hourly**: Transaction log backup
- **Real-time**: Replication to standby server

## Security Considerations

### Data Encryption
- **Password**: BCrypt with salt
- **PII**: AES-256 encryption for sensitive data
- **JWT**: RS256 signing with key rotation

### Audit Trail
- All CRUD operations logged
- User activity tracking
- Admin action auditing

This schema supports all functional requirements while maintaining performance, security, and scalability for enterprise-level library management system.
