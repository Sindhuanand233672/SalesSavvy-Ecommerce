# SalesSavvy Frontend — Updated Complete Package

This package contains the React + Vite frontend source, existing customer/admin pages, Product Reviews integration, Admin Reviews page, theme fixes, and responsive product-detail styling corrections.

## Before replacing your current frontend

1. Stop the frontend dev server.
2. Make a backup copy of your existing `frontend` folder.
3. Extract this ZIP. The folder in the ZIP is the frontend project root (the folder containing `package.json`).
4. Copy the extracted project files into your existing frontend folder, allowing the updated files to replace the old ones. Do not copy a `node_modules` folder; this ZIP intentionally excludes it.
5. Keep your own `.env` file if you already have one. Do not overwrite it with `.env.example`.

## Configure the backend URL

Create or update `.env` in the frontend project root:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

The API client already attaches the JWT stored under `localStorage` key `token` to authenticated requests.

## Install and run

Open a terminal in the frontend project root:

```bash
npm install
npm run dev
```

For a production build:

```bash
npm run build
```

## Product Reviews workflow

- Customer submits a review from `/products/:id`.
- Backend accepts reviews only from customers who have a delivered order containing that product.
- A review is pending until an admin approves it.
- Only approved reviews appear publicly on the product detail page and contribute to its average rating.
- Admin review moderation is available at `/admin/reviews` for an authenticated admin account.

## Notes

- This is a frontend-only package. It does not replace your Spring Boot backend or database.
- Do not change database settings or run schema changes for this frontend update.
- The archive does not include `node_modules`; `npm install` recreates dependencies on your machine.
- The build could not be verified in this packaging environment because the uploaded dependency tree is missing the platform-specific Rolldown native binding. Run `npm install` and `npm run build` locally; if Vite reports an error, share the complete terminal output.
