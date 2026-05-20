# 04. Architecture Design - OrderHub Backend System

## 1. Architecture Overview

OrderHub Backend System is designed as a **modular monolithic backend application** in the first version.

The system is built with:

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- JWT Authentication
- MySQL
- Redis
- RabbitMQ
- MongoDB
- Docker
- GitHub Actions

The first version should start as a modular monolith because it is easier to build, test, debug and deploy while still keeping the source code organized by business modules.

In later phases, the system can evolve into a microservice architecture by splitting modules such as Auth, Product, Order, Payment and Notification into separate services.

---

## 2. Why Start With Modular Monolith

The project starts with a modular monolith instead of microservices.

### Reasons

- Easier for development and learning.
- Easier to debug and test.
- Easier to manage transactions in the order creation flow.
- Avoids unnecessary distributed system complexity at the beginning.
- Still allows clean module separation.
- Can be refactored into microservices later.

### Main Idea

Although the system is deployed as one Spring Boot application in the first version, the code is organized by modules:

```text
auth
user
product
cart
order
payment
notification
```

Each module has its own controller, service, repository, entity, DTO and mapper when needed.

---

## 3. High-Level Architecture

The high-level architecture of the first version:

```text
Client / Postman / Swagger
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
MySQL Database
```

Additional infrastructure components in later phases:

```text
Spring Boot Application
        |
        |---- MySQL      : Core business data
        |---- Redis      : Product cache and token blacklist
        |---- RabbitMQ   : Order and payment events
        |---- MongoDB    : Notifications and audit logs
```

---

## 4. Main Backend Modules

The application is divided into the following modules.

| Module | Responsibility |
|---|---|
| auth | Register, login, JWT generation, authentication |
| user | User profile and role information |
| product | Categories, products and product variants |
| cart | Shopping cart and cart items |
| order | Order creation, order history and order status |
| payment | Fake payment processing |
| notification | User notifications |
| common | Shared response, exception, constants and utilities |
| config | Application configuration |
| security | Spring Security and JWT configuration |

---

## 5. Project Package Structure

The suggested package structure:

```text
src/main/java/com/orderhub
├── OrderHubApplication.java
│
├── common
│   ├── constants
│   ├── exception
│   ├── response
│   └── util
│
├── config
│   ├── OpenApiConfig.java
│   ├── RedisConfig.java
│   ├── RabbitMQConfig.java
│   └── MongoConfig.java
│
├── security
│   ├── JwtAuthenticationFilter.java
│   ├── JwtService.java
│   ├── SecurityConfig.java
│   └── UserDetailsServiceImpl.java
│
├── auth
│   ├── controller
│   ├── dto
│   ├── service
│   └── mapper
│
├── user
│   ├── entity
│   ├── repository
│   ├── service
│   ├── dto
│   └── mapper
│
├── product
│   ├── controller
│   ├── entity
│   ├── repository
│   ├── service
│   ├── dto
│   └── mapper
│
├── cart
│   ├── controller
│   ├── entity
│   ├── repository
│   ├── service
│   ├── dto
│   └── mapper
│
├── order
│   ├── controller
│   ├── entity
│   ├── repository
│   ├── service
│   ├── dto
│   └── mapper
│
├── payment
│   ├── controller
│   ├── entity
│   ├── repository
│   ├── service
│   ├── dto
│   └── mapper
│
└── notification
    ├── controller
    ├── document
    ├── repository
    ├── service
    ├── dto
    └── mapper
```

---

## 6. Layered Architecture

The backend follows a layered architecture.

```text
Controller Layer
        |
        v
Service Layer
        |
        v
Repository Layer
        |
        v
Database Layer
```

Each layer has a clear responsibility.

---

## 7. Controller Layer

### Responsibility

The Controller layer handles HTTP requests and responses.

It should:

- Receive request data.
- Validate input using DTO validation.
- Call the service layer.
- Return a consistent API response.
- Not contain business logic.
- Not directly access repositories.

### Example

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> getProducts(ProductFilterRequest request) {
        return ApiResponse.success(productService.getProducts(request));
    }
}
```

### Rule

Controllers should be thin.

Bad practice:

```text
Controller directly queries database and handles business logic.
```

Good practice:

```text
Controller receives request → calls Service → returns response.
```

---

## 8. Service Layer

### Responsibility

The Service layer contains business logic.

It should:

- Validate business rules.
- Handle transaction boundaries.
- Coordinate multiple repositories.
- Call external infrastructure such as Redis or RabbitMQ when needed.
- Throw custom exceptions when business rules are violated.

### Example

```java
@Service
public class OrderService {

    @Transactional
    public OrderResponse createOrder(Long userId) {
        // Get cart
        // Check stock
        // Create order
        // Create order items
        // Decrease stock
        // Clear cart
        // Return response
    }
}
```

### Important Rule

The order creation flow must use `@Transactional`.

Reason:

```text
If order creation fails at any step, all database changes must be rolled back.
```

---

## 9. Repository Layer

### Responsibility

The Repository layer is responsible for database access.

It should:

- Extend Spring Data JPA repositories.
- Provide query methods.
- Use custom queries when needed.
- Not contain business logic.

### Example

```java
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStatus(String status, Pageable pageable);

    Optional<Product> findByIdAndStatus(Long id, String status);
}
```

---

## 10. Entity Layer

### Responsibility

The Entity layer maps Java objects to database tables.

Each main table in MySQL should have a corresponding entity.

Suggested entities:

```text
User
Role
Category
Product
ProductVariant
Cart
CartItem
Order
OrderItem
Payment
```

### Example

```java
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal price;
    private String status;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
```

### Rule

Entities should represent database structure, not API response structure.

Do not return entities directly from controllers.

---

## 11. DTO Layer

### Responsibility

DTOs are used to receive request data and return response data.

Types of DTOs:

```text
Request DTO
Response DTO
Filter DTO
```

### Examples

```text
RegisterRequest
LoginRequest
AuthResponse
ProductRequest
ProductResponse
CartItemRequest
OrderResponse
PaymentRequest
PaymentResponse
```

### Why Use DTOs

DTOs help to:

- Hide internal entity structure.
- Avoid exposing sensitive fields such as password.
- Customize API response format.
- Validate request input.
- Keep controllers and entities clean.

### Rule

Do not expose entity objects directly in API responses.

---

## 12. Mapper Layer

### Responsibility

Mapper classes convert between entities and DTOs.

Example conversions:

```text
ProductRequest → Product
Product → ProductResponse
Order → OrderResponse
```

### Example

```java
@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        return response;
    }
}
```

In the first version, manual mappers are acceptable.

In later versions, the project can use:

```text
MapStruct
```

---

## 13. Common API Response

All APIs should use a consistent response format.

### Success Response

```json
{
  "success": true,
  "message": "Request processed successfully",
  "data": {}
}
```

### Error Response

```json
{
  "success": false,
  "message": "Error message",
  "errors": []
}
```

### Suggested Class

```java
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        return response;
    }
}
```

---

## 14. Exception Handling Architecture

The system should use centralized exception handling.

### Main Components

```text
GlobalExceptionHandler
BusinessException
ResourceNotFoundException
UnauthorizedException
ForbiddenException
ValidationException
```

### Package

```text
common.exception
```

### Example

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }
}
```

### Benefits

- Consistent error response.
- Cleaner service code.
- Easier debugging.
- Better API consumer experience.

---

## 15. Security Architecture

The system uses Spring Security with JWT authentication.

### Main Security Flow

```text
1. User sends login request.
2. System validates email and password.
3. System generates JWT access token.
4. Client sends JWT in Authorization header.
5. JWT filter validates the token.
6. If token is valid, user is authenticated.
7. Spring Security checks role permissions.
```

### Header Format

```http
Authorization: Bearer <access_token>
```

### Public APIs

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/products
GET  /api/v1/products/{id}
```

### Protected User APIs

```text
GET    /api/v1/auth/me
GET    /api/v1/cart
POST   /api/v1/cart/items
POST   /api/v1/orders
GET    /api/v1/orders
POST   /api/v1/payments/fake-pay
GET    /api/v1/notifications
```

### Admin APIs

```text
POST   /api/v1/admin/products
PUT    /api/v1/admin/products/{id}
DELETE /api/v1/admin/products/{id}
GET    /api/v1/admin/orders
PUT    /api/v1/admin/orders/{id}/status
```

### Roles

```text
ROLE_USER
ROLE_ADMIN
```

---

## 16. Authentication and Authorization

### Authentication

Authentication answers the question:

```text
Who are you?
```

In this project, authentication is handled by login with email and password.

### Authorization

Authorization answers the question:

```text
What are you allowed to do?
```

In this project:

- Normal users can manage their cart, orders, payments and notifications.
- Admin users can manage products and update order status.

---

## 17. Configuration Strategy

The project should use multiple configuration files for different environments.

Suggested files:

```text
application.yml
application-dev.yml
application-prod.yml
application-test.yml
```

### application.yml

Common configuration.

```yaml
spring:
  profiles:
    active: dev
```

### application-dev.yml

Local development configuration.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/orderhub_db
    username: root
    password: root
```

### application-prod.yml

Production configuration.

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### Rule

Sensitive information should not be committed to GitHub.

Examples of sensitive information:

```text
Database password
JWT secret
Cloud credentials
Docker Hub token
```

Use environment variables for secrets.

---

## 18. Database Architecture

The first version uses MySQL as the main database.

### MySQL Stores

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

### Why MySQL

MySQL is used because the core business data requires:

- Strong relationships.
- Foreign keys.
- Transaction support.
- Data consistency.
- Query optimization with indexes.

### Transaction Example

The order creation flow must be transactional:

```text
Create order
Create order items
Decrease stock
Clear cart
```

If any step fails, the whole transaction must be rolled back.

---

## 19. Redis Architecture

Redis will be added in a later phase.

### Main Use Cases

```text
Product cache
Product detail cache
JWT blacklist after logout
Rate limiting in future phases
```

### Product Cache Flow

```text
1. Client requests product list.
2. System checks Redis cache.
3. If cache exists, return data from Redis.
4. If cache does not exist, query MySQL.
5. Save result to Redis.
6. Return data to client.
```

### Cache Invalidation Rule

When admin creates, updates or deletes a product, related product cache should be cleared.

---

## 20. RabbitMQ Architecture

RabbitMQ will be used for asynchronous event-driven communication.

### Main Events

```text
ORDER_CREATED
PAYMENT_SUCCESS
ORDER_CANCELLED
```

### Event Flow

```text
Order Service creates an order
        |
        v
Publish ORDER_CREATED event
        |
        v
Notification Consumer receives event
        |
        v
Create notification
```

### Benefits

- Decouples order logic from notification logic.
- Improves system scalability.
- Makes the system closer to real backend architecture.

---

## 21. MongoDB Architecture

MongoDB will be used for flexible document-based data.

### Main Use Cases

```text
Notifications
Audit logs
Event logs
```

### Why MongoDB For Notifications

Notifications can have different structures depending on the event type.

Example:

```json
{
  "userId": 1,
  "type": "ORDER_CREATED",
  "title": "Order created",
  "message": "Your order ORD-20260520-0001 has been created.",
  "read": false,
  "createdAt": "2026-05-20T10:00:00"
}
```

MongoDB is suitable because notification data is document-based and does not require complex joins.

---

## 22. Docker Architecture

The project should be containerized with Docker.

### First Docker Version

```text
Spring Boot Backend
MySQL
```

### Later Docker Compose Version

```text
orderhub-api
mysql
redis
rabbitmq
mongodb
```

### Suggested docker-compose Services

```text
backend
mysql
redis
rabbitmq
mongodb
```

### Docker Compose Flow

```text
docker compose up -d
        |
        v
Start MySQL
        |
        v
Start Redis
        |
        v
Start RabbitMQ
        |
        v
Start MongoDB
        |
        v
Start Spring Boot backend
```

---

## 23. CI/CD Architecture

The project should use GitHub Actions for CI.

### Initial CI Pipeline

```text
Push / Pull Request to develop or main
        |
        v
Checkout code
        |
        v
Set up JDK 21
        |
        v
Run unit tests
        |
        v
Build project
```

### Future CD Pipeline

```text
Push to main
        |
        v
Run tests
        |
        v
Build Docker image
        |
        v
Push image to Docker Hub
        |
        v
SSH to VPS
        |
        v
Pull latest image
        |
        v
Restart container
```

---

## 24. Testing Architecture

The project should include automated tests.

### Test Types

| Test Type | Purpose |
|---|---|
| Unit Test | Test service logic |
| Integration Test | Test database and API integration |
| Security Test | Test protected APIs |
| Repository Test | Test query methods |

### Tools

```text
JUnit 5
Mockito
Spring Boot Test
Testcontainers
```

### Priority Test Cases

```text
Register with existing email should fail.
Login with invalid password should fail.
Create order with empty cart should fail.
Create order with insufficient stock should fail.
Create order successfully should decrease stock.
User cannot access another user's order.
Payment success should update order status to PAID.
```

---

## 25. Logging Architecture

The application should use structured logging.

### Logging Goals

- Track important business actions.
- Debug errors.
- Monitor order and payment flows.
- Avoid logging sensitive data.

### Suggested Logs

```text
User registered
User logged in
Product created
Order created
Payment processed
Order status updated
Exception occurred
```

### Sensitive Data Rule

Do not log:

```text
Password
JWT token
Full credit card information
Secret keys
```

---

## 26. Validation Architecture

The system should validate input using Bean Validation.

### Common Annotations

```java
@NotBlank
@NotNull
@Email
@Min
@Max
@Size
```

### Example

```java
public class RegisterRequest {

    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;
}
```

### Rule

Input validation should happen before business logic is executed.

---

## 27. API Documentation Architecture

The system should use Swagger/OpenAPI for API documentation.

### Tool

```text
springdoc-openapi
```

### Swagger URL

```text
http://localhost:8080/swagger-ui.html
```

or:

```text
http://localhost:8080/swagger-ui/index.html
```

### Purpose

Swagger helps to:

- Test APIs quickly.
- Show API documentation to interviewers.
- Make the project easier to understand.
- Replace manual Postman testing for simple cases.

---

## 28. Future Microservice Architecture

After the modular monolith version is stable, the system can be split into microservices.

### Possible Microservices

```text
api-gateway
auth-service
product-service
cart-service
order-service
payment-service
notification-service
```

### Database Per Service

| Service | Database |
|---|---|
| auth-service | MySQL |
| product-service | MySQL |
| cart-service | MySQL |
| order-service | MySQL |
| payment-service | MySQL |
| notification-service | MongoDB |

### Microservice Communication

```text
Synchronous communication: REST API
Asynchronous communication: RabbitMQ or Kafka
```

### Example Event-Driven Flow

```text
Order Service creates order
        |
        v
RabbitMQ publishes ORDER_CREATED event
        |
        v
Notification Service consumes event
        |
        v
MongoDB stores notification
```

---

## 29. Implementation Order

The implementation should follow this order:

```text
1. Setup Spring Boot project
2. Configure MySQL connection
3. Create common response and exception handling
4. Implement Auth module
5. Implement Product module
6. Implement Cart module
7. Implement Order module
8. Implement Payment module
9. Add Swagger documentation
10. Add Docker and Docker Compose
11. Add unit tests
12. Add Redis cache
13. Add RabbitMQ event flow
14. Add MongoDB notifications
15. Add GitHub Actions CI
16. Deploy to server
```

### Reason For This Order

- Auth is needed before protected APIs.
- Product is needed before cart.
- Cart is needed before order.
- Order is needed before payment.
- Payment is needed before notification.
- Docker and CI/CD should be added after the main app can run.
- Redis, RabbitMQ and MongoDB should be added after the main business flow is stable.

---

## 30. Coding Rules

### General Rules

```text
Do not put business logic in controllers.
Do not return entities directly from APIs.
Do not store plain text passwords.
Do not commit secrets to GitHub.
Use DTOs for requests and responses.
Use services for business logic.
Use repositories only for database access.
Use transactions for order creation.
Use consistent response format.
Use meaningful exception messages.
```

### Naming Rules

Classes:

```text
User
Product
OrderService
ProductController
OrderRepository
CreateOrderRequest
OrderResponse
```

Methods:

```text
createOrder()
getProductById()
addItemToCart()
processFakePayment()
```

Branches:

```text
feat/auth-jwt
feat/product-api
feat/cart-api
feat/order-flow
feat/payment-flow
chore/docker-compose
ci/github-actions
docs/architecture-design
```

Commits:

```text
[FEAT]: Implement JWT authentication
[FEAT]: Add product APIs
[FIX]: Validate stock before creating order
[TEST]: Add unit tests for order service
[CHORE]: Add Docker Compose configuration
[CI]: Add GitHub Actions workflow
[DOCS]: Add backend architecture document
```

---

## 31. Architecture Decision Records

### Decision 1: Use Modular Monolith First

The system starts as a modular monolith to reduce complexity and help development progress faster.

### Decision 2: Use MySQL For Core Data

MySQL is used for core business data because the data is relational and requires transaction support.

### Decision 3: Use Redis For Cache

Redis is planned for product caching because product APIs are read frequently.

### Decision 4: Use RabbitMQ For Events

RabbitMQ is planned for order and payment events because it is easier to learn and suitable for asynchronous communication.

### Decision 5: Use MongoDB For Notifications

MongoDB is planned for notifications because notification data is flexible and document-based.

---

## 32. Conclusion

This architecture design defines how OrderHub Backend System should be structured and implemented.

The first version uses a modular monolithic architecture with Spring Boot and MySQL. This approach keeps the project simple enough to build while still being clean, maintainable and suitable for future microservice migration.

The architecture supports:

- RESTful API design.
- Clean layered structure.
- JWT authentication.
- Role-based authorization.
- MySQL transaction handling.
- Redis caching in later phases.
- RabbitMQ event-driven communication in later phases.
- MongoDB notification storage in later phases.
- Docker-based deployment.
- GitHub Actions CI/CD.

This document should be used as the main technical guide before implementing the Spring Boot backend source code.