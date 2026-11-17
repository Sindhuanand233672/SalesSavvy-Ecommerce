
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Package,
  ArrowRight,
  RefreshCw,
  XCircle
} from "lucide-react";

import api from "../services/api";
import Navbar from "../components/Navbar";
import "../styles/orders.css";

function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");
  const [cancelling, setCancelling] = useState(null);

  const navigate = useNavigate();

  // Fetch customer orders
  const fetchOrders = async (initialLoad = false) => {
    try {
      if (initialLoad) {
        setLoading(true);
      } else {
        setRefreshing(true);
      }

      setError("");

      const response = await api.get("/orders/my-orders");

      setOrders(
        Array.isArray(response.data) ? response.data : []
      );
    } catch (err) {
      console.error("Error fetching orders:", err);

      setError(
        err.response?.data?.message ||
        "Unable to load your orders. Please try again."
      );
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchOrders(true);
  }, []);

  // Cancel an order
  const cancelOrder = async (orderId) => {
    if (
      !window.confirm(
        `Are you sure you want to cancel order #${orderId}?`
      )
    ) {
      return;
    }

    try {
      setCancelling(orderId);
      setError("");

      await api.put(`/orders/${orderId}/cancel`);

      await fetchOrders(false);

      alert("Order cancelled successfully.");
    } catch (err) {
      console.error("Error cancelling order:", err);

      setError(
        err.response?.data?.message ||
        "Unable to cancel this order."
      );
    } finally {
      setCancelling(null);
    }
  };

  const formatPrice = (amount) =>
    new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2
    }).format(amount || 0);

  const formatDate = (date) =>
    date
      ? new Date(date).toLocaleString("en-IN", {
          day: "2-digit",
          month: "short",
          year: "numeric",
          hour: "2-digit",
          minute: "2-digit"
        })
      : "Not available";

  // Initial loading screen only
  if (loading) {
    return (
      <>
        <Navbar />
        <div className="orders-message">
          Loading your orders...
        </div>
      </>
    );
  }

  return (
    <>
      <Navbar />

      <main className="orders-page">
        <div className="orders-heading">
          <div>
            <span className="orders-eyebrow">
              YOUR PURCHASES
            </span>

            <h1>My Orders</h1>

            <p>Track and manage all your purchases.</p>
          </div>

          <button
            className="orders-refresh"
            onClick={() => fetchOrders(false)}
            disabled={refreshing || cancelling !== null}
          >
            <RefreshCw
              size={17}
              className={refreshing ? "spin" : ""}
            />

            {refreshing ? "Refreshing..." : "Refresh"}
          </button>
        </div>

        {error && (
          <div className="orders-error">
            <p>{error}</p>

            <button
              onClick={() => fetchOrders(false)}
              disabled={refreshing}
            >
              Try Again
            </button>
          </div>
        )}

        {orders.length === 0 ? (
          <div className="orders-empty">
            <Package size={65} />

            <h2>No orders yet</h2>

            <p>Your placed orders will appear here.</p>

            <button onClick={() => navigate("/products")}>
              Start Shopping
              <ArrowRight size={18} />
            </button>
          </div>
        ) : (
          <section className="orders-list">
            {orders.map((order) => (
              <article
                className="order-card"
                key={order.id}
              >
                <div className="order-card-header">
                  <div>
                    <span className="order-label">
                      ORDER ID
                    </span>

                    <h3>#{order.id}</h3>
                  </div>

                  <span
                    className={`order-status ${String(
                      order.status || ""
                    )
                      .toLowerCase()
                      .replaceAll("_", "-")}`}
                  >
                    {order.status}
                  </span>
                </div>

                <div className="order-meta">
                  <span>
                    Placed on {formatDate(order.orderDate)}
                  </span>

                  <strong>
                    {formatPrice(order.totalAmount)}
                  </strong>
                </div>

                <div className="order-products">
                  {(order.items || []).map((item) => (
                    <div
                      className="order-product"
                      key={item.id}
                    >
                      <div className="order-product-icon">
                        <Package size={24} />
                      </div>

                      <div className="order-product-details">
                        <h4>{item.productName}</h4>

                        <span>SKU: {item.sku}</span>

                        <span>
                          {formatPrice(item.unitPrice)} ×{" "}
                          {item.quantity}
                        </span>
                      </div>

                      <strong>
                        {formatPrice(item.subtotal)}
                      </strong>
                    </div>
                  ))}
                </div>

                <div className="order-shipping">
                  <span>Delivery Address</span>

                  <p>
                    {order.shippingAddress ||
                      "Address not available"}
                  </p>

                  {order.couponCode && (
                    <p className="order-coupon">
                      Coupon applied: {order.couponCode}
                    </p>
                  )}
                </div>

                <div className="order-card-footer">
                  <button
                    className="order-details-btn"
                    onClick={() =>
                      navigate(`/orders/${order.id}`)
                    }
                  >
                    View Details
                    <ArrowRight size={16} />
                  </button>

                  {order.status === "PENDING" && (
                    <button
                      className="order-cancel-btn"
                      disabled={
                        cancelling !== null ||
                        refreshing
                      }
                      onClick={() => cancelOrder(order.id)}
                    >
                      <XCircle size={16} />

                      {cancelling === order.id
                        ? "Cancelling..."
                        : "Cancel Order"}
                    </button>
                  )}
                </div>
              </article>
            ))}
          </section>
        )}
      </main>
    </>
  );
}

export default Orders;
