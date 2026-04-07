-- Library Management System - Sample Data
-- Version:1.1
-- Description: Sample data for testing and demonstration

-- ========================================
-- SAMPLE USERS
-- ========================================

-- Admin user
INSERT INTO users (email, password_hash, full_name, status) VALUES
('admin@library.com', '$2a$10$Vb5a7x8L2v8x8x8x8x8', 'System Administrator', 'ACTIVE');

-- Librarian users
INSERT INTO users (email, password_hash, full_name, phone, status) VALUES
('librarian1@library.com', '$2a$10$Vb5a7x8L2v8x8x8x8x8', 'Nguyễn Văn A', '0901234567', 'ACTIVE'),
('librarian2@library.com', '$2a$10$Vb5a7x8L2v8x8x8x8x8', 'Trần Thị B', '0902345678', 'ACTIVE');

-- Regular users
INSERT INTO users (email, password_hash, full_name, phone, status) VALUES
('user1@library.com', '$2a$10$Vb5a7x8L2v8x8x8x8x8', 'Lê Văn C', '0903456789', 'ACTIVE'),
('user2@library.com', '$2a$10$Vb5a7x8L2v8x8x8x8', 'Phạm Thị D', '0904567890', 'ACTIVE'),
('user3@library.com', '$2a$10$Vb5a7x8L2v8x8x8x8', 'Hoàng Văn E', '0905678901', 'ACTIVE'),
('user4@library.com', '$2a$10$Vb5a7x8L2v8x8x8x8', 'Nguyễn Thị F', '0906789012', 'ACTIVE'),
('user5@library.com', '$2a$10$Vb5a7x8L2v8x8x8x8', 'Trần Văn G', '0907890123', 'ACTIVE');

-- Assign roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 4), -- Admin
(2, 3), -- Librarian 1
(3, 3), -- Librarian 2
(4, 2), -- User 1
(5, 2), -- User 2
(6, 2), -- User 3
(7, 2), -- User 4
(8, 2), -- User 5
(9, 2); -- User 6

-- ========================================
-- SAMPLE BOOKS
-- ========================================

INSERT INTO books (isbn, title, author, publisher, publication_date, language, pages, format, total_quantity, available_quantity, average_rating, rating_count, view_count) VALUES
('978-3-16-148410-0', 'Dế Mèn Là Vợ Tôi', 'Nguyễn Nhật Ánh', 'NXB Văn Học', '2020-05-15', 'vi', 320, 'PHYSICAL', 10, 10, 4.5, 12, 156),
('978-3-16-148411-0', 'Số Đỏ', 'Bùi Anh Tấn', 'NXB Văn Học', '2019-08-20', 'vi', 280, 'PHYSICAL', 8, 8, 4.2, 8, 234),
('978-3-16-148412-0', 'Chí Phèo', 'Nam Cao', 'NXB Văn Học', '2018-03-10', 'vi', 250, 'PHYSICAL', 15, 15, 4.8, 25, 189),
('978-3-16-148413-0', 'Lão Hạc', 'Nam Cao', 'NXB Văn Học', '2017-11-25', 'vi', 300, 'PHYSICAL', 12, 12, 4.6, 18, 167),
('978-3-16-148414-0', 'Vợ Nhặt', 'Kim Dung', 'NXB Văn Học', '2021-02-14', 'vi', 180, 'PHYSICAL', 20, 20, 4.3, 15, 145),
('978-3-16-148415-0', 'Nhà Giả Kim', 'Nguyễn Tuân', 'NXB Văn Học', '2016-06-30', 'vi', 400, 'PHYSICAL', 8, 8, 4.7, 22, 198),
('978-3-16-148416-0', 'Tắt Đèn', 'Ngô Tất Văn', 'NXB Văn Học', '2015-09-20', 'vi', 220, 'PHYSICAL', 25, 25, 4.4, 31, 176),
('978-3-16-148417-0', 'Con Chó', 'Vũ Trọng Phụng', 'NXB Văn Học', '2014-04-25', 'vi', 350, 'PHYSICAL', 18, 18, 4.9, 28, 234),
('978-3-16-148418-0', 'Cho Tôi Xem Tay', 'Trần Duy Tân', 'NXB Trẻ', '2022-07-15', 'vi', 150, 'PHYSICAL', 30, 30, 4.1, 19, 156),
('978-602-02103-0', 'Harry Potter and the Philosopher''s Stone', 'J.K. Rowling', 'Bloomsbury', '1997-06-26', 'en', 223, 'PHYSICAL', 5, 5, 4.8, 89, 267),
('978-0-7432-8421-9', 'The Hobbit', 'J.R.R. Tolkien', 'George Allen & Unwin', '1937-09-21', 'en', 310, 'PHYSICAL', 12, 12, 4.6, 45, 189),
('978-0-06-181965-4', 'Dune', 'Frank Herbert', 'Chilton Books', '1965-08-01', 'en', 412, 'PHYSICAL', 8, 8, 4.3, 67, 234);

-- Add book categories
INSERT INTO book_categories (book_id, category_id) VALUES
('550e8400-e29b-41d4-a716-446655440000', 1), -- Fiction
('550e8400-e29b-41d4-a716-446655440000', 2), -- Science Fiction
('550e8400-e29b-41d4-a716-446655440000', 3), -- Romance
('550e8400-e29b-41d4-a716-446655440000', 4), -- Mystery
('550e8400-e29b-41d4-a716-446655440000', 5), -- Biography
('550e8400-e29b-41d4-a716-446655440000', 7), -- Technology
('550e8400-e29b-41d4-a716-446655440000', 8), -- Business
('550e8400-e29b-41d4-a716-446655440000', 9), -- Children

-- Add book tags
INSERT INTO book_tags (book_id, tag_id) VALUES
('550e8400-e29b-41d4-a716-446655440000', 1), -- Bestseller
('550e8400-e29b-41d4-a716-446655440000', 2), -- New Release
('550e8400-e29b-41d4-a716-446655440000', 3), -- Award Winner
('550e8400-e29b-41d4-a716-446655440000', 4), -- Classic
('550e8400-e29b-41d4-a716-446655440000', 5), -- Recommended
('550e8400-e29b-41d4-a716-446655440000', 6), -- Trending
('550e8400-e29b-41d4-a716-446655440000', 7); -- Educational

-- ========================================
-- SAMPLE BORROW POLICIES
-- ========================================

INSERT INTO borrow_policies (member_type, max_books_allowed, loan_period_days, max_extensions, fine_per_day, max_fine, grace_period_days) VALUES
('GUEST', 2, 7, 0, 2000.00, 10000.00, 0),
('USER', 5, 14, 3, 1000.00, 50000.00, 1),
('LIBRARIAN', 10, 30, 5, 500.00, 100000.00, 2),
('ADMIN', 20, 60, 10, 0.00, 0.00, 0);

-- ========================================
-- SAMPLE USER PROFILES
-- ========================================

INSERT INTO user_profiles (auth_user_id, bio, favorite_genres, reading_preferences, total_books_read, total_pages_read, membership_level, points) VALUES
(4, 'Thủ thư với 10 năm kinh nghiệm, yêu thích văn học kinh điển', '{"fiction": true, "biography": true, "history": true}', '{"font_size": "medium", "theme": "light"}', 45, 12345, 'SILVER', 250),
(5, 'Sinh viên năm 4, thích đọc sách kỹ năng và self-help', '{"technology": true, "business": true, "self_help": true}', '{"font_size": "small", "theme": "dark"}', 23, 5678, 'BRONZE', 120),
(6, 'Người đi làm, thích đọc sách kinh doanh và đầu tư', '{"business": true, "finance": true, "biography": true}', '{"font_size": "large", "theme": "light"}', 67, 23456, 'GOLD', 580),
(7, 'Học sinh cấp 3, đam mê văn học và nghệ thuật', '{"fiction": true, "art": true, "philosophy": true}', '{"font_size": "medium", "theme": "sepia"}', 34, 18900, 'SILVER', 340),
(8, 'Gia đình, thích đọc sách cho trẻ em', '{"children": true, "education": true, "family": true}', '{"font_size": "large", "theme": "colorful"}', 89, 45678, 'PLATINUM', 890),
(9, 'Người về hưu, thích đọc lịch sử và tản văn', '{"history": true, "biography": true, "philosophy": true}', '{"font_size": "large", "theme": "sepia"}', 156, 78900, 'GOLD', 1200);

-- ========================================
-- SAMPLE RESERVATIONS
-- ========================================

INSERT INTO reservations (user_id, book_id, expires_at, priority, status) VALUES
(4, '550e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP + INTERVAL '24 hours', 1, 'ACTIVE'), -- Librarian reserves popular book
(5, '550e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP + INTERVAL '24 hours', 2, 'ACTIVE'), -- User reserves new book
(6, '550e8400-e29b-41d4-a716-446655440003', CURRENT_TIMESTAMP + INTERVAL '24 hours', 1, 'ACTIVE'), -- User reserves classic book
(7, '550e8400-e29b-41d4-a716-446655440004', CURRENT_TIMESTAMP + INTERVAL '24 hours', 3, 'ACTIVE'); -- User reserves trending book

-- ========================================
-- SAMPLE BORROW RECORDS
-- ========================================

INSERT INTO borrow_records (member_id, book_id, borrow_date, due_date, borrow_status, condition_on_borrow, renewal_count, max_renewals) VALUES
(5, '550e8400-e29b-41d4-a716-446655440002', CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '4 days', 'RETURNED', 'GOOD', 0, 3),
(6, '550e8400-e29b-41d4-a716-446655440003', CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '9 days', 'ACTIVE', 'GOOD', 0, 3),
(7, '550e8400-e29b-41d4-a716-446655440004', CURRENT_DATE - INTERVAL '15 days', CURRENT_DATE - INTERVAL '1 day', 'OVERDUE', 'GOOD', 1, 3),
(8, '550e8400-e29b-41d4-a716-446655440005', CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE - INTERVAL '6 days', 'OVERDUE', 'WORN', 2, 3);

-- Update book available quantities
UPDATE books SET available_quantity = total_quantity - 1, borrowed_quantity = 1 WHERE isbn IN ('978-3-16-148411-0', '978-3-16-148412-0', '978-3-16-148413-0', '978-3-16-148414-0');

-- ========================================
-- SAMPLE REVIEWS
-- ========================================

INSERT INTO book_reviews (user_id, book_id, rating, title, content, status) VALUES
(4, '550e8400-e29b-41d4-a716-446655440001', 5, 'Tuyệt tác phẩm', 'Một kiệt tác văn học kinh điển của Việt Nam. Lời văn sâu sắc, cách hành nhân vật sống động.', 'PUBLISHED'),
(5, '550e8400-e29b-41d4-a716-446655440002', 4, 'Cảm động', 'Câu chuyện cảm động về tình bạn, đáng để đọc.', 'PUBLISHED'),
(6, '550e8400-e29b-41d4-a716-446655440003', 5, 'Kinh điển', 'Giá trị văn học và lịch sử cao.', 'PUBLISHED'),
(7, '550e8400-e29b-41d4-a716-446655440004', 3, 'Hơi quá dài', 'Cốt truyện hay nhưng diễn giải hơi dài.', 'PUBLISHED'),
(8, '550e8400-e29b-41d4-a716-446655440005', 4, 'Không thể bỏ xuống', 'Đọc xong không thể ngừng, phải đọc hết!', 'PUBLISHED');

-- ========================================
-- SAMPLE FAVORITES & WISHLIST
-- ========================================

INSERT INTO user_favorites (user_id, book_id) VALUES
(5, '550e8400-e29b-41d4-a716-446655440001'),
(5, '550e8400-e29b-41d4-a716-446655440003'),
(5, '550e8400-e29b-41d4-a716-446655440006'),
(6, '550e8400-e29b-41d4-a716-446655440002'),
(6, '550e8400-e29b-41d4-a716-446655440007'),
(7, '550e8400-e29b-41d4-a716-446655440003'),
(7, '550e8400-e29b-41d4-a716-446655440008');

INSERT INTO user_wishlist (user_id, book_id, priority) VALUES
(5, '550e8400-e29b-41d4-a716-4466554408', 3), -- Harry Potter - High priority
(5, '550e8400-e29b-41d4-a716-4466554409', 2), -- The Hobbit - Medium priority
(6, '550e8400-e29b-41d4-a716-4466554407', 1), -- Dune - Low priority
(6, '550e8400-e29b-41d4-a716-4466554406', 3); -- New book - High priority

-- ========================================
-- SAMPLE READING HISTORY
-- ========================================

INSERT INTO reading_history (user_id, book_id, started_at, finished_at, pages_read, rating_given, review_given) VALUES
(4, '550e8400-e29b-41d4-a716-446655440001', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '15 days', 320, 5, TRUE),
(5, '550e8400-e29b-41d4-a716-446655440002', CURRENT_DATE - INTERVAL '45 days', CURRENT_DATE - INTERVAL '30 days', 280, 4, TRUE),
(6, '550e8400-e29b-41d4-a716-446655440003', CURRENT_DATE - INTERVAL '20 days', NULL, 150, NULL, FALSE), -- Currently reading
(7, '550e8400-e29b-41d4-a716-446655440004', CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '45 days', 250, 3, TRUE),
(8, '550e8400-e29b-41d4-a716-446655440005', CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE - INTERVAL '60 days', 412, 4, TRUE);

-- ========================================
-- SAMPLE NOTIFICATIONS
-- ========================================

INSERT INTO notifications (user_id, type, title, content, related_resource_type, related_resource_id, scheduled_at) VALUES
(7, 'DUE_SOON', 'Sắp đến hạn trả sách', 'Bạn có 1 sách sẽ đến hạn trả trong 3 ngày tới: "Số Đỏ"', 'BOOK', '550e8400-e29b-41d4-a716-446655440003', CURRENT_DATE + INTERVAL '3 days'),
(8, 'AVAILABLE', 'Sách đã có sẵn', 'Sách "The Hobbit" mà bạn đặt trong wishlist đã có sẵn để mượn!', 'BOOK', '550e8400-e29b-41d4-a716-4466554409', CURRENT_TIMESTAMP),
(5, 'OVERDUE', 'Quá hạn trả sách', 'Bạn đã quá hạn trả sách "Dune" 2 ngày. Phí phạt: 2,000 VNĐ', 'BOOK', '550e8400-e29b-41d4-a716-4466554407', CURRENT_DATE - INTERVAL '2 days'),
(6, 'APPROVED', 'Yêu cầu mượn được duyệt', 'Yêu cầu mượn sách "Harry Potter" đã được phê duyệt. Hãy đến thư viện để nhận sách!', 'BOOK', '550e8400-e29b-41d4-a716-4466554408', CURRENT_TIMESTAMP);

-- ========================================
-- SAMPLE USER ACTIVITIES
-- ========================================

INSERT INTO user_activities (user_id, activity_type, resource_type, resource_id, description) VALUES
(4, 'LOGIN', 'USER', 1, 'Admin login'),
(5, 'BORROW', 'BOOK', '550e8400-e29b-41d4-a716-446655440002', 'Borrowed "Số Đỏ"'),
(6, 'SEARCH', 'BOOK', NULL, 'Searched for "science fiction" books'),
(7, 'VIEW_BOOK', 'BOOK', '550e8400-e29b-41d4-a716-446655440003', 'Viewed book details for "Chí Phèo"'),
(8, 'ADD_FAVORITE', 'BOOK', '550e8400-e29b-41d4-a716-446655440004', 'Added "Lão Hạc" to favorites'),
(5, 'RETURN', 'BOOK', '550e8400-e29b-41d4-a716-446655440002', 'Returned "Số Đỏ"'),
(6, 'REVIEW', 'BOOK', '550e8400-e29b-41d4-a716-446655440003', 'Reviewed "Chí Phèo"'),
(7, 'RESERVE_BOOK', 'BOOK', '550e8400-e29b-41d4-a716-4466554406', 'Reserved "Vợ Nhặt"');
