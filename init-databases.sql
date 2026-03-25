-- Create databases for each microservice
CREATE DATABASE auth_db;
CREATE DATABASE users_db;
CREATE DATABASE books_db;
CREATE DATABASE borrows_db;

-- Grant privileges to postgres user
GRANT ALL PRIVILEGES ON DATABASE auth_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE users_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE books_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE borrows_db TO postgres;
