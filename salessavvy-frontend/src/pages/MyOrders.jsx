
import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { Package, RefreshCw, ArrowRight, XCircle } from "lucide-react";
import api from "../services/api";
import Navbar from "../components/Navbar";

function MyOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const navigate = useNavigate();

  const fetchOrders = useCallback(async () => {
    try {
      setError("");
      setLoading(true);

      const response = await api.get("/orders/my-orders");
      setOrders(response.data || []);
    } catch (err) {
      setError(
        err.response?.data?.message || "Unable to load your orders."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const cancelOrder = async (orderId) => {
    if (!window.confirm(`Are you sure you want to cancel order #${orderId}?`)) {
      return;
    }

    try {
      await api.put(`/orders/${orderId}/cancel`);
      alert(
  "Order cancelled successfully! Any successful payment has been marked as refunded."
);
      fetchOrders();
    } catch (err) {
      alert(
        err.response?.data?.message || "Unable to cancel this order."
      );
    }
  };

  const formatPrice = (amount) =>
    new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2
    }).format(amount || 0);

  const formatDate = (date) =>
    new Date(date).toLocaleString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });

  return (
    <>
      <Navbar />

      <main className="orders-page">
        <div className="orders-heading">
          <div>
            <span className="orders-eyebrow">YOUR PURCHASES</span>
            <h1>My Orders</h1>
            <p>Track and manage all your purchases.</p>
          </div>

          <button
            className="orders-refresh-btn"
            onClick={fetchOrders}
            disabled={loading}
          >
            <RefreshCw size={17} /> Refresh
          </button>
        </div>

        {loading ? (
          <div className="orders-message">Loading your orders...</div>
        ) : error ? (
          <div className="orders-message">
            <p>{error}</p>
            <button onClick={fetchOrders}>Try Again</button>
          </div>
        ) : orders.length === 0 ? (
          <div className="orders-empty">
            <Package size={55} />
            <h2>No orders yet</h2>
            <p>Your purchases will appear here.</p>
            <button onClick={() => navigate("/products")}>
              Start Shopping <ArrowRight size={17} />
            </button>
          </div>
        ) : (
          <div className="orders-list">
            {orders.map((order) => (
              <article className="order-card" key={order.id}>
                <div className="order-card-header">
                  <div>
                    <span className="order-label">ORDER ID</span>
                    <h2>#{order.id}</h2>
                  </div>

                  <span
                    className={`order-status ${order.status?.toLowerCase()}`}
                  >
                    {order.status}
                  </span>
                </div>

                <div className="order-card-meta">
                  <span>Placed on {formatDate(order.orderDate)}</span>
                  <strong>{formatPrice(order.totalAmount)}</strong>
                </div>

                <div className="order-products">
                  {order.items?.map((item) => (
                    <div className="order-product" key={item.id}>
                      <div className="order-product-icon">
                        <Package size={24} />
                      </div>

                      <div className="order-product-info">
                        <h3>{item.productName}</h3>
                        <p>SKU: {item.sku}</p>
                        <p>
                          {formatPrice(item.unitPrice)} × {item.quantity}
                        </p>
                      </div>

                      <strong>{formatPrice(item.subtotal)}</strong>
                    </div>
                  ))}
                </div>

                <div className="order-shipping">
                  <strong>Delivery Address</strong>
                  <p>{order.shippingAddress}</p>
                </div>

                <div className="order-card-actions">
                  <button
                    className="view-order-btn"
                    onClick={() => navigate(`/orders/${order.id}`)}
                  >
                    View Details <ArrowRight size={17} />
                  </button>

                  {(order.status === "PENDING" ||
  order.status === "CONFIRMED") && (
                    <button
                      className="cancel-order-btn"
                      onClick={() => cancelOrder(order.id)}
                    >
                      <XCircle size={17} /> Cancel Order
                    </button>
                  )}
                </div>
              </article>
            ))}
          </div>
        )}
      </main>
    </>
  );
}

export default MyOrders;
