-- Insert test user with bcrypt hashed password: 123456
-- Hash: $2a$10$SJKM3Mo4/h5ZDrDUWTL9nOQQ.iYf3xtVFLgir1QXB/9sF0o7h3jNm
-- Note: This is inserted only once via ON CONFLICT
INSERT INTO users (email, password, full_name, role, enabled, auth_user_id, current_books_borrowed, deleted, member_status, outstanding_fines, total_books_borrowed) 
VALUES ('abc123@gmail.com', '$2a$10$SJKM3Mo4/h5ZDrDUWTL9nOQQ.iYf3xtVFLgir1QXB/9sF0o7h3jNm', 'Người Dùng Test', 'USER', true, 1, 0, false, 'ACTIVE', 0, 0)
ON CONFLICT (email) DO NOTHING;
