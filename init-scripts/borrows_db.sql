-- ============================================================================
-- Borrows Database Initialization Script
-- Service: Borrow Service (Port 8084)
-- Database: borrows_db
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- 1. BORROW_POLICIES TABLE (Configuration)
-- ============================================================================
CREATE TABLE borrow_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    member_type VARCHAR(50) NOT NULL,
    max_books_allowed INT NOT NULL DEFAULT 5,
    loan_period_days INT NOT NULL DEFAULT 14,
    max_extensions INT NOT NULL DEFAULT 3,
    extension_days INT NOT NULL DEFAULT 7,
    fine_per_day DECIMAL(8, 2) NOT NULL DEFAULT 1.00,
    max_fine_per_book DECIMAL(10, 2),
    book_format_allowed VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT borrow_policies_name_unique UNIQUE(policy_name),
    CONSTRAINT borrow_policies_member_type_check CHECK(member_type IN (
        'STANDARD', 'PREMIUM', 'STUDENT'
    ))
);

CREATE INDEX idx_borrow_policies_member_type ON borrow_policies(member_type);

-- Insert default policies
INSERT INTO borrow_policies (
    policy_name, description, member_type, max_books_allowed, 
    loan_period_days, max_extensions, extension_days, fine_per_day, max_fine_per_book
) VALUES
('STANDARD_POLICY', 'Standard member borrowing policy', 'STANDARD', 5, 14, 3, 7, 1.00, 50.00),
('PREMIUM_POLICY', 'Premium member borrowing policy', 'PREMIUM', 10, 21, 4, 14, 0.50, 50.00),
('STUDENT_POLICY', 'Student member borrowing policy', 'STUDENT', 7, 14, 2, 7, 0.50, 30.00);

-- ============================================================================
-- 2. BORROW_RECORDS TABLE
-- ============================================================================
CREATE TABLE borrow_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL,
    book_id UUID NOT NULL,
    book_copy_id UUID,
    borrow_date DATE NOT NULL DEFAULT CURRENT_DATE,
    borrow_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date DATE NOT NULL,
    return_date DATE,
    return_time TIMESTAMP WITH TIME ZONE,
    extension_count INT DEFAULT 0,
    max_extensions INT DEFAULT 3,
    last_extension_date DATE,
    borrow_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    condition_on_borrow VARCHAR(50),
    condition_on_return VARCHAR(50),
    notes TEXT,
    return_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT borrow_records_status_check CHECK(borrow_status IN (
        'ACTIVE', 'RETURNED', 'OVERDUE', 'LOST', 'PENDING_APPROVAL'
    )),
    CONSTRAINT borrow_records_dates_check CHECK(due_date >= borrow_date),
    CONSTRAINT borrow_records_condition_check CHECK(condition_on_borrow IN (
        'EXCELLENT', 'GOOD', 'FAIR', 'POOR', NULL
    ))
);

CREATE INDEX idx_borrow_records_member ON borrow_records(member_id);
CREATE INDEX idx_borrow_records_book ON borrow_records(book_id);
CREATE INDEX idx_borrow_records_status ON borrow_records(borrow_status);
CREATE INDEX idx_borrow_records_due_date ON borrow_records(due_date) 
    WHERE borrow_status = 'ACTIVE';
CREATE INDEX idx_borrow_records_member_status ON borrow_records(member_id, borrow_status);
CREATE INDEX idx_borrow_records_created ON borrow_records(created_at DESC);
CREATE INDEX idx_borrow_records_member_active ON borrow_records(member_id, borrow_status) 
    WHERE borrow_status IN ('ACTIVE', 'OVERDUE');

-- ============================================================================
-- 3. BORROW_EXTENSIONS TABLE
-- ============================================================================
CREATE TABLE borrow_extensions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    borrow_record_id UUID NOT NULL REFERENCES borrow_records(id) ON DELETE CASCADE,
    extension_number INT NOT NULL DEFAULT 1,
    requested_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_date TIMESTAMP WITH TIME ZONE,
    approved_by_id UUID,
    old_due_date DATE NOT NULL,
    new_due_date DATE NOT NULL,
    extension_days INT NOT NULL,
    reason VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(255),
    is_auto_approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT borrow_extensions_status_check CHECK(status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT borrow_extensions_dates_check CHECK(new_due_date > old_due_date)
);

CREATE INDEX idx_borrow_extensions_record ON borrow_extensions(borrow_record_id);
CREATE INDEX idx_borrow_extensions_status ON borrow_extensions(status);
CREATE INDEX idx_borrow_extensions_requested ON borrow_extensions(requested_date DESC);

-- ============================================================================
-- 4. FINES TABLE
-- ============================================================================
CREATE TABLE fines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    borrow_record_id UUID NOT NULL REFERENCES borrow_records(id) ON DELETE CASCADE,
    member_id UUID NOT NULL,
    fine_type VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    daily_rate DECIMAL(8, 2),
    max_fine_amount DECIMAL(10, 2),
    days_overdue INT,
    reason TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    payment_date TIMESTAMP WITH TIME ZONE,
    waived_by_id UUID,
    waived_date TIMESTAMP WITH TIME ZONE,
    waiver_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fines_type_check CHECK(fine_type IN ('OVERDUE', 'DAMAGE', 'LOSS', 'OTHER')),
    CONSTRAINT fines_status_check CHECK(status IN ('PENDING', 'PAID', 'WAIVED', 'WRITTEN_OFF')),
    CONSTRAINT fines_amount_check CHECK(amount > 0)
);

CREATE INDEX idx_fines_member ON fines(member_id);
CREATE INDEX idx_fines_borrow_record ON fines(borrow_record_id);
CREATE INDEX idx_fines_status ON fines(status);
CREATE INDEX idx_fines_unpaid ON fines(member_id, status) 
    WHERE status = 'PENDING';
CREATE INDEX idx_fines_created ON fines(created_at DESC);
CREATE INDEX idx_fines_member_status ON fines(member_id, status) 
    WHERE status IN ('PENDING', 'PAID');

-- ============================================================================
-- 5. RESERVATIONS TABLE
-- ============================================================================
CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL,
    book_id UUID NOT NULL,
    reservation_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reservation_expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    queue_position INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    notification_status VARCHAR(50) DEFAULT 'NOT_NOTIFIED',
    notified_at TIMESTAMP WITH TIME ZONE,
    fulfilled_date TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT reservations_status_check CHECK(status IN (
        'PENDING', 'READY', 'FULFILLED', 'CANCELLED', 'EXPIRED'
    )),
    CONSTRAINT reservations_notification_check CHECK(notification_status IN (
        'NOT_NOTIFIED', 'NOTIFIED', 'ACKNOWLEDGED'
    ))
);

CREATE INDEX idx_reservations_member ON reservations(member_id);
CREATE INDEX idx_reservations_book ON reservations(book_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_queue ON reservations(book_id, queue_position) 
    WHERE status = 'PENDING';
CREATE INDEX idx_reservations_expiry ON reservations(reservation_expiry_date) 
    WHERE status = 'PENDING';

-- ============================================================================
-- 6. BORROW_EVENTS TABLE (For Saga Pattern & Event Sourcing)
-- ============================================================================
CREATE TABLE borrow_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    borrow_record_id UUID REFERENCES borrow_records(id) ON DELETE SET NULL,
    event_type VARCHAR(100) NOT NULL,
    event_status VARCHAR(50) NOT NULL,
    member_id UUID,
    book_id UUID,
    payload JSONB,
    error_message TEXT,
    saga_id VARCHAR(100),
    triggered_by_id UUID,
    triggered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT borrow_events_type_check CHECK(event_type IN (
        'BORROWED', 'RETURNED', 'EXTENDED', 'OVERDUE', 'FINED', 'DAMAGE_REPORTED',
        'LOST_REPORTED', 'RESERVATION_MADE', 'RESERVATION_FULFILLED', 'FINE_PAID',
        'FINE_WAIVED', 'MEMBER_SANCTIONED'
    )),
    CONSTRAINT borrow_events_status_check CHECK(event_status IN (
        'INITIATED', 'SUCCESS', 'FAILED', 'COMPENSATED', 'PENDING'
    ))
);

CREATE INDEX idx_borrow_events_borrow_record ON borrow_events(borrow_record_id);
CREATE INDEX idx_borrow_events_member ON borrow_events(member_id);
CREATE INDEX idx_borrow_events_type ON borrow_events(event_type);
CREATE INDEX idx_borrow_events_saga ON borrow_events(saga_id);
CREATE INDEX idx_borrow_events_timestamp ON borrow_events(triggered_at DESC);
CREATE INDEX idx_borrow_events_status ON borrow_events(event_status);
