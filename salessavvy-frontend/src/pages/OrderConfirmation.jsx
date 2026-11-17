import { useEffect, useState, useCallback } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import {
  CheckCircle,
  XCircle,
  Clock,
  Package,
  ArrowRight,
  RefreshCw
} from "lucide-react";

import Navbar from "../components/Navbar";
import api from "../services/api";
import "./checkout.css";

function OrderConfirmation() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const initialCheckout = state?.checkout || null;

  const orderId =
    searchParams.get("orderId") ||
    initialCheckout?.orderId ||
    initialCheckout?.order?.id;

  const [order, setOrder] = useState(initialCheckout?.order || null);
  const [payment, setPayment] = useState(initialCheckout?.payment || null);
  const [loading, setLoading] = useState(Boolean(orderId));
  const [error, setError] = useState("");
  const [paymentNotice, setPaymentNotice] = useState(
    state?.paymentNotice || ""
  );
  const [retrying, setRetrying] = useState(false);

  const loadConfirmation = useCallback(async (showLoader = true) => {
    if (!orderId) {
      setLoading(false);
      return;
    }

    try {
      if (showLoader) setLoading(true);
      setError("");

      const [orderResult, paymentResult] = await Promise.all([
        api.get(`/orders/${orderId}`),
        api.get(`/payments/order/${orderId}`)
      ]);

      setOrder(orderResult.data);

      const payments = Array.isArray(paymentResult.data)
        ? paymentResult.data
        : paymentResult.data
          ? [paymentResult.data]
          : [];

      // The newest payment is returned first by the backend.
      setPayment(payments[0] || null);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Unable to refresh order details. Showing the available order information."
      );
    } finally {
      setLoading(false);
    }
  }, [orderId]);

  useEffect(() => {
    loadConfirmation();
  }, [loadConfirmation]);

  const loadRazorpayScript = () =>
    new Promise((resolve) => {
      if (window.Razorpay) {
        resolve(true);
        return;
      }

      const script = document.createElement("script");
      script.src = "https://checkout.razorpay.com/v1/checkout.js";
      script.async = true;
      script.onload = () => resolve(true);
      script.onerror = () => resolve(false);
      document.body.appendChild(script);
    });

  const retryPayment = async () => {
    if (
      !displayedPayment?.razorpayOrderId ||
      !displayedPayment?.razorpayKeyId ||
      !displayedPayment?.amount ||
      retrying
    ) {
      return;
    }

    try {
      setRetrying(true);
      setError("");
      setPaymentNotice("");

      const scriptLoaded = await loadRazorpayScript();
      if (!scriptLoaded) {
        throw new Error("Unable to load Razorpay Checkout. Check your internet connection.");
      }

      const amountInPaise = Math.round(Number(displayedPayment.amount) * 100);
      if (!Number.isSafeInteger(amountInPaise) || amountInPaise <= 0) {
        throw new Error("The payment amount is invalid.");
      }

      const razorpay = new window.Razorpay({
        key: displayedPayment.razorpayKeyId,
        amount: amountInPaise,
        currency: "INR",
        name: "SalesSavvy",
        description: `Payment for Order #${orderId}`,
        order_id: displayedPayment.razorpayOrderId,
        handler: async (razorpayResponse) => {
          try {
            await api.post("/payments/verify", {
              razorpayOrderId: razorpayResponse.razorpay_order_id,
              razorpayPaymentId: razorpayResponse.razorpay_payment_id,
              razorpaySignature: razorpayResponse.razorpay_signature
            });

            setPaymentNotice("Payment verified successfully.");
            await loadConfirmation(false);
          } catch (verificationError) {
            setError(
              verificationError.response?.data?.message ||
              verificationError.response?.data?.detail ||
              "Payment verification is not complete. Refresh the order status before trying again."
            );
            await loadConfirmation(false);
          } finally {
            setRetrying(false);
          }
        },
        modal: {
          ondismiss: () => {
            setRetrying(false);
            setPaymentNotice("Payment window closed. Your order remains pending.");
          }
        },
        theme: { color: "#2563eb" }
      });

      razorpay.on("payment.failed", (failureResponse) => {
        setRetrying(false);
        setPaymentNotice(
          failureResponse.error?.description || "Payment was not completed. You can retry."
        );
      });

      razorpay.open();
    } catch (err) {
      setRetrying(false);
      setError(err.message || "Unable to start payment retry.");
    }
  };

  const formatPrice = (amount) =>
    new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(Number(amount || 0));

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="checkout-message">
          Loading your order confirmation...
        </div>
      </>
    );
  }

  if (!orderId || (!order && !initialCheckout)) {
    return (
      <>
        <Navbar />
        <div className="checkout-message">
          <h2>Order details not found.</h2>
          <button onClick={() => navigate("/orders")}>
            Go to My Orders
          </button>
        </div>
      </>
    );
  }

  const displayedOrder = order || initialCheckout?.order || {};
  const displayedPayment = payment || initialCheckout?.payment || {};

  const total =
    displayedOrder.totalAmount ??
    initialCheckout?.finalAmount ??
    displayedPayment.amount;

  const couponCode =
    displayedOrder.couponCode ??
    initialCheckout?.couponCode;

  const paymentStatus = String(
    displayedPayment.status || "NOT AVAILABLE"
  ).toUpperCase();

  const orderStatus = String(
    displayedOrder.status || "NOT AVAILABLE"
  ).toUpperCase();

  const paymentMethod = String(
    displayedPayment.paymentMethod || ""
  ).toUpperCase();

  const isCashOnDelivery = paymentMethod === "CASH_ON_DELIVERY";
  const isPaymentSuccessful = paymentStatus === "SUCCESS";
  const isOrderConfirmed = orderStatus === "CONFIRMED";

  const isPaymentFailed =
    paymentStatus === "FAILED" ||
    orderStatus === "CANCELLED";

  const isCodConfirmed = isCashOnDelivery && isOrderConfirmed;
  const canRetryPayment =
    paymentMethod === "RAZORPAY" &&
    paymentStatus === "INITIATED" &&
    orderStatus === "PENDING" &&
    Boolean(displayedPayment.razorpayOrderId && displayedPayment.razorpayKeyId);

  const statusType =
    isPaymentSuccessful || isCodConfirmed
      ? "success"
      : isPaymentFailed
        ? "failure"
        : "pending";

  const StatusIcon =
    statusType === "success"
      ? CheckCircle
      : statusType === "failure"
        ? XCircle
        : Clock;

  const heading = isPaymentSuccessful
    ? "Thank You for Your Order!"
    : isCodConfirmed
      ? "Your Order Has Been Placed!"
      : isPaymentFailed
        ? "Payment Unsuccessful"
        : "Your Order is Awaiting Payment";

  const description = isPaymentSuccessful
    ? "Your Razorpay payment has been verified and your order is confirmed."
    : isCodConfirmed
      ? "Your order is confirmed. Please pay when your order is delivered."
      : isPaymentFailed
        ? "The payment was not successful. Check your order status before trying again."
        : "Your order has been created, but payment is not confirmed yet. You can refresh the details to check the latest status.";

  const transactionId =
    displayedPayment.razorpayPaymentId ||
    displayedPayment.transactionId ||
    "Not available";

  return (
    <>
      <Navbar />

      <main className="confirmation-page">
        <div className={`confirmation-card ${statusType}`}>
          <StatusIcon
            className={`confirmation-icon ${statusType}`}
            size={70}
          />

          <span className={`checkout-eyebrow confirmation-eyebrow ${statusType}`}>
            {isPaymentSuccessful
              ? "PAYMENT SUCCESSFUL"
              : isCodConfirmed
                ? "ORDER CONFIRMED · CASH ON DELIVERY"
                : isPaymentFailed
                  ? "PAYMENT FAILED"
                  : "PAYMENT PENDING"}
          </span>

          <h1>{heading}</h1>
          <p>{description}</p>

          {error && (
            <p role="alert" className="checkout-error">
              {error}
            </p>
          )}

          {paymentNotice && (
            <p role="status" className="checkout-success">
              {paymentNotice}
            </p>
          )}

          <div className="confirmation-order-id">
            Order ID: <strong>#{orderId}</strong>
          </div>

          <div className="confirmation-details">
            <div>
              <span>Coupon</span>
              <strong>{couponCode || "Not applied"}</strong>
            </div>

            <div className="confirmation-total">
              <span>Order Total</span>
              <strong>{formatPrice(total)}</strong>
            </div>
          </div>

          <div className="payment-result">
            <h3>Payment Details</h3>

            <div>
              <span>Payment ID</span>
              <strong>{displayedPayment.id || "Not available"}</strong>
            </div>

            <div>
              <span>Method</span>
              <strong>
                {displayedPayment.paymentMethod?.replaceAll("_", " ") ||
                  "Not available"}
              </strong>
            </div>

            <div>
              <span>Payment Status</span>
              <strong className={`payment-status ${statusType}`}>
                {paymentStatus}
              </strong>
            </div>

            <div>
              <span>Order Status</span>
              <strong>{orderStatus}</strong>
            </div>

            <div>
              <span>Transaction / Razorpay Payment ID</span>
              <strong className="transaction-id">{transactionId}</strong>
            </div>

            {displayedPayment.razorpayOrderId && (
              <div>
                <span>Razorpay Order ID</span>
                <strong className="transaction-id">
                  {displayedPayment.razorpayOrderId}
                </strong>
              </div>
            )}
          </div>

          {!isPaymentSuccessful &&
            !isCodConfirmed &&
            !isPaymentFailed && (
              <div className="confirmation-payment-actions">
                <p>
                  Online payment is not confirmed yet. Do not assume the
                  order is paid. Refresh the order details to check its status.
                </p>
                {canRetryPayment && (
                  <button
                    className="place-order-btn"
                    onClick={retryPayment}
                    disabled={retrying}
                  >
                    {retrying ? "Opening Razorpay..." : "Retry Payment"}
                    <ArrowRight size={18} />
                  </button>
                )}
              </div>
            )}

          <div className="confirmation-actions">
            <button
              className="place-order-btn"
              onClick={() => navigate(`/orders/${orderId}`)}
            >
              View Order <ArrowRight size={18} />
            </button>

            <button
              className="place-order-btn"
              onClick={() => navigate("/products")}
            >
              Continue Shopping <ArrowRight size={18} />
            </button>
          </div>

          <button
            className="confirmation-refresh"
            onClick={() => loadConfirmation()}
          >
            <RefreshCw size={16} />
            Refresh Order Details
          </button>

          <p className={`confirmation-note ${statusType}`}>
            <Package size={17} />
            {isPaymentSuccessful
              ? "Your payment is verified and your order is confirmed."
              : isCodConfirmed
                ? "Your order is confirmed. Payment is due on delivery."
                : isPaymentFailed
                  ? "The order is cancelled or the payment failed. Check My Orders for details."
                  : "Your order is awaiting payment confirmation."}
          </p>
        </div>
      </main>
    </>
  );
}

export default OrderConfirmation;
