-- Create base tables for Library Management System
-- This migration runs BEFORE V1__Create_initial_schema.sql
-- to ensure base tables exist before ALTER TABLE statements

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================================
-- 1. CORE TABLES (Extended by V1 migration)
-- ========================================

-- Books base table
CREATE TABLE IF NOT EXISTS books (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    isbn VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    total_quantity INTEGER DEFAULT 1 NOT NULL,
    available_quantity INTEGER DEFAULT 1 NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Borrow records base table
CREATE TABLE IF NOT EXISTS borrow_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    member_id BIGINT NOT NULL,
    book_id UUID NOT NULL,
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
    notes TEXT,
    return_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Borrow policies base table
CREATE TABLE IF NOT EXISTS borrow_policies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    member_type VARCHAR(20) NOT NULL,
    max_books_allowed INTEGER DEFAULT 5,
    loan_period_days INTEGER DEFAULT 14,
    max_extensions INTEGER DEFAULT 2,
    fine_per_day DECIMAL(10,2) DEFAULT 1000.00,
    effective_from DATE,
    effective_to DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User profiles base table
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    member_status VARCHAR(20) DEFAULT 'ACTIVE',
    total_books_borrowed INTEGER DEFAULT 0,
    current_books_borrowed INTEGER DEFAULT 0,
    total_fines DECIMAL(10,2) DEFAULT 0.00,
    outstanding_fines DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Fines base table
CREATE TABLE IF NOT EXISTS fines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    borrow_record_id UUID NOT NULL,
    member_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    fine_type VARCHAR(20) DEFAULT 'OVERDUE',
    status VARCHAR(20) DEFAULT 'PENDING',
    reason TEXT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
