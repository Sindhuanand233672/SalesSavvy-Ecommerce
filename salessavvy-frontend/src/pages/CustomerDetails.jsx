
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import api from "../services/api";

function CustomerDetails() {
  const { customerId } = useParams();
  const navigate = useNavigate();

  const [customer, setCustomer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchCustomerDetails = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get(
        `/admin/customers/${customerId}`
      );

      setCustomer(response.data);
    } catch (err) {
      console.error("Customer details error:", err);

      setError(
        err.response?.data?.message ||
        "Unable to load customer details."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCustomerDetails();
  }, [customerId]);

  const formatDate = (date) => {
    return date
      ? new Date(date).toLocaleString("en-IN", {
          day: "2-digit",
          month: "short",
          year: "numeric",
          hour: "2-digit",
          minute: "2-digit"
        })
      : "-";
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2
    }).format(amount || 0);
  };

  const statusStyle = (status) => {
    const normalized = (status || "").toUpperCase();

    if (normalized === "DELIVERED") {
      return { background: "#e4f8ed", color: "#087443" };
    }

    if (normalized === "CANCELLED" || normalized === "REFUNDED") {
      return { background: "#ffe9e9", color: "#c62828" };
    }

    if (normalized === "SHIPPED" || normalized === "OUT_FOR_DELIVERY") {
      return { background: "#e8f1ff", color: "#2459a6" };
    }

    return { background: "#fff3df", color: "#a15c00" };
  };

  const pageStyle = {
    width: "min(1200px, 94%)",
    margin: "0 auto",
    padding: "40px 0 70px",
    minHeight: "75vh",
    color: "#242433"
  };

  const cardStyle = {
    background: "#ffffff",
    border: "1px solid #eae8f1",
    borderRadius: "14px",
    padding: "24px",
    marginBottom: "24px",
    boxShadow: "0 4px 18px rgba(0,0,0,0.025)"
  };

  const headingStyle = {
    margin: "0 0 20px",
    fontSize: "21px",
    color: "#252433"
  };

  const labelStyle = {
    display: "block",
    color: "#858391",
    fontSize: "12px",
    marginBottom: "7px"
  };

  const valueStyle = {
    fontSize: "15px",
    fontWeight: 600,
    overflowWrap: "anywhere"
  };

  const summaryGridStyle = {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(190px, 1fr))",
    gap: "16px"
  };

  if (loading) {
    return (
      <>
        <Navbar />
        <main style={pageStyle}>
          <p>Loading customer details...</p>
        </main>
        <Footer />
      </>
    );
  }

  if (error || !customer) {
    return (
      <>
        <Navbar />
        <main style={pageStyle}>
          <button
            onClick={() => navigate("/admin/customers")}
            style={{
              border: "none",
              background: "none",
              color: "#7053e8",
              cursor: "pointer",
              fontWeight: 700,
              marginBottom: "20px"
            }}
          >
            ← Back to Customers
          </button>

          <div style={cardStyle}>
            <p style={{ color: "#c62828" }}>
              {error || "Customer not found."}
            </p>

            <button onClick={fetchCustomerDetails}>
              Retry
            </button>
          </div>
        </main>
        <Footer />
      </>
    );
  }

  return (
    <>
      <Navbar />

      <main style={pageStyle}>
        <button
          onClick={() => navigate("/admin/customers")}
          style={{
            border: "none",
            background: "none",
            color: "#7053e8",
            cursor: "pointer",
            fontWeight: 700,
            fontSize: "14px",
            marginBottom: "22px"
          }}
        >
          ← Back to Customers
        </button>

        <div style={{ marginBottom: "28px" }}>
          <span
            style={{
              color: "#7053e8",
              fontSize: "12px",
              fontWeight: 700,
              letterSpacing: "2px"
            }}
          >
            CUSTOMER MANAGEMENT
          </span>

          <h1
            style={{
              fontSize: "clamp(26px, 3vw, 34px)",
              margin: "10px 0",
              color: "#252433"
            }}
          >
            Customer Details
          </h1>

          <p style={{ color: "#858391", margin: 0 }}>
            Complete customer profile and purchase history.
          </p>
        </div>

        <section style={cardStyle}>
          <h2 style={headingStyle}>Personal Information</h2>

          <div style={summaryGridStyle}>
            <div>
              <span style={labelStyle}>Customer ID</span>
              <div style={valueStyle}>#{customer.id}</div>
            </div>

            <div>
              <span style={labelStyle}>Full Name</span>
              <div style={valueStyle}>{customer.fullName || "-"}</div>
            </div>

            <div>
              <span style={labelStyle}>Email Address</span>
              <div style={valueStyle}>{customer.email || "-"}</div>
            </div>

            <div>
              <span style={labelStyle}>Phone Number</span>
              <div style={valueStyle}>{customer.phoneNumber || "-"}</div>
            </div>

            <div>
              <span style={labelStyle}>Joined Date</span>
              <div style={valueStyle}>
                {formatDate(customer.createdAt)}
              </div>
            </div>

            <div>
              <span style={labelStyle}>Account Status</span>
              <span
                style={{
                  display: "inline-block",
                  padding: "6px 12px",
                  borderRadius: "20px",
                  fontSize: "12px",
                  fontWeight: 700,
                  background: customer.active ? "#e4f8ed" : "#ffe9e9",
                  color: customer.active ? "#087443" : "#c62828"
                }}
              >
                {customer.active ? "Active" : "Inactive"}
              </span>
            </div>
          </div>
        </section>

        <section style={cardStyle}>
          <h2 style={headingStyle}>Purchase Summary</h2>

          <div style={summaryGridStyle}>
            <div>
              <span style={labelStyle}>Total Orders</span>
              <div style={{ ...valueStyle, fontSize: "25px" }}>
                {customer.totalOrders ?? 0}
              </div>
            </div>

            <div>
              <span style={labelStyle}>Total Amount Spent</span>
              <div
                style={{
                  ...valueStyle,
                  fontSize: "25px",
                  color: "#7053e8"
                }}
              >
                {formatCurrency(customer.totalSpent)}
              </div>
            </div>
          </div>
        </section>

        <section style={cardStyle}>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              gap: "12px",
              flexWrap: "wrap",
              marginBottom: "20px"
            }}
          >
            <h2 style={{ ...headingStyle, margin: 0 }}>
              Order History
            </h2>

            <span style={{ color: "#858391", fontSize: "13px" }}>
              {customer.orders?.length || 0} orders
            </span>
          </div>

          {!customer.orders || customer.orders.length === 0 ? (
            <p style={{ color: "#858391" }}>
              This customer has not placed any orders.
            </p>
          ) : (
            <div style={{ display: "grid", gap: "18px" }}>
              {customer.orders.map((order) => (
                <div
                  key={order.orderId}
                  style={{
                    border: "1px solid #eeeef2",
                    borderRadius: "12px",
                    padding: "18px"
                  }}
                >
                  <div
                    style={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "center",
                      gap: "12px",
                      flexWrap: "wrap",
                      marginBottom: "14px"
                    }}
                  >
                    <div>
                      <strong>Order #{order.orderId}</strong>
                      <p
                        style={{
                          color: "#858391",
                          fontSize: "12px",
                          margin: "6px 0 0"
                        }}
                      >
                        {formatDate(order.orderDate)}
                      </p>
                    </div>

                    <span
                      style={{
                        ...statusStyle(order.status),
                        padding: "7px 12px",
                        borderRadius: "20px",
                        fontSize: "11px",
                        fontWeight: 700
                      }}
                    >
                      {(order.status || "").replaceAll("_", " ")}
                    </span>
                  </div>

                  <div
                    style={{
                      display: "grid",
                      gridTemplateColumns: "repeat(auto-fit, minmax(170px, 1fr))",
                      gap: "12px",
                      marginBottom: "16px"
                    }}
                  >
                    <div>
                      <span style={labelStyle}>Order Amount</span>
                      <strong>{formatCurrency(order.totalAmount)}</strong>
                    </div>

                    <div>
                      <span style={labelStyle}>Coupon Code</span>
                      <strong>{order.couponCode || "Not Applied"}</strong>
                    </div>
                  </div>

                  <div style={{ marginBottom: "16px" }}>
                    <span style={labelStyle}>Shipping Address</span>
                    <div style={{ fontSize: "13px", lineHeight: 1.6 }}>
                      {order.shippingAddress || "-"}
                    </div>
                  </div>

                  <div
                    style={{
                      borderTop: "1px solid #eeeef2",
                      paddingTop: "14px"
                    }}
                  >
                    <strong style={{ fontSize: "13px" }}>
                      Ordered Products
                    </strong>

                    <div style={{ display: "grid", gap: "12px", marginTop: "12px" }}>
                      {(order.items || []).map((item, index) => (
                        <div
                          key={`${item.productId}-${index}`}
                          style={{
                            display: "flex",
                            alignItems: "center",
                            gap: "14px",
                            flexWrap: "wrap"
                          }}
                        >
                          {item.imageUrl && (
                            <img
                              src={item.imageUrl}
                              alt={item.productName || "Product"}
                              onError={(e) => {
                                e.currentTarget.style.display = "none";
                              }}
                              style={{
                                width: "58px",
                                height: "58px",
                                objectFit: "contain",
                                border: "1px solid #eeeef2",
                                borderRadius: "8px",
                                padding: "4px"
                              }}
                            />
                          )}

                          <div style={{ flex: 1, minWidth: "150px" }}>
                            <strong style={{ fontSize: "13px" }}>
                              {item.productName || "Product"}
                            </strong>

                            <p
                              style={{
                                margin: "5px 0 0",
                                color: "#858391",
                                fontSize: "12px"
                              }}
                            >
                              SKU: {item.sku || "-"}
                            </p>

                            <p
                              style={{
                                margin: "4px 0 0",
                                color: "#858391",
                                fontSize: "12px"
                              }}
                            >
                              Quantity: {item.quantity}
                            </p>
                          </div>

                          <div style={{ fontSize: "13px", fontWeight: 700 }}>
                            {formatCurrency(item.subtotal)}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </main>

      <Footer />
    </>
  );
}

export default CustomerDetails;
