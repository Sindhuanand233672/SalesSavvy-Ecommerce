# SalesSavvy — Full Stack E-Commerce Platform

<p align="center">

  <strong>A modern e-commerce application built with Spring Boot, React, and MySQL.</strong>

  <br/>

  Discover products, shop smarter, manage orders, and enjoy a seamless online shopping experience.

</p>

<p align="center">

  <a href="https://salessavvy-ecommerce-1.onrender.com">Live Demo</a> •
  <a href="https://github.com/Sindhuanand233672/SalesSavvy-Ecommerce">GitHub Repository</a>

</p>

---

## Overview

**SalesSavvy** is a full-stack e-commerce web application designed to deliver a seamless online shopping experience for customers while providing administrators with comprehensive tools to manage products, orders, customers, promotions, and reviews.

The platform features a React-based user interface, a RESTful backend developed using Spring Boot, and a MySQL-compatible cloud database. It incorporates JWT authentication, role-based access control, shopping cart management, wishlist functionality, coupon-based discounts, checkout, online payments, order tracking, and customer reviews.

The project demonstrates full-stack development, backend API design, database integration, authentication, payment integration, and deployment of a web application.

---

## Live Application

| Platform | Link |
|---|---|
| Live Website | https://salessavvy-ecommerce-1.onrender.com |
| GitHub Repository | https://github.com/Sindhuanand233672/SalesSavvy-Ecommerce |

---

## Key Features

### Customer Features

- User registration and login
- JWT-based authentication
- Browse and search products
- Product details and stock availability
- Category-based product browsing
- Deals and special offers
- Shopping cart management
- Wishlist management
- Quantity management
- Address management
- Coupon application and discount calculation
- Checkout and order placement
- Cash on Delivery (COD)
- Razorpay payment integration
- Order confirmation
- Order history
- Order details
- Order and delivery tracking
- Product reviews and ratings
- Personal review management
- Responsive user interface
- Dark mode support

### Admin Features

- Admin authentication and protected access
- Admin dashboard
- Product management
- Category management
- Customer management
- Order management
- Order status updates
- Review moderation and approval
- Coupon management
- Deals management
- Inventory and stock management

---

## Technology Stack

### Frontend

| Technology | Purpose |
|---|---|
| React.js | UI development |
| JavaScript | Application logic |
| HTML5 | Web structure |
| CSS3 | Styling and responsive UI |
| Vite | Frontend development and build tool |
| React Router | Client-side routing |
| Fetch API | Backend communication |

### Backend

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming language |
| Spring Boot | Backend application framework |
| Spring Web | REST API development |
| Spring Security | Application security |
| JWT | Authentication and authorization |
| Spring Data JPA | Database persistence |
| Hibernate | ORM |
| Maven | Dependency management and build |
| REST APIs | Frontend-backend communication |

### Database

| Technology | Purpose |
|---|---|
| MySQL | Relational database |
| TiDB Cloud | Cloud-hosted MySQL-compatible database |
| JPA/Hibernate | Entity mapping and persistence |

### Tools & Platforms

- Git
- GitHub
- Postman
- Swagger / OpenAPI
- Eclipse IDE
- Visual Studio Code
- Render
- TiDB Cloud

---

## Application Architecture

SalesSavvy follows a frontend-backend architecture where the React application communicates with the Spring Boot backend through REST APIs.

```text
                    ┌──────────────────────┐
                    │       Customer      │
                    │     / Admin User    │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │   React Frontend     │
                    │   JavaScript + Vite │
                    └──────────┬──────────┘
                               │
                         REST API Requests
                               │
                               ▼
                    ┌──────────────────────┐
                    │ Spring Boot Backend  │
                    │    REST APIs        │
                    └──────────┬──────────┘
                               │
                ┌──────────────┼──────────────┐
                │              │              │
                ▼              ▼              ▼
          ┌──────────┐  ┌────────────┐  ┌───────────┐
          │ Security │  │ Services   │  │   JPA    │
          │ JWT     │  │ Business    │  │ Hibernate│
          │ RBAC    │  │ Logic       │  │          │
          └──────────┘  └────────────┘  └─────┬─────┘
                                               │
                                               ▼
                                      ┌────────────────┐
                                      │ MySQL / TiDB   │
                                      │ Cloud Database  │
                                      └────────────────┘
```

---

## Authentication & Security

SalesSavvy implements security features to protect application resources and manage access.

- JWT-based authentication
- Secure user login and registration
- Role-based access control
- Protected customer endpoints
- Restricted admin endpoints
- Password hashing
- Authentication-based API access
- Environment-based configuration for sensitive credentials

The application separates customer and administrator permissions to prevent unauthorized access to administrative operations.

---

## Payment Integration

SalesSavvy supports multiple payment methods to provide flexibility during checkout.

### Cash on Delivery (COD)

Customers can place orders with Cash on Delivery. Payment remains pending until the payment is collected during delivery.

### Razorpay

Razorpay integration enables online payment processing through the checkout workflow.

Payment-related functionality includes:

- Online payment initiation
- Payment status tracking
- Payment and order association
- Payment status display
- Order confirmation workflow

> Payment credentials and sensitive configuration values are maintained separately from the source code.

---

## Order Management & Delivery Tracking

The application supports the order lifecycle from checkout to delivery.

### Order Lifecycle

```text
Cart
  │
  ▼
Checkout
  │
  ▼
Order Placed
  │
  ▼
Order Confirmed
  │
  ▼
Processing
  │
  ▼
Out for Delivery
  │
  ▼
Delivered
```

Customers can view their order history, inspect individual order details, and track order progress.

Administrators can manage orders and update their status through the admin interface.

---

## Customer Reviews & Ratings

SalesSavvy includes a product review system that allows customers to share feedback about purchased products.

Features include:

- Star-based ratings
- Written product reviews
- Customer feedback display
- Product rating aggregation
- Review moderation
- Admin approval workflow
- Customer review management

Reviews are moderated before being displayed publicly.

---

## Project Modules

| Module | Description |
|---|---|
| Authentication | Registration, login, JWT authentication |
| User Management | Customer profiles and admin access |
| Product Management | Product listing, details, inventory |
| Category Management | Product categorization |
| Cart Management | Add, update, remove and clear cart items |
| Wishlist | Save products for later |
| Deals | Promotional products and special offers |
| Coupons | Discount codes and coupon validation |
| Checkout | Address selection and checkout |
| Orders | Order placement and management |
| Payments | COD and Razorpay integration |
| Delivery Tracking | Order progress and tracking |
| Reviews | Ratings, feedback and moderation |
| Admin Dashboard | Administrative management tools |

---

## Screenshots

### Homepage

The homepage provides a clean shopping interface featuring product discovery, promotional content, navigation, and shopping shortcuts.

### Product Details

Displays product information, pricing, stock availability, quantity selection, cart actions, wishlist actions, and customer reviews.

### Order Confirmation

Displays order confirmation details, payment information, coupon information, and order navigation.

### Admin Order Management

Provides an administrative view of orders, payment details, order statuses, and management actions.

---

## Getting Started

Follow these steps to run SalesSavvy locally.

### Prerequisites

Install the following:

- Java 21
- Maven
- Node.js and npm
- MySQL or a compatible database
- Git

---

### 1. Clone the Repository

```bash
git clone https://github.com/Sindhuanand233672/SalesSavvy-Ecommerce.git
```

Navigate to the project directory:

```bash
cd SalesSavvy-Ecommerce
```

---

### 2. Configure the Backend

Navigate to the backend directory:

```bash
cd sales-savvy
```

Configure your database credentials and application environment variables.

Example configuration:

```properties
DB_URL=jdbc:mysql://localhost:3306/sales_savvy_db
DB_USERNAME=root
DB_PASSWORD=your_database_password

JWT_SECRET=your_secure_jwt_secret
JWT_EXPIRATION_MS=86400000

SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

Make sure your database is running and the database configuration is correct.

---

### 3. Run the Backend

From the backend directory:

```bash
mvn spring-boot:run
```

The backend will run at:

```text
http://localhost:8080
```

---

### 4. Configure the Frontend

Open another terminal and navigate to the frontend directory:

```bash
cd salessavvy-frontend
```

Install dependencies:

```bash
npm install
```

Create a `.env` file in the frontend directory:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

---

### 5. Run the Frontend

Start the React development server:

```bash
npm run dev
```

The frontend will be available at the local URL provided by Vite, typically:

```text
http://localhost:5173
```

---

## API Documentation

SalesSavvy backend APIs are designed using REST principles.

Swagger/OpenAPI documentation is available when the backend is running and Swagger has been configured.

### API Documentation

```text
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI Specification

```text
http://localhost:8080/v3/api-docs
```

### API Categories

- Authentication APIs
- Product APIs
- Category APIs
- Cart APIs
- Wishlist APIs
- Address APIs
- Coupon APIs
- Checkout APIs
- Order APIs
- Order tracking APIs
- Payment APIs
- Review APIs
- Admin APIs

---

## Environment Variables

Sensitive information should not be committed to GitHub.

### Backend

| Variable | Description |
|---|---|
| `DB_URL` | Database connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | JWT signing secret |
| `JWT_EXPIRATION_MS` | JWT expiration duration |
| `SERVER_PORT` | Backend server port |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins |
| `RAZORPAY_KEY_ID` | Razorpay public/key ID |
| `RAZORPAY_KEY_SECRET` | Razorpay secret key |

### Frontend

| Variable | Description |
|---|---|
| `VITE_API_BASE_URL` | Backend API base URL |

Create your own environment files locally and configure environment variables on the deployment platform.

Do not commit `.env` files, passwords, secret keys, private tokens, or production credentials.

---

## Deployment

SalesSavvy is deployed online using cloud hosting services.

| Component | Hosting |
|---|---|
| Frontend | Render |
| Backend | Render |
| Database | TiDB Cloud |

Deployment requires the appropriate environment variables, database connectivity, CORS configuration, and frontend API configuration.

### Deployment Configuration

- Cloud-hosted frontend
- Cloud-hosted Spring Boot backend
- Cloud database connectivity
- Environment-based secrets
- Production API communication
- CORS configuration for deployed frontend

---

## Future Enhancements

Potential future improvements include:

- Automated unit and integration testing
- CI/CD pipeline using GitHub Actions
- Email notifications for order updates
- Advanced analytics dashboard
- Improved search and filtering
- Product recommendations
- Performance optimization
- Advanced inventory analytics
- Enhanced monitoring and logging

---

## Learning Outcomes

Building SalesSavvy provided hands-on experience with:

- Full-stack application development
- Java and Spring Boot
- REST API development
- React application development
- JWT authentication
- Role-based authorization
- Database design and integration
- JPA and Hibernate
- Payment integration
- E-commerce business workflows
- API testing using Postman
- API documentation using Swagger
- Git and GitHub workflows
- Cloud deployment
- Environment-based configuration
- Debugging and integration of frontend and backend systems

---

## Author

**Sindhu Anand**

Aspiring Software Developer | Java | Spring Boot | React | SQL

GitHub: [Sindhuanand233672](https://github.com/Sindhuanand233672)

---

<p align="center">

  <strong>SalesSavvy — Discover more. Shop smarter. Live better.</strong>

</p>
