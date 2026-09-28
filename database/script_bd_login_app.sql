CREATE DATABASE IF NOT EXISTS login_app_db;
USE login_app_db;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL, -- 'admin', 'user', etc.
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);