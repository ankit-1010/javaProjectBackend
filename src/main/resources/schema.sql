-- MoneyMate MySQL Database Schema (Tables are automatically managed by Hibernate ddl-auto=update)

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'Active',
    phone VARCHAR(30),
    avatar VARCHAR(255),
    joined_on DATE NOT NULL,
    total_balance DECIMAL(12, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    icon VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'EXPENSE',
    color VARCHAR(20) DEFAULT '#3B82F6',
    transaction_count INT DEFAULT 0,
    budget_limit DECIMAL(12, 2) DEFAULT 0.00
);

-- 3. Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(100),
    description VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Completed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Budgets Table
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(100) NOT NULL,
    spent_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    allocated_amount DECIMAL(12, 2) NOT NULL,
    month_year VARCHAR(20) NOT NULL DEFAULT 'September 2026',
    icon VARCHAR(50),
    color VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Goals Table
CREATE TABLE IF NOT EXISTS goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    saved_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    target_amount DECIMAL(12, 2) NOT NULL,
    target_date VARCHAR(50) NOT NULL,
    icon VARCHAR(50) DEFAULT 'Target',
    color VARCHAR(20) DEFAULT '#3B82F6',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
