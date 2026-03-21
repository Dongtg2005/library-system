-- Create databases for each microservice
CREATE DATABASE auth_db;
CREATE DATABASE user_db;
CREATE DATABASE book_db;
CREATE DATABASE borrow_db;

-- Grant privileges to postgres user
GRANT ALL PRIVILEGES ON DATABASE auth_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE user_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE book_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE borrow_db TO postgres;
