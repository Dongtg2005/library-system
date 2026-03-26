-- ============================================================================
-- Books Database Initialization Script
-- Service: Book Service (Port 8083)
-- Database: books_db
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- 1. PUBLISHERS TABLE
-- ============================================================================
CREATE TABLE publishers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    publisher_name VARCHAR(255) NOT NULL UNIQUE,
    address TEXT,
    country VARCHAR(100),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    website_url VARCHAR(500),
    established_year INT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_publishers_name ON publishers(publisher_name);

-- ============================================================================
-- 2. AUTHORS TABLE
-- ============================================================================
CREATE TABLE authors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_name VARCHAR(255) NOT NULL,
    birth_date DATE,
    nationality VARCHAR(100),
    biography TEXT,
    author_image_url VARCHAR(500),
    website_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT authors_status_check CHECK(status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE INDEX idx_authors_name ON authors(author_name);

-- ============================================================================
-- 3. CATEGORIES TABLE (Hierarchical)
-- ============================================================================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_name VARCHAR(100) NOT NULL UNIQUE,
    category_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    parent_category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    icon_url VARCHAR(500),
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_categories_name ON categories(category_name);
CREATE INDEX idx_categories_code ON categories(category_code);
CREATE INDEX idx_categories_parent ON categories(parent_category_id);

-- Insert default categories
INSERT INTO categories (category_name, category_code, description, display_order) VALUES
('Fiction', 'FICTION', 'Fictional works', 1),
('Non-Fiction', 'NON_FICTION', 'Non-fictional works', 2),
('Science', 'SCIENCE', 'Science books', 3),
('History', 'HISTORY', 'Historical books', 4),
('Biography', 'BIOGRAPHY', 'Biographical works', 5),
('Children', 'CHILDREN', 'Books for children', 6),
('Young Adult', 'YOUNG_ADULT', 'Young adult literature', 7),
('Mystery', 'MYSTERY', 'Mystery novels', 8),
('Romance', 'ROMANCE', 'Romance fiction', 9),
('Technology', 'TECHNOLOGY', 'Technology books', 10);

-- ============================================================================
-- 4. BOOKS TABLE (Core)
-- ============================================================================
CREATE TABLE books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    isbn VARCHAR(20) UNIQUE,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    publication_year INT,
    publisher_id UUID REFERENCES publishers(id) ON DELETE SET NULL,
    language VARCHAR(50) DEFAULT 'en',
    total_pages INT,
    edition VARCHAR(100),
    book_format VARCHAR(50),
    total_quantity INT NOT NULL DEFAULT 1,
    available_quantity INT NOT NULL DEFAULT 1,
    reserved_quantity INT DEFAULT 0,
    borrowed_quantity INT DEFAULT 0,
    damaged_quantity INT DEFAULT 0,
    lost_quantity INT DEFAULT 0,
    library_rating DECIMAL(3, 2) DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    borrow_count BIGINT DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    book_image_url VARCHAR(500),
    book_price DECIMAL(10, 2),
    replacement_cost DECIMAL(10, 2),
    acquisition_date DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT books_format_check CHECK(book_format IN ('HARDCOVER', 'PAPERBACK', 'EBOOK', 'AUDIOBOOK')),
    CONSTRAINT books_status_check CHECK(status IN ('ACTIVE', 'ARCHIVED', 'OUT_OF_STOCK')),
    CONSTRAINT books_quantity_check CHECK(total_quantity > 0),
    CONSTRAINT books_available_check CHECK(available_quantity >= 0)
);

CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_status ON books(status);
CREATE INDEX idx_books_available ON books(available_quantity) 
    WHERE available_quantity > 0 AND status = 'ACTIVE';
CREATE INDEX idx_books_created_at ON books(created_at DESC);
CREATE INDEX idx_books_title_available ON books(title, available_quantity) 
    WHERE status = 'ACTIVE';

-- ============================================================================
-- 5. BOOKS_AUTHORS TABLE (Many-to-Many)
-- ============================================================================
CREATE TABLE books_authors (
    book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES authors(id) ON DELETE CASCADE,
    author_order INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (book_id, author_id),
    CONSTRAINT books_authors_order_check CHECK(author_order > 0)
);

CREATE INDEX idx_books_authors_book ON books_authors(book_id);
CREATE INDEX idx_books_authors_author ON books_authors(author_id);

-- ============================================================================
-- 6. BOOKS_CATEGORIES TABLE (Many-to-Many)
-- ============================================================================
CREATE TABLE books_categories (
    book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (book_id, category_id)
);

CREATE INDEX idx_books_categories_book ON books_categories(book_id);
CREATE INDEX idx_books_categories_category ON books_categories(category_id);

-- ============================================================================
-- 7. BOOK_EDITIONS TABLE
-- ============================================================================
CREATE TABLE book_editions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    edition_number INT NOT NULL DEFAULT 1,
    edition_name VARCHAR(255),
    isbn VARCHAR(20),
    publication_date DATE,
    publisher_id UUID REFERENCES publishers(id) ON DELETE SET NULL,
    total_pages INT,
    language VARCHAR(50),
    book_format VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT book_editions_unique UNIQUE(book_id, edition_number)
);

CREATE INDEX idx_book_editions_book ON book_editions(book_id);
CREATE INDEX idx_book_editions_isbn ON book_editions(isbn);

-- ============================================================================
-- 8. BOOK_REVIEWS TABLE
-- ============================================================================
CREATE TABLE book_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    member_id UUID NOT NULL,
    rating INT NOT NULL CHECK(rating >= 1 AND rating <= 5),
    review_title VARCHAR(255),
    review_content TEXT,
    helpful_count INT DEFAULT 0,
    unhelpful_count INT DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PUBLISHED',
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT book_reviews_rating_check CHECK(rating >= 1 AND rating <= 5),
    CONSTRAINT book_reviews_status_check CHECK(status IN ('PUBLISHED', 'PENDING', 'REJECTED'))
);

CREATE INDEX idx_book_reviews_book ON book_reviews(book_id);
CREATE INDEX idx_book_reviews_member ON book_reviews(member_id);
CREATE INDEX idx_book_reviews_created ON book_reviews(created_at DESC);
CREATE INDEX idx_book_reviews_rating ON book_reviews(rating);
