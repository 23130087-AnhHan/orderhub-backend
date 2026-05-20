# 02. Database Design - OrderHub Backend System

## 1. Database Design Goals

OrderHub Backend System uses **MySQL** as the main relational database to store core business data such as users, roles, products, carts, orders, order items and payments.

The database is designed to achieve the following goals:

- Store relational business data in a clear and consistent structure.
- Support the main e-commerce order flow.
- Ensure data integrity using primary keys, foreign keys and unique constraints.
- Support transaction handling when creating orders and updating inventory.
- Support query optimization through proper indexing.
- Make the system easy to extend with Redis, RabbitMQ, MongoDB and microservices in later phases.
- Match backend intern requirements such as database schema design, optimized queries, data integrity, indexing and transaction handling.

---

## 2. Why MySQL Is Used

MySQL is used because the core data of this system has strong relationships and requires consistency.

Examples:

- One user can create many orders.
- One order contains many order items.
- One product can have many product variants.
- One cart contains many cart items.
- One order has one payment record.

Data such as users, products, orders and payments should be stored in a relational database because they require reliable relationships, constraints and transactions.

In later phases, the system can also use:

- **Redis** for caching product data.
- **RabbitMQ** for asynchronous event-driven communication.
- **MongoDB** for notifications or audit logs.

---

## 3. Main Tables

The initial database schema contains the following tables:

| No. | Table Name | Purpose |
|---|---|---|
| 1 | users | Store user account information |
| 2 | roles | Store system roles |
| 3 | user_roles | Map users and roles |
| 4 | categories | Store product categories |
| 5 | products | Store product information |
| 6 | product_variants | Store product variants such as SKU, size, color and stock |
| 7 | carts | Store user shopping carts |
| 8 | cart_items | Store items inside carts |
| 9 | orders | Store customer orders |
| 10 | order_items | Store order item details |
| 11 | payments | Store payment information |

---

## 4. Relationship Overview

### 4.1. User and Role

One user can have multiple roles. One role can also belong to multiple users.

Relationship:

```text
users n ---- n roles
users 1 ---- n user_roles
roles 1 ---- n user_roles
```

Example:

- A normal customer has `ROLE_USER`.
- An administrator can have both `ROLE_USER` and `ROLE_ADMIN`.

---

### 4.2. Category and Product

One category can contain many products.

Relationship:

```text
categories 1 ---- n products
```

Example:

- The category `T-Shirts` can contain many T-shirt products.
- The category `Shoes` can contain many shoe products.

---

### 4.3. Product and Product Variant

One product can have many variants.

Relationship:

```text
products 1 ---- n product_variants
```

Example:

The product `Basic T-Shirt` may have these variants:

- White, size M
- White, size L
- Black, size M
- Black, size L

Each variant has its own SKU, price and stock quantity.

---

### 4.4. User and Cart

Each user has one shopping cart.

Relationship:

```text
users 1 ---- 1 carts
```

---

### 4.5. Cart and Cart Item

One cart can contain many cart items.

Relationship:

```text
carts 1 ---- n cart_items
```

Each cart item points to a specific product variant.

---

### 4.6. User and Order

One user can create many orders.

Relationship:

```text
users 1 ---- n orders
```

---

### 4.7. Order and Order Item

One order can contain many order items.

Relationship:

```text
orders 1 ---- n order_items
```

---

### 4.8. Order and Payment

One order has one payment record in the first version of the system.

Relationship:

```text
orders 1 ---- 1 payments
```

---

## 5. Table Design Details

---

## 5.1. users

### Purpose

The `users` table stores account information for customers and administrators.

### Table Structure

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | User ID |
| full_name | VARCHAR(100) | NOT NULL | User full name |
| email | VARCHAR(150) | NOT NULL, UNIQUE | Login email |
| password | VARCHAR(255) | NOT NULL | Encrypted password |
| phone | VARCHAR(20) | NULL | User phone number |
| status | VARCHAR(30) | NOT NULL | Account status |
| created_at | DATETIME | NOT NULL | Created time |
| updated_at | DATETIME | NULL | Last updated time |

### Suggested Status Values

```text
ACTIVE
INACTIVE
LOCKED
```

### Suggested Indexes

| Index Name | Column | Reason |
|---|---|---|
| idx_users_email | email | Speed up login and email existence checks |
| idx_users_status | status | Filter users by account status |

### Business Notes

- Email must be unique.
- Password must be encrypted before storing.
- Plain text passwords must never be stored in the database.

---

## 5.2. roles

### Purpose

The `roles` table stores available system roles.

### Table Structure

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Role ID |
| name | VARCHAR(50) | NOT NULL, UNIQUE | Role name |

### Sample Data

```text
ROLE_USER
ROLE_ADMIN
```

### Business Notes

- `ROLE_USER` is used for normal customers.
- `ROLE_ADMIN` is used for system administrators.

---

## 5.3. user_roles

### Purpose

The `user_roles` table is a join table that represents the many-to-many relationship between users and roles.

### Table Structure

| Column | Type | Constraint | Description |
|---|---|---|---|
| user_id | BIGINT | PK, FK | User ID |
| role_id | BIGINT | PK, FK | Role ID |

### Primary Key

```text
PRIMARY KEY (user_id, role_id)
```

### Foreign Keys

```text
user_id REFERENCES users(id)
role_id REFERENCES roles(id)
```

### Business Notes

- A user can have more than one role.
- The same role can be assigned to many users.

---

## 5.4. categories

### Purpose

The `categories` table stores product categories.

### Table Structure

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Category ID |
| name | VARCHAR(100) | NOT NULL | Category name |
| description | TEXT | NULL | Category description |
| status | VARCHAR(30) | NOT NULL | Category status |
| created_at | DATETIME | NOT NULL | Created time |
| updated_at | DATETIME | NULL | Last updated time |

### Suggested Status Values

```text
ACTIVE
INACTIVE
```

### Suggested Indexes

| Index Name | Column | Reason |
|---|---|---|
| idx_categories_status | status | Filter active and inactive categories |
| idx_categories_name | name | Search category by name |

### Business Notes

- A category can contain many products.
- Inactive categories should not be displayed to normal users.

---

## 5.5. products

### Purpose

The `products` table stores general product information.

### Table Structure

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Product ID |
| category_id | BIGINT | FK, NOT NULL | Category ID |
| name | VARCHAR(150) | NOT NULL | Product name |
| description | TEXT | NULL | Product description |
| price | DECIMAL(15,2) | NOT NULL | Base product price |
| thumbnail_url | VARCHAR(255) | NULL | Product thumbnail image |
| status | VARCHAR(30) | NOT NULL | Product status |
| created_at | DATETIME | NOT NULL | Created time |
| updated_at | DATETIME | NULL | Last updated time |

### Suggested Status Values

```text
ACTIVE
INACTIVE
OUT_OF_STOCK
```

### Suggested Indexes

| Index Name | Column | Reason |
|---|---|---|
| idx_products_category_id | category_id | Filter products by category |
| idx_products_status | status | Filter active products |
| idx_products_name | name | Search product by name |

### Foreign Key

```text
category_id REFERENCES categories(id)
```

### Business Notes

- A product belongs to one category.
- A product can have multiple variants.
- Product price is the base price. A variant can have its own price.

---

## 5.6. product_variants

### Purpose

The `product_variants` table stores specific versions of a product, such as size, color, SKU and stock quantity.

### Table Structure

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Product variant ID |
| product_id | BIGINT | FK, NOT NULL | Product ID |
| sku | VARCHAR(100) | NOT NULL, UNIQUE | Stock Keeping Unit |
| color | VARCHAR(50) | NULL | Product color |
| size | VARCHAR(50) | NULL | Product size |
| stock_quantity | INT | NOT NULL | Available stock quantity |
| price | DECIMAL(15,2) | NOT NULL | Variant price |
| status | VARCHAR(30) | NOT NULL | Variant status |
| created_at | DATETIME | NOT NULL | Created time |
| updated_at | DATETIME | NULL | Last updated time |

### Suggested Status Values

```text
ACTIVE
INACTIVE
OUT_OF_STOCK
```

### Suggested Indexes

| Index Name | Column | Reason |
|---|---|---|
| idx_product_variants_product_id | product_id | Find variants of a product |
| idx_product_variants_sku | sku | Find variant by SKU |
| idx_product_variants_status | status | Filter active variants |

### Foreign Key

```text
product_id REFERENCES products(id)
```

### Business Notes

- SKU must be unique.
- Stock quantity must not be negative.
- The system must check stock quantity before creating an order.
- Product variants are used instead of storing stock directly in the products table because each size and color can have different stock.

---

## 5.7. carts

### Purpose

The `carts` table stores shopping carts for users.

### Table Structure

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Cart ID |
| user_id | BIGINT | FK, NOT NULL, UNIQUE | User ID |
| created_at | DATETIME | NOT NULL | Created time |
| updated_at | DATETIME | NULL | Last updated time |

### Suggested Indexes

| Index Name | Column | Reason |
|---|---|---|
| idx_carts_user_id | user_id | Find cart by user |

### Foreign Key

```text
user_id REFERENCES users(id)
```

### Business Notes

- Each user has one cart.
- A cart can contain many cart items.

---

## 5.8. cart_items

### Purpose

The `cart_items` table stores product variants added to a user's cart.

### Table Structure

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Cart item ID |
| cart_id | BIGINT | FK, NOT NULL | Cart ID |
| product_variant_id | BIGINT | FK, NOT NULL | Product variant ID |
| quantity | INT | NOT NULL | Item quantity |
| created_at | DATETIME | NOT NULL | Created time |
| updated_at | DATETIME | NULL | Last updated time |

### Suggested Indexes

| Index Name | Column | Reason |
|---|---|---|
| idx_cart_items_cart_id | cart_id | Find all items in a cart |
| idx_cart_items_product_variant_id | product_variant_id | Find cart items by product variant |

### Foreign Keys

```text
cart_id REFERENCES carts(id)
product_variant_id REFERENCES product_variants(id)
```

### Business Notes

- Quantity must be greater than 0.
- The same product variant should not be duplicated in the same cart.
- If a user adds the same variant again, the system should update the quantity instead of creating a duplicate row.

---

## 5.9. orders

### Purpose

The `orders` table stores customer orders.

### Table Structure

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Order ID |
| user_id | BIGINT | FK, NOT NULL | User ID |
| order_code | VARCHAR(50) | NOT NULL, UNIQUE | Unique order code |
| total_amount | DECIMAL(15,2) | NOT NULL | Total order amount |
| status | VARCHAR(30) | NOT NULL | Order status |
| created_at | DATETIME | NOT NULL | Created time |
| updated_at | DATETIME | NULL | Last updated time |

### Suggested Order Status Values

```text
PENDING
PAID
CONFIRMED
SHIPPING
COMPLETED
CANCELLED
FAILED
```

### Suggested Indexes

| Index Name | Column | Reason |
|---|---|---|
| idx_orders_user_id | user_id | Find orders by user |
| idx_orders_order_code | order_code | Find order by code |
| idx_orders_status | status | Filter orders by status |
| idx_orders_created_at | created_at | Sort or filter orders by creation time |

### Foreign Key

```text
user_id REFERENCES users(id)
```

### Business Notes

- Order code must be unique.
- Order creation must be handled inside a database transaction.
- When an order is created, the system must check stock and decrease stock quantity.
- If any step fails during order creation, the whole transaction must be rolled back.

---

## 5.10. order_items

### Purpose

The `order_items` table stores item details of each order.

### Table Structure

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Order item ID |
| order_id | BIGINT | FK, NOT NULL | Order ID |
| product_variant_id | BIGINT | FK, NOT NULL | Product variant ID |
| product_name | VARCHAR(150) | NOT NULL | Product name at purchase time |
| variant_info | VARCHAR(150) | NULL | Variant information at purchase time |
| quantity | INT | NOT NULL | Purchased quantity |
| unit_price | DECIMAL(15,2) | NOT NULL | Unit price at purchase time |
| total_price | DECIMAL(15,2) | NOT NULL | Total price of this item |

### Suggested Indexes

| Index Name | Column | Reason |
|---|---|---|
| idx_order_items_order_id | order_id | Find all items in an order |
| idx_order_items_product_variant_id | product_variant_id | Track sold variants |

### Foreign Keys

```text
order_id REFERENCES orders(id)
product_variant_id REFERENCES product_variants(id)
```

### Business Notes

- `product_name`, `variant_info` and `unit_price` are stored directly in `order_items`.
- This is important because order history must not change when product information changes later.
- Total price should be calculated as:

```text
total_price = quantity * unit_price
```

---

## 5.11. payments

### Purpose

The `payments` table stores payment information for orders.

### Table Structure

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Payment ID |
| order_id | BIGINT | FK, NOT NULL, UNIQUE | Order ID |
| payment_method | VARCHAR(50) | NOT NULL | Payment method |
| amount | DECIMAL(15,2) | NOT NULL | Payment amount |
| status | VARCHAR(30) | NOT NULL | Payment status |
| transaction_code | VARCHAR(100) | NULL, UNIQUE | Payment transaction code |
| paid_at | DATETIME | NULL | Payment completed time |
| created_at | DATETIME | NOT NULL | Created time |

### Suggested Payment Methods

```text
COD
FAKE_BANKING
FAKE_VNPAY
```

### Suggested Payment Status Values

```text
PENDING
SUCCESS
FAILED
```

### Suggested Indexes

| Index Name | Column | Reason |
|---|---|---|
| idx_payments_order_id | order_id | Find payment by order |
| idx_payments_status | status | Filter payments by status |
| idx_payments_transaction_code | transaction_code | Find payment by transaction code |

### Foreign Key

```text
order_id REFERENCES orders(id)
```

### Business Notes

- In the first version, one order has one payment record.
- If payment is successful, the order status should be updated to `PAID`.
- If payment fails, the order can remain `PENDING` or become `FAILED`, depending on business rules.

---

## 6. Business Rules

---

## 6.1. Create Order Flow

When a user creates an order, the system should follow this flow:

```text
1. User sends a create order request.
2. System gets the user's cart.
3. System checks whether the cart is empty.
4. System checks stock quantity for each cart item.
5. System creates a new order with status PENDING.
6. System creates order item records.
7. System decreases stock quantity of each product variant.
8. System clears cart items.
9. System returns the order response.
```

Important rule:

```text
The create order process must be executed inside a database transaction.
```

If any step fails, the whole transaction must be rolled back.

---

## 6.2. Payment Flow

When a user makes a fake payment, the system should follow this flow:

```text
1. User sends a fake payment request.
2. System checks whether the order exists.
3. System checks whether the order belongs to the current user.
4. System checks whether the order is still payable.
5. System creates or updates the payment record.
6. If payment is successful, system updates order status to PAID.
7. System publishes a PAYMENT_SUCCESS event in a later phase.
8. System returns the payment response.
```

---

## 6.3. Inventory Rule

The system must not allow users to create an order if the requested quantity is greater than the available stock quantity.

Example:

```text
Product variant stock quantity: 5
Requested quantity: 7
Result: Order creation must fail
```

---

## 6.4. Order History Rule

Order item information must remain unchanged even if product information changes later.

For this reason, the system stores the following fields directly in `order_items`:

```text
product_name
variant_info
unit_price
```

---

## 6.5. Cart Rule

A user should not have duplicate cart items for the same product variant.

Example:

```text
If product_variant_id = 10 already exists in the user's cart,
adding the same product variant again should update the quantity.
```

---

## 7. Suggested SQL DDL

The SQL below is an initial database schema for development.

```sql
CREATE DATABASE IF NOT EXISTS orderhub_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE orderhub_db;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_status (status)
);

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_categories_status (status),
    INDEX idx_categories_name (name)
);

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(15,2) NOT NULL,
    thumbnail_url VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_products_category_id (category_id),
    INDEX idx_products_status (status),
    INDEX idx_products_name (name)
);

CREATE TABLE product_variants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(50),
    size VARCHAR(50),
    stock_quantity INT NOT NULL DEFAULT 0,
    price DECIMAL(15,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_variants_product
        FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_product_variants_stock
        CHECK (stock_quantity >= 0),
    INDEX idx_product_variants_product_id (product_id),
    INDEX idx_product_variants_sku (sku),
    INDEX idx_product_variants_status (status)
);

CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_carts_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_carts_user_id (user_id)
);

CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_variant_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id) REFERENCES carts(id),
    CONSTRAINT fk_cart_items_product_variant
        FOREIGN KEY (product_variant_id) REFERENCES product_variants(id),
    CONSTRAINT chk_cart_items_quantity
        CHECK (quantity > 0),
    CONSTRAINT uk_cart_variant
        UNIQUE (cart_id, product_variant_id),
    INDEX idx_cart_items_cart_id (cart_id),
    INDEX idx_cart_items_product_variant_id (product_variant_id)
);

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    total_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_order_code (order_code),
    INDEX idx_orders_status (status),
    INDEX idx_orders_created_at (created_at)
);

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_variant_id BIGINT NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    variant_info VARCHAR(150),
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_product_variant
        FOREIGN KEY (product_variant_id) REFERENCES product_variants(id),
    CONSTRAINT chk_order_items_quantity
        CHECK (quantity > 0),
    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_product_variant_id (product_variant_id)
);

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    transaction_code VARCHAR(100) UNIQUE,
    paid_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_payments_order_id (order_id),
    INDEX idx_payments_status (status),
    INDEX idx_payments_transaction_code (transaction_code)
);
```

---

## 8. Initial Seed Data

The system should insert default roles when the application starts or when the database is initialized.

```sql
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
```

Optional sample categories:

```sql
INSERT INTO categories (name, description, status)
VALUES 
('T-Shirts', 'T-shirt products', 'ACTIVE'),
('Shoes', 'Shoe products', 'ACTIVE'),
('Accessories', 'Fashion accessories', 'ACTIVE');
```

---

## 9. Query Optimization Notes

### 9.1. Product Listing

The product listing API will usually filter by category and status.

Example query:

```sql
SELECT *
FROM products
WHERE status = 'ACTIVE'
ORDER BY created_at DESC;
```

Useful indexes:

```text
idx_products_status
idx_products_category_id
```

---

### 9.2. User Order History

The order history API will usually get orders by user ID and sort by created time.

Example query:

```sql
SELECT *
FROM orders
WHERE user_id = ?
ORDER BY created_at DESC;
```

Useful indexes:

```text
idx_orders_user_id
idx_orders_created_at
```

---

### 9.3. Login Query

The login feature will find users by email.

Example query:

```sql
SELECT *
FROM users
WHERE email = ?;
```

Useful index:

```text
idx_users_email
```

---

## 10. Future Database Improvements

In later phases, this project can be improved with:

- Flyway for database migration management.
- Redis for product caching.
- MongoDB for notifications and audit logs.
- RabbitMQ events for order and payment processing.
- Optimistic locking for inventory updates.
- Query analysis using `EXPLAIN`.
- Separate databases for microservices.
- Soft delete support using `deleted_at`.
- Audit fields such as `created_by` and `updated_by`.

---

## 11. Conclusion

This database design provides a clear relational structure for the first version of OrderHub Backend System.

It supports:

- User authentication and authorization.
- Product and variant management.
- Shopping cart management.
- Order creation with inventory validation.
- Payment simulation.
- Basic indexing for common queries.
- Transaction-safe order processing.

This schema is suitable for the monolithic version of the project and can be extended later when the system evolves into a microservice architecture.