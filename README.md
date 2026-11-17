# SalesSavvy — Full-Stack E-commerce Application

SalesSavvy is a full-stack e-commerce application built with React, Vite, Spring Boot, Java 21, Spring Security, JWT, Spring Data JPA and MySQL.

## Project structure

- `frontend/` — React + Vite customer storefront and admin interface
- `backend/` — Spring Boot REST API
- `backend/Dockerfile` — container build for deployment

## Run locally

### Backend

1. Create a MySQL database named `sales_savvy_db`.
2. Set the environment variables below in your IDE run configuration or shell.
3. Run the Spring Boot application from `backend/`.

Required backend environment variables:

- `DB_URL=jdbc:mysql://localhost:3306/sales_savvy_db`
- `DB_USERNAME=your_mysql_username`
- `DB_PASSWORD=your_mysql_password`
- `JWT_SECRET=a_long_random_secret_at_least_32_bytes`
- `CORS_ALLOWED_ORIGINS=http://localhost:5173`
- `SERVER_PORT=8080`

`JWT_SECRET` must be a strong random secret. Never commit real secrets or `.env` files.

### Frontend

1. Open `frontend/`.
2. Copy `.env.example` to `.env`.
3. Set `VITE_API_BASE_URL=http://localhost:8080/api`.
4. Run `npm ci`, then `npm run dev`.

## Production deployment (Render)

This is a monorepo with separate frontend and backend services.

### 1. MySQL database

Create a MySQL database with a managed external provider (for example, Aiven or Railway). Render's built-in managed database offering is PostgreSQL, while this application currently uses MySQL.

Keep the database credentials private. Obtain the provider's JDBC connection details and set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` on the backend service.

### 2. Backend Web Service

- Root directory: `backend`
- Runtime: Docker
- Dockerfile: `./Dockerfile`
- Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `CORS_ALLOWED_ORIGINS`.
- `CORS_ALLOWED_ORIGINS` must contain the exact deployed frontend origin, for example `https://your-frontend-name.onrender.com`.
- Render supplies `PORT`; the application is configured to use it.

### 3. Frontend Static Site

- Root directory: `frontend`
- Build command: `npm ci && npm run build`
- Publish directory: `dist`
- Add environment variable `VITE_API_BASE_URL=https://your-backend-name.onrender.com/api`
- The `public/_redirects` file provides SPA route fallback.

### 4. After deployment

- Add the final frontend origin to backend `CORS_ALLOWED_ORIGINS`.
- Redeploy the backend after changing CORS.
- Test registration/login, product browsing, cart, checkout, simulated payment, order history, admin operations, coupons and cancellation.
- Confirm no test credentials or secrets are committed to GitHub.

## Important security notes

- Database credentials and JWT secret are environment variables, not source code.
- Demo password reset is disabled by default. Do not enable it in production.
- Payments in this project are simulated; they are not processed by a real payment gateway.
- Order cancellation marks successful simulated payments as `REFUNDED` in the database; it does not issue a real bank/card refund.
