
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  MapPin,
  Plus,
  ArrowLeft,
  ArrowRight,
  ShoppingBag,
  Tag,
  X
} from "lucide-react";

import api from "../services/api";
import Navbar from "../components/Navbar";
import "./checkout.css";

function Checkout() {
  const navigate = useNavigate();

  const [addresses, setAddresses] = useState([]);
  const [selectedAddress, setSelectedAddress] = useState("");
  const [showAddressForm, setShowAddressForm] = useState(false);

  const [cartSubtotal, setCartSubtotal] = useState(0);
  const [couponCode, setCouponCode] = useState("");
  const [couponResult, setCouponResult] = useState(null);
  const [couponLoading, setCouponLoading] = useState(false);
  const [couponMessage, setCouponMessage] = useState("");

  const [paymentMethod, setPaymentMethod] = useState("RAZORPAY");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const [address, setAddress] = useState({
    recipientName: "",
    phoneNumber: "",
    addressLine1: "",
    addressLine2: "",
    landmark: "",
    city: "",
    state: "",
    postalCode: "",
    country: "India",
    addressType: "HOME",
    defaultAddress: false
  });

  const formatPrice = (amount) =>
    new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(Number(amount) || 0);

  useEffect(() => {
    let active = true;

    const fetchCheckoutData = async () => {
      try {
        setError("");

        const results = await Promise.allSettled([
          api.get("/addresses"),
          api.get("/cart")
        ]);

        if (!active) return;

        const addressResult = results[0];
        const cartResult = results[1];

        if (addressResult.status === "fulfilled") {
          const data = Array.isArray(addressResult.value.data)
            ? addressResult.value.data
            : [];

          setAddresses(data);

          if (data.length > 0) {
            const defaultAddress = data.find(
              (item) => item.defaultAddress
            );

            setSelectedAddress(
              String(defaultAddress?.id || data[0].id)
            );
          }
        } else {
          setError(
            addressResult.reason?.response?.data?.message ||
              "Unable to load your addresses."
          );
        }

        if (cartResult.status === "fulfilled") {
          const cart = cartResult.value.data;
          setCartSubtotal(Number(cart?.totalAmount) || 0);

          if (!cart?.items?.length) {
            setError(
              "Your cart is empty. Please add products before checkout."
            );
          }
        } else {
          setError(
            cartResult.reason?.response?.data?.message ||
              "Unable to load your cart."
          );
        }
      } finally {
        if (active) setLoading(false);
      }
    };

    fetchCheckoutData();

    return () => {
      active = false;
    };
  }, []);

  const handleAddressChange = (event) => {
    const { name, value, type, checked } = event.target;

    setAddress((previous) => ({
      ...previous,
      [name]: type === "checkbox" ? checked : value
    }));
  };

  const addAddress = async (event) => {
    event.preventDefault();

    try {
      setSubmitting(true);
      setError("");

      const response = await api.post("/addresses", address);
      const newAddress = response.data;

      setAddresses((previous) => [...previous, newAddress]);
      setSelectedAddress(String(newAddress.id));
      setShowAddressForm(false);

      setAddress({
        recipientName: "",
        phoneNumber: "",
        addressLine1: "",
        addressLine2: "",
        landmark: "",
        city: "",
        state: "",
        postalCode: "",
        country: "India",
        addressType: "HOME",
        defaultAddress: false
      });
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to save delivery address."
      );
    } finally {
      setSubmitting(false);
    }
  };

  const applyCoupon = async () => {
    const code = couponCode.trim().toUpperCase();

    if (!code) {
      setCouponResult(null);
      setCouponMessage("Please enter a coupon code.");
      return;
    }

    if (cartSubtotal <= 0) {
      setCouponMessage("Your cart total is not available.");
      return;
    }

    try {
      setCouponLoading(true);
      setCouponMessage("");
      setCouponResult(null);

      const response = await api.post("/coupons/validate", {
        code,
        orderAmount: cartSubtotal
      });

      const result = response.data;

      if (
        result?.valid === true &&
        Number.isFinite(Number(result.discountAmount)) &&
        Number.isFinite(Number(result.finalAmount))
      ) {
        setCouponCode(code);
        setCouponResult(result);
        setCouponMessage(
          result.message || "Coupon applied successfully."
        );
      } else {
        setCouponResult(null);
        setCouponMessage(
          result?.message || "This coupon is not valid."
        );
      }
    } catch (err) {
      setCouponResult(null);
      setCouponMessage(
        err.response?.data?.message ||
          err.response?.data?.detail ||
          "Unable to validate this coupon."
      );
    } finally {
      setCouponLoading(false);
    }
  };

  const removeCoupon = () => {
    setCouponCode("");
    setCouponResult(null);
    setCouponMessage("");
  };

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

  const placeOrder = async () => {
    if (!selectedAddress) {
      setError("Please select a delivery address.");
      return;
    }

    if (cartSubtotal <= 0) {
      setError("Your cart is empty or its total could not be loaded.");
      return;
    }

    if (couponCode.trim() && !couponResult?.valid) {
      setCouponMessage(
        "Please apply a valid coupon or remove the coupon code."
      );
      return;
    }

    try {
      setSubmitting(true);
      setError("");

      const response = await api.post("/checkout", {
        addressId: Number(selectedAddress),
        couponCode: couponResult?.valid ? couponCode.trim() : null,
        paymentMethod
      });

      const checkoutData = response.data;
      const orderId =
        checkoutData?.orderId ||
        checkoutData?.order?.id;

      if (!orderId) {
        throw new Error(
          "Checkout succeeded, but the server did not return an order ID."
        );
      }

      const payment = checkoutData?.payment;

      const showPendingOrder = (message) => {
        setSubmitting(false);
        navigate(`/order-confirmation?orderId=${orderId}`, {
          state: {
            checkout: checkoutData,
            paymentNotice: message
          }
        });
      };

      if (paymentMethod === "CASH_ON_DELIVERY") {
        navigate(`/order-confirmation?orderId=${orderId}`, {
          state: { checkout: checkoutData }
        });
        return;
      }

      if (
        !payment?.razorpayOrderId ||
        !payment?.razorpayKeyId ||
        payment?.amount == null
      ) {
        throw new Error(
          "The server did not return the required Razorpay payment details."
        );
      }

      const scriptLoaded = await loadRazorpayScript();

      if (!scriptLoaded) {
        showPendingOrder(
          "Your order was created, but Razorpay Checkout could not load. Use Retry Payment on this page."
        );
        return;
      }

      const amountInPaise = Math.round(Number(payment.amount) * 100);

      if (!Number.isSafeInteger(amountInPaise) || amountInPaise <= 0) {
        throw new Error("The payment amount returned by the server is invalid.");
      }

      const options = {
        key: payment.razorpayKeyId,
        amount: amountInPaise,
        currency: "INR",
        name: "SalesSavvy",
        description: `Payment for Order #${orderId}`,
        order_id: payment.razorpayOrderId,

        handler: async (razorpayResponse) => {
          try {
            setError("");

            const verificationResponse = await api.post(
              "/payments/verify",
              {
                razorpayOrderId: razorpayResponse.razorpay_order_id,
                razorpayPaymentId: razorpayResponse.razorpay_payment_id,
                razorpaySignature: razorpayResponse.razorpay_signature
              }
            );

            navigate(`/order-confirmation?orderId=${orderId}`, {
              state: {
                checkout: {
                  ...checkoutData,
                  payment: verificationResponse.data
                }
              }
            });
          } catch (verificationError) {
            showPendingOrder(
              verificationError.response?.data?.message ||
                verificationError.response?.data?.detail ||
                "Payment verification is pending. Check your order status and retry if the payment was not captured."
            );
          }
        },

        modal: {
          ondismiss: () => {
            showPendingOrder(
              "Payment window was closed. Your order is still pending. You can retry payment from this page."
            );
          }
        },

        theme: {
          color: "#2563eb"
        }
      };

      const razorpay = new window.Razorpay(options);

      razorpay.on("payment.failed", (failureResponse) => {
        showPendingOrder(
          failureResponse.error?.description ||
            "Payment was not completed. Your order is still pending. You can retry payment."
        );
      });

      razorpay.open();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.detail ||
          err.message ||
          "Unable to complete checkout. Please try again."
      );
      setSubmitting(false);
    }
  };

  const discountAmount = couponResult?.valid
    ? Number(couponResult.discountAmount) || 0
    : 0;

  const finalAmount = couponResult?.valid
    ? Number(couponResult.finalAmount)
    : cartSubtotal;

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="checkout-message">
          Loading checkout...
        </div>
      </>
    );
  }

  return (
    <>
      <Navbar />

      <main className="checkout-page">
        <div className="checkout-heading">
          <span className="checkout-eyebrow">ALMOST THERE</span>
          <h1>Checkout</h1>
          <p>Complete your delivery and payment details.</p>
        </div>

        {error && (
          <div className="checkout-error" role="alert">
            {error}
          </div>
        )}

        <div className="checkout-layout">
          <section className="checkout-main">

            {/* DELIVERY ADDRESS */}
            <div className="checkout-card">
              <div className="checkout-card-heading">
                <div>
                  <MapPin size={21} />
                  <h2>Delivery Address</h2>
                </div>

                <button
                  type="button"
                  className="add-address-btn"
                  onClick={() => setShowAddressForm(!showAddressForm)}
                  disabled={submitting}
                >
                  <Plus size={17} />
                  Add Address
                </button>
              </div>

              {addresses.length === 0 && !showAddressForm && (
                <p className="no-address">
                  You haven't saved a delivery address yet.
                </p>
              )}

              <div className="address-list">
                {addresses.map((item) => (
                  <label
                    className={`address-option ${
                      selectedAddress === String(item.id) ? "selected" : ""
                    }`}
                    key={item.id}
                  >
                    <input
                      type="radio"
                      name="deliveryAddress"
                      value={item.id}
                      checked={selectedAddress === String(item.id)}
                      onChange={() => setSelectedAddress(String(item.id))}
                    />

                    <div>
                      <div className="address-title">
                        <strong>{item.recipientName}</strong>
                        <span>{item.addressType}</span>
                        {item.defaultAddress && <small>DEFAULT</small>}
                      </div>

                      <p>
                        {item.addressLine1}
                        {item.addressLine2 && `, ${item.addressLine2}`}
                        {item.landmark && `, ${item.landmark}`}
                      </p>

                      <p>
                        {item.city}, {item.state} - {item.postalCode}
                      </p>

                      <p>{item.country}</p>
                      <p>Phone: {item.phoneNumber}</p>
                    </div>
                  </label>
                ))}
              </div>

              {showAddressForm && (
                <form className="address-form" onSubmit={addAddress}>
                  <h3>Add Delivery Address</h3>

                  <div className="address-form-grid">
                    <input
                      name="recipientName"
                      placeholder="Recipient Name"
                      value={address.recipientName}
                      onChange={handleAddressChange}
                      required
                    />

                    <input
                      name="phoneNumber"
                      placeholder="Phone Number"
                      value={address.phoneNumber}
                      onChange={handleAddressChange}
                      pattern="\+?[0-9]{10,15}"
                      title="Enter a valid 10 to 15 digit phone number"
                      required
                    />

                    <input
                      name="addressLine1"
                      placeholder="Address Line 1"
                      value={address.addressLine1}
                      onChange={handleAddressChange}
                      required
                    />

                    <input
                      name="addressLine2"
                      placeholder="Address Line 2 (Optional)"
                      value={address.addressLine2}
                      onChange={handleAddressChange}
                    />

                    <input
                      name="landmark"
                      placeholder="Landmark (Optional)"
                      value={address.landmark}
                      onChange={handleAddressChange}
                    />

                    <input
                      name="city"
                      placeholder="City"
                      value={address.city}
                      onChange={handleAddressChange}
                      required
                    />

                    <input
                      name="state"
                      placeholder="State"
                      value={address.state}
                      onChange={handleAddressChange}
                      required
                    />

                    <input
                      name="postalCode"
                      placeholder="Postal Code"
                      value={address.postalCode}
                      onChange={handleAddressChange}
                      required
                    />

                    <input
                      name="country"
                      placeholder="Country"
                      value={address.country}
                      onChange={handleAddressChange}
                      required
                    />

                    <select
                      name="addressType"
                      value={address.addressType}
                      onChange={handleAddressChange}
                    >
                      <option value="HOME">Home</option>
                      <option value="OFFICE">Office</option>
                      <option value="OTHER">Other</option>
                    </select>
                  </div>

                  <label className="default-address-check">
                    <input
                      type="checkbox"
                      name="defaultAddress"
                      checked={address.defaultAddress}
                      onChange={handleAddressChange}
                    />
                    Set as default address
                  </label>

                  <div className="address-form-actions">
                    <button
                      type="button"
                      className="cancel-address-btn"
                      onClick={() => setShowAddressForm(false)}
                    >
                      Cancel
                    </button>

                    <button
                      type="submit"
                      className="save-address-btn"
                      disabled={submitting}
                    >
                      {submitting ? "Saving..." : "Save Address"}
                    </button>
                  </div>
                </form>
              )}
            </div>

            {/* PAYMENT METHOD */}
            <div className="checkout-card">
              <div className="checkout-card-heading">
                <div>
                  <ShoppingBag size={21} />
                  <h2>Payment Method</h2>
                </div>
              </div>

              <div className="payment-options">
                {[
                  ["RAZORPAY", "Pay Online (UPI / Cards / Net Banking)"],
                  ["CASH_ON_DELIVERY", "Cash on Delivery"]
                ].map(([value, label]) => (
                  <label
                    key={value}
                    className={`payment-option ${
                      paymentMethod === value ? "selected" : ""
                    }`}
                  >
                    <input
                      type="radio"
                      name="paymentMethod"
                      value={value}
                      checked={paymentMethod === value}
                      onChange={() => setPaymentMethod(value)}
                    />
                    <span>{label}</span>
                  </label>
                ))}
              </div>

              <p className="payment-note">
                Payment will be initiated when you place your order.
              </p>
            </div>

            {/* COUPON */}
            <div className="checkout-card">
              <div className="checkout-card-heading">
                <div>
                  <Tag size={21} />
                  <h2>Apply Coupon</h2>
                </div>
              </div>

              <div className="coupon-input-row">
                <input
                  type="text"
                  placeholder="Enter coupon code"
                  value={couponCode}
                  disabled={couponResult?.valid || couponLoading}
                  onChange={(event) => {
                    setCouponCode(event.target.value.toUpperCase());
                    setCouponResult(null);
                    setCouponMessage("");
                  }}
                />

                {couponResult?.valid ? (
                  <button
                    type="button"
                    className="coupon-remove-btn"
                    onClick={removeCoupon}
                  >
                    <X size={16} />
                    Remove
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={applyCoupon}
                    disabled={couponLoading || !couponCode.trim()}
                  >
                    {couponLoading ? "Checking..." : "Apply"}
                  </button>
                )}
              </div>

              {couponMessage && (
                <p
                  className={`coupon-feedback ${
                    couponResult?.valid ? "success" : "error"
                  }`}
                >
                  {couponMessage}
                </p>
              )}

              {couponResult?.valid && (
                <div className="coupon-applied">
                  <Tag size={17} />
                  <span>
                    Coupon{" "}
                    <strong>
                      {couponResult.couponCode || couponCode}
                    </strong>{" "}
                    applied!
                  </span>
                </div>
              )}
            </div>
          </section>

          {/* ORDER SUMMARY */}
          <aside className="checkout-summary">
            <h2>Order Summary</h2>

            <div className="checkout-price-row">
              <span>Subtotal</span>
              <strong>{formatPrice(cartSubtotal)}</strong>
            </div>

            <div className="checkout-price-row">
              <span>Shipping</span>
              <strong className="free-shipping">FREE</strong>
            </div>

            {couponResult?.valid && (
              <div className="checkout-price-row discount-row">
                <span>Coupon Discount</span>
                <strong>- {formatPrice(discountAmount)}</strong>
              </div>
            )}

            <div className="checkout-summary-divider" />

            <div className="checkout-final-total">
              <span>Total Payable</span>
              <strong>{formatPrice(finalAmount)}</strong>
            </div>

            <div className="checkout-summary-note">
              <span>Delivery</span>
              <strong>
                {selectedAddress ? "Address selected" : "Select an address"}
              </strong>
            </div>

            <div className="checkout-summary-note">
              <span>Payment</span>
              <strong>{paymentMethod.replaceAll("_", " ")}</strong>
            </div>

            <button
              className="place-order-btn"
              onClick={placeOrder}
              disabled={
                submitting ||
                couponLoading ||
                !selectedAddress ||
                cartSubtotal <= 0 ||
                (couponCode.trim() !== "" && !couponResult?.valid)
              }
            >
              {submitting ? "Processing..." : "Place Order"}
              <ArrowRight size={18} />
            </button>

            <button
              className="back-to-cart-btn"
              onClick={() => navigate("/cart")}
              disabled={submitting}
            >
              <ArrowLeft size={17} />
              Back to Cart
            </button>

            <p className="checkout-secure-note">
              Your final payable amount will be verified by the server.
            </p>
          </aside>
        </div>
      </main>
    </>
  );
}

export default Checkout;
