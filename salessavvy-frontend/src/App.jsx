
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import OrderDetails from "./pages/OrderDetails";
import Wishlist from "./pages/Wishlist";
import Home from "./pages/Home";
import Orders from "./pages/Orders";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Cart from "./pages/Cart";
import Checkout from "./pages/Checkout";
import OrderConfirmation from "./pages/OrderConfirmation";
import Products from "./pages/Products";
import ProductDetails from "./pages/ProductDetails";
import AdminReviews from "./pages/AdminReviews";
import Categories from "./pages/Categories";
import Deals from "./pages/Deals";
import AdminDashboard from "./pages/AdminDashboard";
import AdminProducts from "./pages/AdminProducts";
import AdminOrders from "./pages/AdminOrders";
import AdminCategories from "./pages/AdminCategories";
import AdminCoupons from "./pages/AdminCoupons";
import AdminCustomers from "./pages/AdminCustomers";
import CustomerDetails from "./pages/CustomerDetails";

import { AuthProvider, useAuth } from "./context/AuthContext";

function ProtectedRoute({ children }) {
  const { isLoggedIn } = useAuth();

  return isLoggedIn ? children : <Navigate to="/login" replace />;
}

function CustomerRoute({ children }) {
  const { isLoggedIn, isAdmin } = useAuth();

  if (!isLoggedIn) {
    return <Navigate to="/login" replace />;
  }

  if (isAdmin) {
    return <Navigate to="/admin/dashboard" replace />;
  }

  return children;
}

function AdminRoute({ children }) {
  const { isLoggedIn, isAdmin } = useAuth();

  if (!isLoggedIn) {
    return <Navigate to="/login" replace />;
  }

  if (!isAdmin) {
    return <Navigate to="/" replace />;
  }

  return children;
}

function AppRoutes() {
  return (
    <Routes>
      {/* Public Pages */}
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/products" element={<Products />} />
      <Route path="/categories" element={<Categories />} />
      <Route path="/deals" element={<Deals />} />

      {/* Public Product Details */}
      <Route
        path="/products/:id"
        element={<ProductDetails />}
      />

      {/* Customer-only Pages */}
      <Route
        path="/cart"
        element={
          <CustomerRoute>
            <Cart />
          </CustomerRoute>
        }
      />

      <Route
        path="/orders"
        element={
          <CustomerRoute>
            <Orders />
          </CustomerRoute>
        }
      />

      <Route
        path="/orders/:orderId"
        element={
          <CustomerRoute>
            <OrderDetails />
          </CustomerRoute>
        }
      />

      <Route
        path="/wishlist"
        element={
          <CustomerRoute>
            <Wishlist />
          </CustomerRoute>
        }
      />

      <Route
        path="/checkout"
        element={
          <CustomerRoute>
            <Checkout />
          </CustomerRoute>
        }
      />

      <Route
        path="/order-confirmation"
        element={
          <CustomerRoute>
            <OrderConfirmation />
          </CustomerRoute>
        }
      />

      {/* Admin-only Pages */}
      <Route
        path="/admin"
        element={
          <AdminRoute>
            <AdminDashboard />
          </AdminRoute>
        }
      />

      <Route
        path="/admin/dashboard"
        element={
          <AdminRoute>
            <AdminDashboard />
          </AdminRoute>
        }
      />

      <Route
        path="/admin/reviews"
        element={
          <AdminRoute>
            <AdminReviews />
          </AdminRoute>
        }
      />

      <Route
        path="/admin/orders"
        element={
          <AdminRoute>
            <AdminOrders />
          </AdminRoute>
        }
      />

      <Route
        path="/admin/categories"
        element={
          <AdminRoute>
            <AdminCategories />
          </AdminRoute>
        }
      />

      <Route
        path="/admin/products"
        element={
          <AdminRoute>
            <AdminProducts />
          </AdminRoute>
        }
      />

      <Route
        path="/admin/coupons"
        element={
          <AdminRoute>
            <AdminCoupons />
          </AdminRoute>
        }
      />

      <Route
        path="/admin/customers"
        element={
          <AdminRoute>
            <AdminCustomers />
          </AdminRoute>
        }
      />

      <Route
        path="/admin/customers/:customerId"
        element={
          <AdminRoute>
            <CustomerDetails />
          </AdminRoute>
        }
      />

      {/* Invalid URL */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
