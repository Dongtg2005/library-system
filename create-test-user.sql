-- Register a test user with all required fields
INSERT INTO users (email, password, full_name, role, enabled, auth_user_id, current_books_borrowed, deleted, member_status, outstanding_fines, total_books_borrowed) 
SELECT 1 as id, 'abc123@gmail.com' as email, '$2a$10$SJKM3Mo4/h5ZDrDUWTL9nOQQ.iYf3xtVFLgir1QXB/9sF0o7h3jNm' as password, 'Người Dùng Test' as full_name, 'USER' as role, true as enabled, 1 as auth_user_id, 0 as current_books_borrowed, false as deleted, 'ACTIVE' as member_status, 0 as outstanding_fines, 0 as total_books_borrowed
WHERE NOT EXISTS(SELECT 1 FROM users WHERE email = 'abc123@gmail.com');

SELECT id, email, password, enabled FROM users WHERE email='abc123@gmail.com';
