
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  ArrowLeft,
  Package,
  CreditCard,
  CheckCircle,
  XCircle,
  RefreshCw,
  Truck,
  Clock
} from "lucide-react";

import api from "../services/api";
import Navbar from "../components/Navbar";

function OrderDetails() {
  const { orderId } = useParams();
  const navigate = useNavigate();

  const [order, setOrder] = useState(null);
  const [payments, setPayments] = useState([]);
  const [tracking, setTracking] = useState([]);

  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [processingId, setProcessingId] = useState(null);
  const [cancelling, setCancelling] = useState(false);

  const [error, setError] = useState("");
  const [paymentError, setPaymentError] = useState("");
  const [trackingError, setTrackingError] = useState("");
  const [message, setMessage] = useState("");

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

  const fetchDetails = async (showRefresh = false) => {
    try {
      if (showRefresh) {
        setRefreshing(true);
        setMessage("");
      }

      setError("");
      setPaymentError("");
      setTrackingError("");

      // 1. Fetch order details
      const orderResponse = await api.get(`/orders/${orderId}`);
      setOrder(orderResponse.data);

      // 2. Fetch payment details
      try {
        const paymentResponse = await api.get(
          `/payments/order/${orderId}`
        );

        const data = paymentResponse.data;

        setPayments(
          Array.isArray(data)
            ? data
            : data
              ? [data]
              : []
        );
      } catch (paymentErr) {
        console.error("Payment details error:", paymentErr);
        setPayments([]);
        setPaymentError("Unable to load payment details.");
      }

      // 3. Fetch order tracking history
      try {
        const trackingResponse = await api.get(
          `/orders/${orderId}/tracking`
        );

        setTracking(
          Array.isArray(trackingResponse.data)
            ? trackingResponse.data
            : []
        );
      } catch (trackingErr) {
        console.error("Order tracking error:", trackingErr);
        setTracking([]);
        setTrackingError("Unable to load order tracking history.");
      }

      if (showRefresh) {
        setMessage("Order, payment and tracking details refreshed successfully.");
      }
    } catch (err) {
      console.error("Order details error:", err);

      setOrder(null);
      setError(
        err.response?.data?.message ||
        "Unable to load order details."
      );
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    setLoading(true);
    fetchDetails();
  }, [orderId]);

  const handlePayment = async (paymentId, action) => {
    const actionText =
      action === "complete" ? "complete" : "fail";

    if (
      !window.confirm(
        `Are you sure you want to ${actionText} payment #${paymentId}?`
      )
    ) {
      return;
    }

    try {
      setProcessingId(paymentId);
      setError("");
      setMessage("");

      await api.put(`/payments/${paymentId}/${action}`);

      setMessage(
        action === "complete"
          ? "Payment completed successfully!"
          : "Payment marked as failed."
      );

      await fetchDetails();
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Unable to process payment."
      );
    } finally {
      setProcessingId(null);
    }
  };

  const handleCancelOrder = async () => {
    if (
      !window.confirm(
        `Are you sure you want to cancel order #${orderId}?`
      )
    ) {
      return;
    }

    try {
      setCancelling(true);
      setError("");
      setMessage("");

      await api.put(`/orders/${orderId}/cancel`);

      setMessage(
        "Order cancelled successfully. Any successful payment has been marked as refunded."
      );

      await fetchDetails();
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Unable to cancel this order."
      );
    } finally {
      setCancelling(false);
    }
  };

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="order-details-message">
          Loading order details...
        </div>
      </>
    );
  }

  if (!order) {
    return (
      <>
        <Navbar />
        <div className="order-details-message">
          <h2>{error || "Order not found"}</h2>
          <button onClick={() => navigate("/orders")}>
            Back to My Orders
          </button>
        </div>
      </>
    );
  }

  const canCancel =
    order.status === "PENDING" ||
    order.status === "CONFIRMED";

  return (
    <>
      <Navbar />

      <main className="order-details-page">

        <div className="order-details-actions">
          <button
            className="back-orders-btn"
            onClick={() => navigate("/orders")}
          >
            <ArrowLeft size={18} />
            Back to My Orders
          </button>

          {canCancel && (
            <button
              className="cancel-order-btn"
              onClick={handleCancelOrder}
              disabled={cancelling || processingId !== null || refreshing}
            >
              <XCircle size={17} />
              {cancelling ? "Cancelling..." : "Cancel Order"}
            </button>
          )}
        </div>

        <div className="order-details-heading">
          <div>
            <span className="order-eyebrow">
              PURCHASE INFORMATION
            </span>

            <h1>Order #{order.id}</h1>

            <p>Placed on {formatDate(order.orderDate)}</p>
          </div>

          <span
            className={`order-status ${String(order.status || "")
              .toLowerCase()
              .replaceAll("_", "-")}`}
          >
            {order.status}
          </span>
        </div>

        {message && (
          <div className="payment-alert success">
            <CheckCircle size={18} />
            {message}
          </div>
        )}

        {error && (
          <div className="payment-alert error">
            {error}
          </div>
        )}

        {/* ORDERED PRODUCTS */}
        <section className="order-detail-card">
          <h2>
            <Package size={21} />
            Ordered Products
          </h2>

          {(order.items || []).map((item) => (
            <div className="detail-product" key={item.id}>
              <div>
                <h3>{item.productName}</h3>
                <p>SKU: {item.sku}</p>

                <p>
                  {formatPrice(item.unitPrice)} × {item.quantity}
                </p>
              </div>

              <strong>{formatPrice(item.subtotal)}</strong>
            </div>
          ))}

          {order.couponCode && (
            <p>Coupon applied: {order.couponCode}</p>
          )}

          <div className="detail-total">
            <span>Total Amount</span>
            <strong>{formatPrice(order.totalAmount)}</strong>
          </div>
        </section>

        {/* DELIVERY ADDRESS */}
        <section className="order-detail-card">
          <h2>
            <Truck size={21} />
            Delivery Address
          </h2>

          <p className="shipping-address">
            {order.shippingAddress || "Address not available"}
          </p>

          {order.estimatedDeliveryDate && (
            <div className="delivery-estimate">
              <strong>Estimated Delivery:</strong>{" "}
              {new Date(
                `${order.estimatedDeliveryDate}T00:00:00`
              ).toLocaleDateString("en-IN", {
                day: "2-digit",
                month: "long",
                year: "numeric"
              })}

              {order.deliveryStartTime && order.deliveryEndTime && (
                <p>
                  Expected time: {order.deliveryStartTime} –{" "}
                  {order.deliveryEndTime}
                </p>
              )}
            </div>
          )}

          {order.deliveredAt && (
            <p className="delivery-completed">
              <CheckCircle size={17} />
              Delivered on {formatDate(order.deliveredAt)}
            </p>
          )}

          {order.deliveryRemarks && (
            <p className="shipping-address">
              {order.deliveryRemarks}
            </p>
          )}
        </section>

        {/* ORDER TRACKING */}
        <section className="order-detail-card">
          <h2>
            <Truck size={21} />
            Order Tracking
          </h2>

          {trackingError && (
            <div className="payment-alert error">
              {trackingError}
            </div>
          )}

          {!trackingError && tracking.length === 0 && (
            <p className="shipping-address">
              No tracking updates are available yet.
            </p>
          )}

          {!trackingError && tracking.length > 0 && (
            <div className="tracking-timeline">
              {tracking.map((event, index) => (
                <div className="tracking-event" key={event.id || index}>
                  <div className="tracking-marker">
                    <CheckCircle size={20} />
                  </div>

                  <div className="tracking-content">
                    <h3>
                      {String(event.newStatus || "")
                        .replaceAll("_", " ")}
                    </h3>

                    {event.previousStatus && (
                      <p>
                        Previous status:{" "}
                        {String(event.previousStatus).replaceAll("_", " ")}
                      </p>
                    )}

                    {event.remarks && (
                      <p>{event.remarks}</p>
                    )}

                    <span>
                      <Clock size={14} />
                      {formatDate(event.updatedAt)}
                    </span>

                    {event.updatedBy && (
                      <small>
                        Updated by: {event.updatedBy}
                      </small>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* PAYMENT DETAILS */}
        <section className="order-detail-card">
          <h2>
            <CreditCard size={21} />
            Payment Details
          </h2>

          {paymentError && (
            <p className="payment-alert error">
              {paymentError}
            </p>
          )}

          {!paymentError && payments.length === 0 ? (
            <p className="shipping-address">
              No payment details available.
            </p>
          ) : (
            payments.map((payment) => (
              <div key={payment.id} className="payment-detail">

                <div className="payment-detail-row">
                  <span>Payment ID</span>
                  <strong>{payment.id}</strong>
                </div>

                <div className="payment-detail-row">
                  <span>Payment Method</span>
                  <strong>{payment.paymentMethod}</strong>
                </div>

                <div className="payment-detail-row">
                  <span>Amount</span>
                  <strong>{formatPrice(payment.amount)}</strong>
                </div>

                <div className="payment-detail-row">
                  <span>Payment Status</span>

                  <strong
                    className={`payment-status ${String(
                      payment.status || ""
                    ).toLowerCase()}`}
                  >
                    {payment.status}
                  </strong>
                </div>

                <div className="payment-detail-row">
                  <span>Transaction ID</span>

                  <strong className="transaction-id">
                    {payment.transactionId || "Not available"}
                  </strong>
                </div>

                <div className="payment-detail-row">
                  <span>Payment Date</span>

                  <strong>
                    {formatDate(payment.paymentDate)}
                  </strong>
                </div>

                {payment.status === "INITIATED" && (
                  <div className="payment-actions">

                    <button
                      className="payment-complete-btn"
                      disabled={
                        processingId !== null ||
                        cancelling ||
                        refreshing
                      }
                      onClick={() =>
                        handlePayment(payment.id, "complete")
                      }
                    >
                      <CheckCircle size={17} />

                      {processingId === payment.id
                        ? "Processing..."
                        : "Complete Payment"}
                    </button>

                    <button
                      className="payment-fail-btn"
                      disabled={
                        processingId !== null ||
                        cancelling ||
                        refreshing
                      }
                      onClick={() =>
                        handlePayment(payment.id, "fail")
                      }
                    >
                      <XCircle size={17} />
                      Simulate Failure
                    </button>

                  </div>
                )}
              </div>
            ))
          )}

          {/* REFRESH ALL DETAILS */}
          <button
            className="payment-refresh-btn"
            onClick={() => fetchDetails(true)}
            disabled={
              refreshing ||
              processingId !== null ||
              cancelling
            }
          >
            <RefreshCw
              size={16}
              className={refreshing ? "refresh-spinning" : ""}
            />

            {refreshing
              ? "Refreshing Details..."
              : "Refresh Order Details"}
          </button>
        </section>

      </main>
    </>
  );
}

export default OrderDetails;
