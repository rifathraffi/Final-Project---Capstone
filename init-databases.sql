-- Initialize separate databases for database-per-service pattern

-- Create auth database
CREATE DATABASE auth_db;

-- Create product database
CREATE DATABASE product_db;

-- Create order database
CREATE DATABASE order_db;

-- Grant privileges to postgres user
GRANT ALL PRIVILEGES ON DATABASE auth_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE product_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE order_db TO postgres;
