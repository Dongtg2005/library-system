CREATE SCHEMA IF NOT EXISTS library;
SET search_path TO library;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'USER' NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    description TEXT,
    quantity INT DEFAULT 0,
    available_quantity INT DEFAULT 0,
    category VARCHAR(100),
    publication_year INT,
    publisher VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_books_title ON books(title);

CREATE TABLE borrows (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    book_id BIGINT NOT NULL REFERENCES books(id),
    borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_borrows_user_id ON borrows(user_id);
CREATE INDEX idx_borrows_book_id ON borrows(book_id);
CREATE INDEX idx_borrows_status ON borrows(status);

INSERT INTO users (email, password, full_name, role) 
VALUES ('admin@library.com', '$2a$10$N9qo8ucowan11vers.oRYeEwqlKxlNlZOKhzbyRKDe2Yay4P/3TYu6', 'Admin User', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

INSERT INTO books (title, author, isbn, category) 
VALUES 
    ('The Clean Code', 'Robert C. Martin', '978-0132350884', 'Programming'),
    ('Design Patterns', 'Gang of Four', '978-0201633610', 'Software Engineering'),
    ('The Pragmatic Programmer', 'David Thomas', '978-9780135957059', 'Programming')
ON CONFLICT (isbn) DO NOTHING;
