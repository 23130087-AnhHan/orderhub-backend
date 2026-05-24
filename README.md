# OrderHub Backend System

OrderHub Backend System is a production-oriented backend project built with **Java Spring Boot**.  
The project simulates a real e-commerce order management system and focuses on backend engineering fundamentals such as RESTful API design, relational database design, authentication, transaction handling, caching, message queue, NoSQL storage, Docker containerization and clean architecture.

This project is designed as a portfolio project for a **Backend Engineer Intern** position.

---

## 1. Project Overview

OrderHub is a backend system that supports the main business flow of an e-commerce platform:

```text
User registers / logs in
→ User views products
→ User adds product variants to cart
→ User creates an order from cart
→ User makes a fake payment
→ System updates order status
→ System creates notifications
```

The system starts as a **modular monolithic application**, but it is designed in a way that can be extended into microservices later.

---

## 2. Why This Project Was Built

This project was built to practice and demonstrate backend engineering skills required in real-world software development.

The project covers:

- RESTful API design
- Java Spring Boot backend development
- MySQL relational database design
- JWT authentication and authorization
- Transaction handling for order creation
- Redis caching for product APIs
- RabbitMQ event-driven communication
- MongoDB document storage for notifications
- Docker Compose for local infrastructure
- Git and GitHub workflow
- Layered architecture: Controller, Service, Repository, Entity, DTO, Mapper

---

## 3. Technology Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| API | Spring Web |
| Security | Spring Security, JWT |
| Database | MySQL |
| ORM | Spring Data JPA, Hibernate |
| Cache | Redis |
| Message Queue | RabbitMQ |
| NoSQL | MongoDB |
| Build Tool | Maven |
| Containerization | Docker, Docker Compose |
| API Testing | Postman |
| Version Control | Git, GitHub |

---

## 4. Main Features

### Authentication

- Register account
- Login with email and password
- Password encryption with BCrypt
- JWT access token generation
- Get current authenticated user
- Role-based authorization

### Product Module

- Public users can view product list
- Public users can view product detail
- Admin can create categories
- Admin can create products with variants
- Admin can update products
- Admin can soft-delete products

### Cart Module

- Authenticated user can view cart
- Add product variant to cart
- Update cart item quantity
- Remove item from cart
- Prevent duplicate product variant rows in the same cart

### Order Module

- Create order from current user's cart
- Check cart before creating order
- Check product stock before creating order
- Decrease stock quantity after order creation
- Clear cart after order creation
- View order history
- View order detail
- Prevent users from viewing another user's order

### Payment Module

- Fake payment simulation
- Payment success updates order status to `PAID`
- Payment failure creates failed payment record
- Prevent paying an already paid order
- Prevent user from paying another user's order

### Notification Module

- Create notification when order is created
- Create notification when payment succeeds or fails
- View notifications
- Mark notification as read
- Notifications are stored in MongoDB

---

## 5. System Architecture

The current version uses a **modular monolithic architecture**.

```text
Client / Postman
        |
        v
Spring Boot REST API
        |
        v
Controller Layer
        |
        v
Service Layer
        |
        v
Repository Layer
        |
        v
Database / Cache / Message Queue
```

Infrastructure components:

```text
Spring Boot Backend
├── MySQL      → core business data
├── Redis      → product API cache
├── RabbitMQ   → event-driven notification
└── MongoDB    → notification document storage
```

---

## 6. Project Structure

```text
src/main/java/com/orderhub
├── auth
│   ├── controller
│   ├── dto
│   └── service
│
├── user
│   ├── entity
│   └── repository
│
├── product
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── cart
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── order
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── payment
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── notification
│   ├── controller
│   ├── document
│   ├── dto
│   ├── mapper
│   ├── repository
│   └── service
│
├── event
│   ├── config
│   ├── consumer
│   ├── dto
│   └── publisher
│
├── common
│   ├── controller
│   ├── exception
│   └── response
│
├── config
└── security
```

---

## 7. Layer Responsibilities

### Controller Layer

The controller layer receives HTTP requests and returns API responses.

Responsibilities:

- Receive request body and query parameters
- Validate input
- Call service layer
- Return `ApiResponse`
- Do not contain business logic

Example:

```text
ProductController
CartController
OrderController
PaymentController
NotificationController
```

### Service Layer

The service layer contains business logic.

Responsibilities:

- Validate business rules
- Handle transactions
- Coordinate repositories
- Publish events
- Throw custom exceptions

Example:

```text
OrderService creates order, checks stock, decreases stock and clears cart.
PaymentService processes fake payment and publishes payment event.
```

### Repository Layer

The repository layer communicates with the database.

Responsibilities:

- Query MySQL using JPA
- Query MongoDB using MongoRepository
- Provide data access methods

### Entity Layer

Entity classes map Java objects to MySQL tables.

Example:

```text
User
Role
Product
ProductVariant
Cart
Order
Payment
```

### Document Layer

Document classes map Java objects to MongoDB collections.

Example:

```text
Notification
```

### DTO Layer

DTO classes define request and response data.

Benefits:

- Avoid exposing entity directly
- Control API response structure
- Validate request data
- Hide sensitive information

### Mapper Layer

Mapper classes convert data between entities/documents and DTOs.

Example:

```text
Product → ProductResponse
Cart → CartResponse
Order → OrderResponse
Notification → NotificationResponse
```

---

## 8. Database Design

The core business data is stored in MySQL.

Main tables:

```text
users
roles
user_roles
categories
products
product_variants
carts
cart_items
orders
order_items
payments
```

### Important Relationships

```text
User 1 ---- 1 Cart
Cart 1 ---- n CartItem
Product 1 ---- n ProductVariant
User 1 ---- n Order
Order 1 ---- n OrderItem
Order 1 ---- 1 Payment
```

### Why MySQL?

MySQL is used for core business data because this data requires:

- Strong relationships
- Foreign keys
- Data consistency
- Transaction support
- Query optimization with indexes

---

## 9. Redis Cache Flow

Redis is used to cache public product APIs.

Cached APIs:

```http
GET /api/v1/products
GET /api/v1/products/{id}
```

Flow:

```text
Client requests product API
→ Backend checks Redis
→ If cache exists, return cached data
→ If cache does not exist, query MySQL
→ Store result in Redis
→ Return response
```

When admin creates, updates or deletes products, product cache is cleared.

This prevents users from seeing outdated product data.

---

## 10. RabbitMQ Event-Driven Flow

RabbitMQ is used for event-driven notification creation.

Before RabbitMQ:

```text
OrderService → NotificationService
PaymentService → NotificationService
```

After RabbitMQ:

```text
OrderService
→ EventPublisher
→ RabbitMQ
→ NotificationEventConsumer
→ NotificationService
→ MongoDB
```

Events:

```text
ORDER_CREATED
PAYMENT_SUCCESS
PAYMENT_FAILED
```

Benefits:

- Reduces direct dependency between services
- Makes notification processing asynchronous
- Makes the system easier to migrate to microservices later
- Demonstrates event-driven architecture

---

## 11. MongoDB Notification Storage

Notifications are stored in MongoDB instead of MySQL.

Why MongoDB?

Notifications are document-based data. They do not require complex relational joins.

Example notification document:

```json
{
  "userId": 1,
  "type": "ORDER_CREATED",
  "title": "Order created",
  "message": "Your order ORD-20260521-XXXXXXXX has been created.",
  "read": false,
  "createdAt": "2026-05-21T10:00:00"
}
```

MongoDB is suitable for:

- Notifications
- Audit logs
- Event logs
- Flexible document data

---

## 12. API Response Format

All APIs use a consistent response format.

### Success Response

```json
{
  "success": true,
  "message": "Request processed successfully",
  "data": {},
  "timestamp": "2026-05-21T10:00:00"
}
```

### Error Response

```json
{
  "success": false,
  "message": "Error message",
  "errors": null,
  "timestamp": "2026-05-21T10:00:00"
}
```

---

## 13. Main API Endpoints

### Health

```http
GET /api/v1/health
```

### Auth

```http
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/me
```

### Products

```http
GET /api/v1/products
GET /api/v1/products/{id}
GET /api/v1/categories
```

### Admin Products

```http
POST   /api/v1/admin/categories
POST   /api/v1/admin/products
PUT    /api/v1/admin/products/{id}
DELETE /api/v1/admin/products/{id}
```

### Cart

```http
GET    /api/v1/cart
POST   /api/v1/cart/items
PUT    /api/v1/cart/items/{id}
DELETE /api/v1/cart/items/{id}
```

### Orders

```http
POST /api/v1/orders
GET  /api/v1/orders
GET  /api/v1/orders/{id}
```

### Payments

```http
POST /api/v1/payments/fake-pay
GET  /api/v1/payments/orders/{orderId}
```

### Notifications

```http
GET /api/v1/notifications
PUT /api/v1/notifications/{id}/read
```

---

## 14. How To Run With Docker Compose

### Prerequisites

Make sure you have installed:

```text
Docker Desktop
Git
Java 21
Maven or Maven Wrapper
```

### Step 1: Clone the project

```bash
git clone https://github.com/your-github-username/orderhub-backend.git
cd orderhub-backend
```

### Step 2: Start all services

```bash
docker compose up -d --build
```

This command starts:

```text
Spring Boot backend
MySQL
Redis
RabbitMQ
MongoDB
```

### Step 3: Check running containers

```bash
docker ps
```

Expected containers:

```text
orderhub-backend
orderhub-mysql
orderhub-redis
orderhub-rabbitmq
orderhub-mongodb
```

### Step 4: View backend logs

```bash
docker compose logs -f backend
```

The backend is ready when you see:

```text
Started OrderHubApplication
Tomcat started on port 8080
```

### Step 5: Test health API

```http
GET http://localhost:8080/api/v1/health
```

Expected response:

```json
{
  "success": true,
  "message": "Health check successful",
  "data": "OrderHub Backend is running"
}
```

---

## 15. Docker Services

| Service | Container Name | Port |
|---|---|---|
| Backend | orderhub-backend | 8080 |
| MySQL | orderhub-mysql | 3307:3306 |
| Redis | orderhub-redis | 6379 |
| RabbitMQ | orderhub-rabbitmq | 5672, 15672 |
| MongoDB | orderhub-mongodb | 27017 |

RabbitMQ Management UI:

```text
http://localhost:15672
```

Default login:

```text
Username: orderhub
Password: orderhub_password
```

---

## 16. Useful Docker Commands

Start all services:

```bash
docker compose up -d --build
```

Stop all services:

```bash
docker compose down
```

Stop and remove volumes:

```bash
docker compose down -v
```

View logs:

```bash
docker compose logs -f backend
```

Restart backend:

```bash
docker compose restart backend
```

Check containers:

```bash
docker ps
```

---

## 17. How To Test Main Business Flow

### Step 1: Register

```http
POST http://localhost:8080/api/v1/auth/register
```

Body:

```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "Password123",
  "phone": "0909123456"
}
```

### Step 2: Login

```http
POST http://localhost:8080/api/v1/auth/login
```

Body:

```json
{
  "email": "john@example.com",
  "password": "Password123"
}
```

Copy the `accessToken`.

Use it in protected APIs:

```http
Authorization: Bearer <USER_TOKEN>
```

### Step 3: Create admin user

By default, registered users have `ROLE_USER`.

To test admin APIs, add `ROLE_ADMIN` manually in MySQL:

```sql
SELECT * FROM users;
SELECT * FROM roles;

INSERT INTO user_roles (user_id, role_id)
VALUES (1, 2);
```

After assigning admin role, login again to get a new token.

### Step 4: Create category

```http
POST http://localhost:8080/api/v1/admin/categories
Authorization: Bearer <ADMIN_TOKEN>
```

Body:

```json
{
  "name": "T-Shirts",
  "description": "T-shirt products",
  "status": "ACTIVE"
}
```

### Step 5: Create product

```http
POST http://localhost:8080/api/v1/admin/products
Authorization: Bearer <ADMIN_TOKEN>
```

Body:

```json
{
  "categoryId": 1,
  "name": "Basic T-Shirt",
  "description": "A basic cotton T-shirt",
  "price": 199000,
  "thumbnailUrl": "https://example.com/tshirt.jpg",
  "status": "ACTIVE",
  "variants": [
    {
      "sku": "TSHIRT-WHITE-M",
      "color": "White",
      "size": "M",
      "stockQuantity": 20,
      "price": 199000,
      "status": "ACTIVE"
    }
  ]
}
```

### Step 6: Add item to cart

```http
POST http://localhost:8080/api/v1/cart/items
Authorization: Bearer <USER_TOKEN>
```

Body:

```json
{
  "productVariantId": 1,
  "quantity": 2
}
```

### Step 7: Create order

```http
POST http://localhost:8080/api/v1/orders
Authorization: Bearer <USER_TOKEN>
```

Body:

```json
{
  "note": "Please deliver after 6 PM"
}
```

After order creation:

```text
Order status = PENDING
Product stock decreases
Cart is cleared
ORDER_CREATED event is published
Notification is created in MongoDB
```

### Step 8: Fake payment

```http
POST http://localhost:8080/api/v1/payments/fake-pay
Authorization: Bearer <USER_TOKEN>
```

Body:

```json
{
  "orderId": 1,
  "paymentMethod": "FAKE_VNPAY",
  "success": true
}
```

After successful payment:

```text
Payment status = SUCCESS
Order status = PAID
PAYMENT_SUCCESS event is published
Notification is created in MongoDB
```

### Step 9: View notifications

```http
GET http://localhost:8080/api/v1/notifications
Authorization: Bearer <USER_TOKEN>
```

### Step 10: Mark notification as read

```http
PUT http://localhost:8080/api/v1/notifications/{notificationId}/read
Authorization: Bearer <USER_TOKEN>
```

---

## 18. How To Check Redis

Open Redis CLI:

```bash
docker exec -it orderhub-redis redis-cli
```

Ping Redis:

```bash
PING
```

Expected:

```text
PONG
```

Show cache keys:

```bash
KEYS *
```

---

## 19. How To Check RabbitMQ

Open RabbitMQ UI:

```text
http://localhost:15672
```

Login:

```text
Username: orderhub
Password: orderhub_password
```

Check:

```text
Exchanges → orderhub.exchange
Queues → notification.queue
```

---

## 20. How To Check MongoDB

Open Mongo shell:

```bash
docker exec -it orderhub-mongodb mongosh -u orderhub -p orderhub_password --authenticationDatabase admin
```

Use database:

```javascript
use orderhub_notification_db
```

Find notifications:

```javascript
db.notifications.find().pretty()
```

---

## 21. Business Rules

### Register Rule

- Email must be unique
- Password must be encrypted before saving
- Default role is `ROLE_USER`

### Product Rule

- Product belongs to one category
- Product can have many variants
- SKU must be unique
- Product variant stores stock quantity

### Cart Rule

- Each user has one cart
- Cart item points to product variant
- Same product variant must not be duplicated in the same cart
- Adding the same variant increases quantity

### Order Rule

- Order is created from cart
- Empty cart cannot create order
- System checks stock before creating order
- Stock is decreased after order creation
- Cart is cleared after order creation
- Order creation must be transactional

### Payment Rule

- Only order owner can pay the order
- Paid order cannot be paid again
- Successful payment updates order status to `PAID`
- Failed payment creates failed payment record

### Notification Rule

- Order creation creates `ORDER_CREATED` notification
- Payment success creates `PAYMENT_SUCCESS` notification
- Payment failure creates `PAYMENT_FAILED` notification
- User can only read their own notifications

---

## 22. What I Learned

Through this project, I practiced:

- Designing REST APIs
- Designing relational database schema
- Implementing JWT authentication
- Using Spring Security
- Handling transactions with `@Transactional`
- Using DTOs and mappers
- Writing clean service-layer business logic
- Using Redis cache to improve read performance
- Using RabbitMQ for event-driven communication
- Using MongoDB for document-based notification storage
- Running backend infrastructure with Docker Compose
- Managing project workflow with Git branches, issues and pull requests

---

## 23. Future Improvements

Planned improvements:

- Add Postman Collection
- Add Swagger/OpenAPI documentation
- Add GitHub Actions CI pipeline
- Add unit tests for service layer
- Add integration tests with Testcontainers
- Add refresh token
- Add logout and Redis token blacklist
- Add admin order management APIs
- Add shipping tracking simulation
- Add audit log module
- Add real payment gateway integration
- Split modular monolith into microservices

---

## 24. Interview Explanation

This project demonstrates backend engineering skills beyond simple CRUD.

Key points to explain in an interview:

```text
I designed the system as a modular monolith first to keep development simple while maintaining clean module boundaries.

I used MySQL for core business data because users, products, carts, orders and payments have strong relational structure.

I used Redis to cache product APIs because product listing and product detail are read frequently.

I used RabbitMQ to decouple order/payment logic from notification creation, which makes the system closer to event-driven architecture.

I used MongoDB for notifications because notifications are document-based and do not require complex joins.

I used Docker Compose to run the backend and infrastructure consistently across environments.

I used DTOs and mappers to avoid exposing entities directly through APIs.

I used @Transactional in order and payment flows to protect data consistency.
```

---

## 25. Current Project Status

Completed:

```text
Requirements documentation
Database design
API design
Architecture design
Spring Boot setup
Common response and exception handling
JWT authentication
Product module
Cart module
Order module
Payment module
Notification module
Docker Compose
Redis cache
RabbitMQ event-driven notification
MongoDB notification storage
```

Next steps:

```text
Postman Collection
Swagger/OpenAPI
GitHub Actions CI
Unit tests
Integration tests
Deployment
```

---

## 26. Author

Project name:

```text
OrderHub Backend System
```

Purpose:

```text
Backend Engineer Intern portfolio project
```

Main focus:

```text
Java Spring Boot, MySQL, Redis, RabbitMQ, MongoDB, Docker, REST API, JWT, clean architecture
```