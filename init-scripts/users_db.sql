-- ============================================================================
-- Users Database Initialization Script
-- Service: User Service (Port 8082)
-- Database: users_db
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- 1. MEMBERS TABLE (Core User Profile)
-- ============================================================================
CREATE TABLE members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_user_id UUID NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(20) CHECK(gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_SAY')),
    member_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    member_type VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    membership_expiry_date DATE,
    total_books_borrowed INT DEFAULT 0,
    current_books_borrowed INT DEFAULT 0,
    total_fines_paid DECIMAL(10, 2) DEFAULT 0,
    outstanding_fines DECIMAL(10, 2) DEFAULT 0,
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_phone_verified BOOLEAN DEFAULT FALSE,
    last_activity_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT members_status_check CHECK(member_status IN ('ACTIVE', 'SUSPENDED', 'EXPIRED')),
    CONSTRAINT members_type_check CHECK(member_type IN ('STANDARD', 'PREMIUM', 'STUDENT'))
);

CREATE INDEX idx_members_auth_user_id ON members(auth_user_id);
CREATE INDEX idx_members_email ON members(email);
CREATE INDEX idx_members_status ON members(member_status);
CREATE INDEX idx_members_created_at ON members(created_at DESC);
CREATE INDEX idx_members_status_created ON members(member_status, created_at DESC);

-- ============================================================================
-- 2. MEMBER_PROFILES TABLE
-- ============================================================================
CREATE TABLE member_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL UNIQUE REFERENCES members(id) ON DELETE CASCADE,
    bio TEXT,
    profile_picture_url VARCHAR(500),
    cover_picture_url VARCHAR(500),
    occupation VARCHAR(255),
    organization VARCHAR(255),
    favorite_genres TEXT,
    reading_level VARCHAR(50),
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relationship VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_member_profiles_member ON member_profiles(member_id);

-- ============================================================================
-- 3. MEMBER_ADDRESSES TABLE
-- ============================================================================
CREATE TABLE member_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    address_type VARCHAR(50) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state_province VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT member_addresses_type_check CHECK(address_type IN ('HOME', 'WORK', 'BILLING'))
);

CREATE INDEX idx_member_addresses_member ON member_addresses(member_id);
CREATE INDEX idx_member_addresses_default ON member_addresses(member_id, is_default);

-- ============================================================================
-- 4. MEMBER_PREFERENCES TABLE
-- ============================================================================
CREATE TABLE member_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL UNIQUE REFERENCES members(id) ON DELETE CASCADE,
    email_notifications_enabled BOOLEAN DEFAULT TRUE,
    sms_notifications_enabled BOOLEAN DEFAULT FALSE,
    push_notifications_enabled BOOLEAN DEFAULT TRUE,
    newsletter_subscribed BOOLEAN DEFAULT TRUE,
    language VARCHAR(10) DEFAULT 'en',
    theme VARCHAR(20) DEFAULT 'LIGHT',
    notification_frequency VARCHAR(50) DEFAULT 'IMMEDIATE',
    receive_due_date_reminders BOOLEAN DEFAULT TRUE,
    receive_fine_notifications BOOLEAN DEFAULT TRUE,
    receive_new_books_notifications BOOLEAN DEFAULT FALSE,
    receive_recommendation_notifications BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_member_preferences_member ON member_preferences(member_id);

-- ============================================================================
-- 5. MEMBER_DEVICES TABLE
-- ============================================================================
CREATE TABLE member_devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    device_name VARCHAR(255),
    device_type VARCHAR(50),
    operating_system VARCHAR(100),
    browser_name VARCHAR(100),
    browser_version VARCHAR(50),
    device_fingerprint VARCHAR(255),
    ip_address VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    last_accessed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_member_devices_member ON member_devices(member_id);
CREATE INDEX idx_member_devices_active ON member_devices(member_id, is_active);

-- ============================================================================
-- 6. MEMBER_LOGIN_HISTORY TABLE
-- ============================================================================
CREATE TABLE member_login_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    device_id UUID REFERENCES member_devices(id) ON DELETE SET NULL,
    login_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logout_at TIMESTAMP WITH TIME ZONE,
    ip_address VARCHAR(50),
    user_agent TEXT,
    is_successful BOOLEAN DEFAULT TRUE,
    failure_reason VARCHAR(255),
    session_duration_seconds BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_member_login_history_member ON member_login_history(member_id);
CREATE INDEX idx_member_login_history_date ON member_login_history(login_at DESC);

-- ============================================================================
-- SEED DATA: MEMBER PROFILES
-- ============================================================================
INSERT INTO members (id, auth_user_id, email, member_status, member_type, is_email_verified) VALUES
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin@lms.com', 'ACTIVE', 'STANDARD', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'librarian@lms.com', 'ACTIVE', 'STANDARD', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'student@lms.com', 'ACTIVE', 'STUDENT', TRUE),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b14', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'user@lms.com', 'ACTIVE', 'STANDARD', TRUE);

