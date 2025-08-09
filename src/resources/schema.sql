-- COMPLETE SCHEMA FOR LOGISTICS AI AGENT
-- File: src/main/resources/schema.sql

-- Products table for logistics system
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    weight DECIMAL(10,2) NOT NULL,
    volume DECIMAL(10,2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    location VARCHAR(50) NOT NULL
);

-- Orders table for logistics system
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(50) NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    order_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    delivery_address TEXT NOT NULL,
    requested_delivery_date TIMESTAMP,
    total_weight DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    total_volume DECIMAL(10,2) NOT NULL DEFAULT 0.0
);

-- DATABASE INDEXES FOR PERFORMANCE
CREATE INDEX IF NOT EXISTS idx_products_location ON products(location);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_products_stock_quantity ON products(stock_quantity);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_client_id ON orders(client_id);
CREATE INDEX IF NOT EXISTS idx_orders_order_date ON orders(order_date);
CREATE INDEX IF NOT EXISTS idx_orders_requested_delivery_date ON orders(requested_delivery_date);