
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Package,
  Tags,
  TicketPercent,
  ClipboardList,
  RefreshCw,
  Users,
  ShoppingBag,
  IndianRupee,
  Clock,
  CheckCircle,
  AlertTriangle,
  TrendingUp
} from "lucide-react";

import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import api from "../services/api";

import "../styles/admin-dashboard.css";

function AdminDashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/admin/dashboard");
      setDashboard(response.data);
    } catch (err) {
      console.error("Dashboard loading error:", err);
      setError("Unable to load dashboard statistics. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const formatCurrency = (amount) =>
    Number(amount || 0).toLocaleString("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2
    });

  const formatDate = (date) =>
    date ? new Date(date).toLocaleDateString("en-IN") : "-";

  const quickActions = [
    {
      title: "Customer Management",
      description: "View and manage registered customers",
      path: "/admin/customers",
      icon: Users
    },
    {
      title: "Order Management",
      description: "View and manage customer orders",
      path: "/admin/orders",
      icon: ClipboardList
    },
    {
      title: "Product Management",
      description: "Add, update and manage products",
      path: "/admin/products",
      icon: Package
    },
    {
      title: "Category Management",
      description: "Organise your product categories",
      path: "/admin/categories",
      icon: Tags
    },
    {
      title: "Coupon Management",
      description: "Create and manage discount coupons",
      path: "/admin/coupons",
      icon: TicketPercent
    }
  ];

  const statistics = dashboard
    ? [
        {
          title: "Total Customers",
          value: dashboard.totalUsers,
          icon: Users,
          type: "number"
        },
        {
          title: "Total Products",
          value: dashboard.totalProducts,
          icon: Package,
          type: "number"
        },
        {
          title: "Total Orders",
          value: dashboard.totalOrders,
          icon: ShoppingBag,
          type: "number"
        },
        {
          title: "Total Revenue",
          value: formatCurrency(dashboard.totalRevenue),
          icon: IndianRupee,
          type: "currency"
        },
        {
          title: "Pending Orders",
          value: dashboard.pendingOrders,
          icon: Clock,
          type: "number"
        },
        {
          title: "Completed Orders",
          value: dashboard.completedOrders,
          icon: CheckCircle,
          type: "number"
        },
        {
          title: "Low Stock Products",
          value: dashboard.lowStockProducts,
          icon: AlertTriangle,
          type: "number"
        }
      ]
    : [];

  return (
    <>
      <Navbar />

      <main className="admin-dashboard">

        {/* Dashboard Header */}
        <div className="admin-header">
          <div>
            <span className="eyebrow">ADMINISTRATION</span>
            <h1>Dashboard Overview</h1>
            <p>
              Welcome to SalesSavvy. Monitor your store and manage
              your business from one place.
            </p>
          </div>

          <div className="admin-header-actions">
            <button
              type="button"
              onClick={fetchDashboard}
              disabled={loading}
            >
              <RefreshCw
                size={16}
                className={loading ? "refresh-spinning" : ""}
              />
              {loading ? "Refreshing..." : "Refresh Dashboard"}
            </button>
          </div>
        </div>

        {/* Quick Management Actions */}
        <section className="admin-quick-section">
          <div className="admin-section-heading">
            <div>
              <span className="eyebrow">STORE CONTROL</span>
              <h2>Quick Management</h2>
              <p>Access all your store management tools.</p>
            </div>
          </div>

          <div className="admin-quick-actions">
            {quickActions.map((action) => {
              const Icon = action.icon;

              return (
                <Link
                  to={action.path}
                  className="admin-quick-card"
                  key={action.path}
                >
                  <div className="admin-quick-icon">
                    <Icon size={23} />
                  </div>

                  <div className="admin-quick-info">
                    <strong>{action.title}</strong>
                    <small>{action.description}</small>
                  </div>

                  <span className="admin-quick-arrow">→</span>
                </Link>
              );
            })}
          </div>
        </section>

        {/* Loading */}
        {loading && (
          <div className="admin-loading">
            <RefreshCw className="refresh-spinning" size={22} />
            <p>Loading dashboard statistics...</p>
          </div>
        )}

        {/* Error */}
        {!loading && error && (
          <div className="admin-error">
            <AlertTriangle size={20} />
            <span>{error}</span>

            <button type="button" onClick={fetchDashboard}>
              Try Again
            </button>
          </div>
        )}

        {/* Dashboard Content */}
        {!loading && dashboard && !error && (
          <>
            {/* Statistics */}
            <section className="admin-overview-section">
              <div className="admin-section-heading">
                <div>
                  <span className="eyebrow">BUSINESS OVERVIEW</span>
                  <h2>Store Performance</h2>
                  <p>Important statistics about your store.</p>
                </div>
              </div>

              <div className="admin-stat-grid">
                {statistics.map((stat) => {
                  const Icon = stat.icon;

                  return (
                    <div className="admin-stat-card" key={stat.title}>
                      <div className="admin-stat-top">
                        <span>{stat.title}</span>
                        <div className="admin-stat-icon">
                          <Icon size={21} />
                        </div>
                      </div>

                      <h2>{stat.value ?? 0}</h2>
                    </div>
                  );
                })}
              </div>
            </section>

            {/* Recent Orders */}
            <section className="admin-data-section">
              <div className="admin-data-heading">
                <div>
                  <span className="eyebrow">LATEST ACTIVITY</span>
                  <h2>Recent Orders</h2>
                </div>

                <Link to="/admin/orders">
                  View All Orders →
                </Link>
              </div>

              {dashboard.recentOrders?.length > 0 ? (
                <div className="admin-table-wrapper">
                  <table className="admin-table">
                    <thead>
                      <tr>
                        <th>Order ID</th>
                        <th>Customer</th>
                        <th>Amount</th>
                        <th>Status</th>
                        <th>Date</th>
                      </tr>
                    </thead>

                    <tbody>
                      {dashboard.recentOrders.map((order) => (
                        <tr key={order.orderId}>
                          <td>#{order.orderId}</td>
                          <td>{order.customerName}</td>
                          <td>{formatCurrency(order.totalAmount)}</td>
                          <td>
                            <span
                              className={`admin-status admin-status-${String(
                                order.status || "unknown"
                              ).toLowerCase()}`}
                            >
                              {order.status}
                            </span>
                          </td>
                          <td>{formatDate(order.orderDate)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <p className="admin-empty">
                  No recent orders available.
                </p>
              )}
            </section>

            {/* Top Selling Products */}
            <section className="admin-data-section">
              <div className="admin-data-heading">
                <div>
                  <span className="eyebrow">PRODUCT PERFORMANCE</span>
                  <h2>Top Selling Products</h2>
                </div>

                <Link to="/admin/products">
                  Manage Products →
                </Link>
              </div>

              {dashboard.topSellingProducts?.length > 0 ? (
                <div className="admin-table-wrapper">
                  <table className="admin-table">
                    <thead>
                      <tr>
                        <th>Product ID</th>
                        <th>Product Name</th>
                        <th>Quantity Sold</th>
                      </tr>
                    </thead>

                    <tbody>
                      {dashboard.topSellingProducts.map((product) => (
                        <tr key={product.productId}>
                          <td>#{product.productId}</td>
                          <td>{product.productName}</td>
                          <td>{product.totalQuantitySold}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <p className="admin-empty">
                  No sales data available.
                </p>
              )}
            </section>

            {/* Dashboard Footer Information */}
            <div className="admin-dashboard-footer">
              <TrendingUp size={18} />
              <span>
                Your SalesSavvy store overview is up to date.
              </span>
            </div>
          </>
        )}
      </main>

      <Footer />
    </>
  );
}

export default AdminDashboard;
