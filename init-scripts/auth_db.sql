-- ============================================================================
-- Auth Database Initialization Script
-- Service: Auth Service (Port 8081)
-- Database: auth_db
-- ============================================================================

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ============================================================================
-- 1. USERS TABLE
-- ============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    last_login_at TIMESTAMP WITH TIME ZONE,
    password_changed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT users_role_valid CHECK(role IN ('ADMIN', 'LIBRARIAN', 'USER')),
    CONSTRAINT users_status_valid CHECK(status IN ('ACTIVE', 'SUSPENDED', 'BANNED'))
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at DESC);
CREATE INDEX idx_users_active ON users(email) WHERE status = 'ACTIVE' AND deleted_at IS NULL;

-- ============================================================================
-- 2. ROLES TABLE
-- ============================================================================
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_system_role BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_roles_name ON roles(role_name);

-- Insert default roles
INSERT INTO roles (role_name, description, is_system_role) VALUES
('ADMIN', 'System Administrator', TRUE),
('LIBRARIAN', 'Library Staff', TRUE),
('USER', 'Regular User', TRUE);

-- ============================================================================
-- 3. PERMISSIONS TABLE
-- ============================================================================
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT permissions_category_check CHECK(category IN ('BOOK', 'USER', 'BORROW', 'SYSTEM'))
);

CREATE INDEX idx_permissions_code ON permissions(permission_code);
CREATE INDEX idx_permissions_category ON permissions(category);

-- Insert default permissions
INSERT INTO permissions (permission_code, description, category) VALUES
('BOOK_CREATE', 'Create new books', 'BOOK'),
('BOOK_READ', 'Read book information', 'BOOK'),
('BOOK_UPDATE', 'Update book information', 'BOOK'),
('BOOK_DELETE', 'Delete books', 'BOOK'),
('USER_READ', 'Read user information', 'USER'),
('USER_UPDATE', 'Update user information', 'USER'),
('USER_DELETE', 'Delete users', 'USER'),
('BORROW_CREATE', 'Create borrow records', 'BORROW'),
('BORROW_MANAGE', 'Manage borrow records', 'BORROW'),
('SYSTEM_ADMIN', 'Full system access', 'SYSTEM');

-- ============================================================================
-- 4. ROLE_PERMISSIONS TABLE (Junction Table)
-- ============================================================================
CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions(permission_id);

-- Assign permissions to roles
INSERT INTO role_permissions (role_id, permission_id) VALUES
(
    (SELECT id FROM roles WHERE role_name = 'ADMIN'),
    (SELECT id FROM permissions WHERE permission_code = 'SYSTEM_ADMIN')
),
(
    (SELECT id FROM roles WHERE role_name = 'LIBRARIAN'),
    (SELECT id FROM permissions WHERE permission_code = 'BOOK_CREATE')
),
(
    (SELECT id FROM roles WHERE role_name = 'LIBRARIAN'),
    (SELECT id FROM permissions WHERE permission_code = 'BOOK_READ')
),
(
    (SELECT id FROM roles WHERE role_name = 'LIBRARIAN'),
    (SELECT id FROM permissions WHERE permission_code = 'BOOK_UPDATE')
),
(
    (SELECT id FROM roles WHERE role_name = 'LIBRARIAN'),
    (SELECT id FROM permissions WHERE permission_code = 'BORROW_MANAGE')
),
(
    (SELECT id FROM roles WHERE role_name = 'USER'),
    (SELECT id FROM permissions WHERE permission_code = 'BOOK_READ')
),
(
    (SELECT id FROM roles WHERE role_name = 'USER'),
    (SELECT id FROM permissions WHERE permission_code = 'BORROW_CREATE')
);

-- ============================================================================
-- 5. REFRESH_TOKENS TABLE
-- ============================================================================
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    device_info VARCHAR(255),
    ip_address VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- ============================================================================
-- 6. AUDIT_LOGS TABLE
-- ============================================================================
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    ip_address VARCHAR(50),
    user_agent TEXT,
    status VARCHAR(50) NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT audit_logs_action_valid CHECK(action IN (
        'LOGIN', 'LOGOUT', 'REGISTER', 'PASSWORD_CHANGE', '2FA_ENABLED',
        '2FA_DISABLED', 'SESSION_EXPIRED', 'INVALID_TOKEN', 'PERMISSION_DENIED'
    )),
    CONSTRAINT audit_logs_status_valid CHECK(status IN ('SUCCESS', 'FAILED'))
);

CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

-- ============================================================================
-- SEED DATA: DEFAULT ACCOUNTS (Password: 123456)
-- ============================================================================
INSERT INTO users (id, email, password, full_name, role, status) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin@lms.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00GdRIdum5flge', 'LMS Administrator', 'ADMIN', 'ACTIVE'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'librarian@lms.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00GdRIdum5flge', 'LMS Librarian', 'LIBRARIAN', 'ACTIVE'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'student@lms.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00GdRIdum5flge', 'LMS Student', 'USER', 'ACTIVE'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'user@lms.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00GdRIdum5flge', 'LMS Standard User', 'USER', 'ACTIVE');

