# 03. API Design - OrderHub Backend System

## 1. API Design Overview

This document defines the REST API specification for OrderHub Backend System.

The API is designed for a backend e-commerce order management system. It supports authentication, product management, cart management, order creation, fake payment, notification and admin operations.

The API follows RESTful principles and uses JSON as the main data format.

---

## 2. Base URL

For local development:

```text
http://localhost:8080/api/v1
```

For production:

```text
https://api.orderhub.example.com/api/v1
```

---

## 3. Common API Response Format

All APIs should return a consistent JSON response format.

### 3.1. Success Response

```json
{
  "success": true,
  "message": "Request processed successfully",
  "data": {}
}
```

### 3.2. Error Response

```json
{
  "success": false,
  "message": "Error message",
  "errors": []
}
```

### 3.3. Pagination Response

```json
{
  "success": true,
  "message": "Data retrieved successfully",
  "data": {
    "items": [],
    "page": 0,
    "size": 10,
    "totalItems": 100,
    "totalPages": 10
  }
}
```

---

## 4. Authentication

Protected APIs require a JWT token in the request header.

```http
Authorization: Bearer <access_token>
```

---

## 5. HTTP Status Codes

| Status Code | Meaning |
|---|---|
| 200 | OK |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 500 | Internal Server Error |

---

## 6. Auth APIs

---

## 6.1. Register

### Endpoint

```http
POST /auth/register
```

### Description

Create a new user account.

### Authentication

Not required.

### Request Body

```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "Password@123",
  "phone": "0909123456"
}
```

### Success Response

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "fullName": "John Doe",
    "email": "john@example.com",
    "phone": "0909123456",
    "status": "ACTIVE"
  }
}
```

### Error Cases

| Status Code | Reason |
|---|---|
| 400 | Invalid input |
| 409 | Email already exists |

---

## 6.2. Login

### Endpoint

```http
POST /auth/login
```

### Description

Authenticate user and return JWT access token.

### Authentication

Not required.

### Request Body

```json
{
  "email": "john@example.com",
  "password": "Password@123"
}
```

### Success Response

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "jwt_token_here",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "fullName": "John Doe",
      "email": "john@example.com",
      "roles": ["ROLE_USER"]
    }
  }
}
```

### Error Cases

| Status Code | Reason |
|---|---|
| 400 | Invalid input |
| 401 | Invalid email or password |
| 403 | Account is locked or inactive |

---

## 6.3. Get Current User

### Endpoint

```http
GET /auth/me
```

### Description

Get current authenticated user information.

### Authentication

Required.

### Success Response

```json
{
  "success": true,
  "message": "Current user retrieved successfully",
  "data": {
    "id": 1,
    "fullName": "John Doe",
    "email": "john@example.com",
    "phone": "0909123456",
    "roles": ["ROLE_USER"],
    "status": "ACTIVE"
  }
}
```

---

## 7. Product APIs

---

## 7.1. Get Product List

### Endpoint

```http
GET /products
```

### Description

Get a paginated list of active products.

### Authentication

Not required.

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| page | int | No | Page number |
| size | int | No | Page size |
| keyword | string | No | Search keyword |
| categoryId | long | No | Filter by category |
| status | string | No | Filter by product status |

### Example Request

```http
GET /products?page=0&size=10&keyword=shirt&categoryId=1
```

### Success Response

```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": {
    "items": [
      {
        "id": 1,
        "name": "Basic T-Shirt",
        "price": 199000,
        "thumbnailUrl": "https://example.com/tshirt.jpg",
        "status": "ACTIVE",
        "category": {
          "id": 1,
          "name": "T-Shirts"
        }
      }
    ],
    "page": 0,
    "size": 10,
    "totalItems": 1,
    "totalPages": 1
  }
}
```

---

## 7.2. Get Product Detail

### Endpoint

```http
GET /products/{id}
```

### Description

Get product detail with variants.

### Authentication

Not required.

### Success Response

```json
{
  "success": true,
  "message": "Product retrieved successfully",
  "data": {
    "id": 1,
    "name": "Basic T-Shirt",
    "description": "A basic cotton T-shirt",
    "price": 199000,
    "thumbnailUrl": "https://example.com/tshirt.jpg",
    "status": "ACTIVE",
    "category": {
      "id": 1,
      "name": "T-Shirts"
    },
    "variants": [
      {
        "id": 1,
        "sku": "TSHIRT-WHITE-M",
        "color": "White",
        "size": "M",
        "stockQuantity": 20,
        "price": 199000,
        "status": "ACTIVE"
      }
    ]
  }
}
```

### Error Cases

| Status Code | Reason |
|---|---|
| 404 | Product not found |

---

## 8. Admin Product APIs

Admin APIs require `ROLE_ADMIN`.

---

## 8.1. Create Product

### Endpoint

```http
POST /admin/products
```

### Description

Create a new product.

### Authentication

Required. Admin only.

### Request Body

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

### Success Response

```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": 1,
    "name": "Basic T-Shirt"
  }
}
```

---

## 8.2. Update Product

### Endpoint

```http
PUT /admin/products/{id}
```

### Description

Update product information.

### Authentication

Required. Admin only.

### Request Body

```json
{
  "categoryId": 1,
  "name": "Updated Basic T-Shirt",
  "description": "Updated product description",
  "price": 209000,
  "thumbnailUrl": "https://example.com/updated-tshirt.jpg",
  "status": "ACTIVE"
}
```

---

## 8.3. Delete Product

### Endpoint

```http
DELETE /admin/products/{id}
```

### Description

Soft delete or deactivate a product.

### Authentication

Required. Admin only.

### Success Response

```json
{
  "success": true,
  "message": "Product deleted successfully",
  "data": null
}
```

---

## 9. Cart APIs

Cart APIs require authenticated user.

---

## 9.1. Get Current User Cart

### Endpoint

```http
GET /cart
```

### Description

Get current user's cart.

### Authentication

Required.

### Success Response

```json
{
  "success": true,
  "message": "Cart retrieved successfully",
  "data": {
    "id": 1,
    "items": [
      {
        "id": 1,
        "productVariantId": 1,
        "productName": "Basic T-Shirt",
        "variantInfo": "White / M",
        "quantity": 2,
        "unitPrice": 199000,
        "totalPrice": 398000
      }
    ],
    "totalAmount": 398000
  }
}
```

---

## 9.2. Add Item To Cart

### Endpoint

```http
POST /cart/items
```

### Description

Add a product variant to the current user's cart.

### Authentication

Required.

### Request Body

```json
{
  "productVariantId": 1,
  "quantity": 2
}
```

### Success Response

```json
{
  "success": true,
  "message": "Item added to cart successfully",
  "data": {
    "cartItemId": 1
  }
}
```

### Error Cases

| Status Code | Reason |
|---|---|
| 400 | Invalid quantity |
| 404 | Product variant not found |

---

## 9.3. Update Cart Item Quantity

### Endpoint

```http
PUT /cart/items/{id}
```

### Description

Update quantity of a cart item.

### Authentication

Required.

### Request Body

```json
{
  "quantity": 3
}
```

---

## 9.4. Remove Cart Item

### Endpoint

```http
DELETE /cart/items/{id}
```

### Description

Remove an item from cart.

### Authentication

Required.

### Success Response

```json
{
  "success": true,
  "message": "Cart item removed successfully",
  "data": null
}
```

---

## 10. Order APIs

Order APIs require authenticated user.

---

## 10.1. Create Order

### Endpoint

```http
POST /orders
```

### Description

Create an order from the current user's cart.

### Authentication

Required.

### Request Body

```json
{
  "note": "Please deliver after 6 PM"
}
```

### Business Flow

```text
1. Get current user's cart.
2. Check if cart is empty.
3. Check stock quantity of each product variant.
4. Create order with status PENDING.
5. Create order items.
6. Decrease product variant stock.
7. Clear cart items.
8. Return order response.
```

### Success Response

```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "orderCode": "ORD-20260520-0001",
    "totalAmount": 398000,
    "status": "PENDING",
    "createdAt": "2026-05-20T10:00:00"
  }
}
```

### Error Cases

| Status Code | Reason |
|---|---|
| 400 | Cart is empty |
| 400 | Not enough stock |
| 401 | User is not authenticated |

---

## 10.2. Get My Orders

### Endpoint

```http
GET /orders
```

### Description

Get current user's order history.

### Authentication

Required.

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| page | int | No | Page number |
| size | int | No | Page size |
| status | string | No | Filter by order status |

### Success Response

```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": {
    "items": [
      {
        "id": 1,
        "orderCode": "ORD-20260520-0001",
        "totalAmount": 398000,
        "status": "PENDING",
        "createdAt": "2026-05-20T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalItems": 1,
    "totalPages": 1
  }
}
```

---

## 10.3. Get Order Detail

### Endpoint

```http
GET /orders/{id}
```

### Description

Get detail of one order.

### Authentication

Required.

### Success Response

```json
{
  "success": true,
  "message": "Order retrieved successfully",
  "data": {
    "id": 1,
    "orderCode": "ORD-20260520-0001",
    "totalAmount": 398000,
    "status": "PENDING",
    "items": [
      {
        "productName": "Basic T-Shirt",
        "variantInfo": "White / M",
        "quantity": 2,
        "unitPrice": 199000,
        "totalPrice": 398000
      }
    ],
    "createdAt": "2026-05-20T10:00:00"
  }
}
```

### Error Cases

| Status Code | Reason |
|---|---|
| 403 | User cannot access another user's order |
| 404 | Order not found |

---

## 11. Admin Order APIs

Admin order APIs require `ROLE_ADMIN`.

---

## 11.1. Get All Orders

### Endpoint

```http
GET /admin/orders
```

### Description

Get all orders in the system.

### Authentication

Required. Admin only.

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| page | int | No | Page number |
| size | int | No | Page size |
| status | string | No | Filter by order status |
| keyword | string | No | Search by order code or user email |

---

## 11.2. Update Order Status

### Endpoint

```http
PUT /admin/orders/{id}/status
```

### Description

Update order status.

### Authentication

Required. Admin only.

### Request Body

```json
{
  "status": "CONFIRMED"
}
```

### Success Response

```json
{
  "success": true,
  "message": "Order status updated successfully",
  "data": {
    "id": 1,
    "orderCode": "ORD-20260520-0001",
    "status": "CONFIRMED"
  }
}
```

---

## 12. Payment APIs

Payment APIs require authenticated user.

---

## 12.1. Fake Payment

### Endpoint

```http
POST /payments/fake-pay
```

### Description

Simulate payment for an order.

### Authentication

Required.

### Request Body

```json
{
  "orderId": 1,
  "paymentMethod": "FAKE_VNPAY",
  "success": true
}
```

### Business Flow

```text
1. Check whether order exists.
2. Check whether order belongs to current user.
3. Check whether order is payable.
4. Create or update payment record.
5. If payment success, update order status to PAID.
6. Publish PAYMENT_SUCCESS event in a later phase.
7. Return payment response.
```

### Success Response

```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "paymentId": 1,
    "orderId": 1,
    "amount": 398000,
    "paymentMethod": "FAKE_VNPAY",
    "status": "SUCCESS",
    "transactionCode": "PAY-20260520-0001",
    "paidAt": "2026-05-20T10:10:00"
  }
}
```

### Error Cases

| Status Code | Reason |
|---|---|
| 400 | Order is not payable |
| 403 | User cannot pay another user's order |
| 404 | Order not found |

---

## 12.2. Get Payment By Order

### Endpoint

```http
GET /payments/orders/{orderId}
```

### Description

Get payment information by order ID.

### Authentication

Required.

### Success Response

```json
{
  "success": true,
  "message": "Payment retrieved successfully",
  "data": {
    "paymentId": 1,
    "orderId": 1,
    "amount": 398000,
    "paymentMethod": "FAKE_VNPAY",
    "status": "SUCCESS",
    "transactionCode": "PAY-20260520-0001",
    "paidAt": "2026-05-20T10:10:00"
  }
}
```

---

## 13. Notification APIs

Notification APIs require authenticated user.

In the first monolithic version, notifications can be stored in MySQL or simulated. In a later phase, notifications will be stored in MongoDB.

---

## 13.1. Get My Notifications

### Endpoint

```http
GET /notifications
```

### Description

Get current user's notifications.

### Authentication

Required.

### Success Response

```json
{
  "success": true,
  "message": "Notifications retrieved successfully",
  "data": {
    "items": [
      {
        "id": "notif_001",
        "type": "ORDER_CREATED",
        "title": "Order created",
        "message": "Your order ORD-20260520-0001 has been created.",
        "read": false,
        "createdAt": "2026-05-20T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalItems": 1,
    "totalPages": 1
  }
}
```

---

## 13.2. Mark Notification As Read

### Endpoint

```http
PUT /notifications/{id}/read
```

### Description

Mark one notification as read.

### Authentication

Required.

### Success Response

```json
{
  "success": true,
  "message": "Notification marked as read",
  "data": null
}
```

---

## 14. API Security Rules

| API Group | Authentication | Role |
|---|---|---|
| Auth register/login | No | Public |
| Product list/detail | No | Public |
| Cart APIs | Yes | ROLE_USER |
| Order APIs | Yes | ROLE_USER |
| Payment APIs | Yes | ROLE_USER |
| Notification APIs | Yes | ROLE_USER |
| Admin product APIs | Yes | ROLE_ADMIN |
| Admin order APIs | Yes | ROLE_ADMIN |

---

## 15. API Implementation Priority

The APIs should be implemented in this order:

```text
1. Auth APIs
2. Product APIs
3. Cart APIs
4. Order APIs
5. Payment APIs
6. Notification APIs
7. Admin APIs
```

Reason:

- Auth must be implemented first because protected APIs need JWT.
- Product APIs are needed before cart.
- Cart is needed before order.
- Order is needed before payment.
- Notification can be added after order and payment events are available.

---

## 16. Future API Improvements

In later phases, the API can be improved with:

- Refresh token API.
- Logout with Redis token blacklist.
- Real payment gateway integration.
- API rate limiting.
- API versioning strategy.
- Advanced product filtering.
- Order cancellation API.
- Shipping tracking API.
- OpenAPI/Swagger documentation.
- Microservice API Gateway.

---

## 17. Conclusion

This API design defines the main REST endpoints for the first version of OrderHub Backend System.

It provides a clear implementation direction for:

- Authentication and authorization.
- Product management.
- Cart management.
- Order creation.
- Fake payment.
- Notification.
- Admin operations.

The implementation should follow this document to keep the backend consistent, maintainable and easy to demonstrate during internship interviews.