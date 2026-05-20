# 01. Project Requirements - OrderHub Backend System

## 1. Project Overview

OrderHub Backend System is a backend application that simulates an e-commerce order management system. The project focuses on RESTful API design, relational database design, authentication, order transaction handling, caching, event-driven communication, containerization, testing and CI/CD.

## 2. Main Actors

- Guest
- User
- Admin
- System

## 3. User Features

- Register account
- Login with email and password
- View product list
- View product detail
- Add product to cart
- Update cart item quantity
- Remove item from cart
- Create order
- Make fake payment
- View order history
- View order detail
- Receive order notifications

## 4. Admin Features

- Manage categories
- Manage products
- Manage product variants
- Manage inventory
- View orders
- Update order status

## 5. System Features

- JWT authentication
- Role-based authorization
- MySQL database
- Redis cache for product APIs
- RabbitMQ event for order/payment events
- MongoDB for notifications
- Docker Compose deployment
- GitHub Actions CI pipeline
- Unit testing

## 6. Project Goal

The goal of this project is to build a production-like backend system suitable for a Backend Engineer Intern portfolio.