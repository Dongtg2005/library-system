-- Tạo database cho Auth Service & User Service (có thể dùng chung 1 DB nhưng khác schema, hoặc tách hẳn DB)
CREATE DATABASE lms_user_db;

-- Tạo database cho Book Service
CREATE DATABASE lms_book_db;

-- Tạo database cho Borrow Service
CREATE DATABASE lms_borrow_db;

-- Cấp quyền cho user mặc định (giả sử user là 'postgres')
GRANT ALL PRIVILEGES ON DATABASE lms_user_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE lms_book_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE lms_borrow_db TO postgres;