import { useEffect, useMemo, useState } from "react";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import api from "../services/api";
import "../styles/admin-orders.css";

const PAGE_SIZE = 10;

const nextStatuses = {
  CONFIRMED: "PROCESSING",
  PROCESSING: "SHIPPED",
  SHIPPED: "OUT_FOR_DELIVERY",
  OUT_FOR_DELIVERY: "DELIVERED"
};

function AdminOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [updatingId, setUpdatingId] = useState(null);

  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [currentPage, setCurrentPage] = useState(1);

  const [selectedOrder, setSelectedOrder] = useState(null);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [detailsError, setDetailsError] = useState("");

  const fetchOrders = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/admin/orders");

      setOrders(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error("Admin orders error:", err);

      setError(
        err.response?.data?.message || "Unable to load orders."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  useEffect(() => {
    setCurrentPage(1);
  }, [search, statusFilter]);

  const updateStatus = async (orderId, currentStatus) => {
    const nextStatus = nextStatuses[currentStatus];

    if (!nextStatus) return;

    const confirmed = window.confirm(
      `Change order #${orderId} from ${currentStatus} to ${nextStatus}?`
    );

    if (!confirmed) return;

    try {
      setUpdatingId(orderId);

      await api.put(
        `/admin/orders/${orderId}/status`,
        null,
        { params: { status: nextStatus } }
      );

      await fetchOrders();
    } catch (err) {
      alert(
        err.response?.data?.message ||
        "Unable to update order status."
      );
    } finally {
      setUpdatingId(null);
    }
  };

  const viewOrderDetails = async (orderId) => {
    try {
      setDetailsLoading(true);
      setDetailsError("");
      setSelectedOrder(null);

      const response = await api.get(
        `/admin/orders/${orderId}`
      );

      setSelectedOrder(response.data);
    } catch (err) {
      setDetailsError(
        err.response?.data?.message ||
        "Unable to load order details."
      );
    } finally {
      setDetailsLoading(false);
    }
  };

  const closeOrderDetails = () => {
    setSelectedOrder(null);
    setDetailsError("");
    setDetailsLoading(false);
  };

  const formatCurrency = (amount) =>
    Number(amount || 0).toLocaleString("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2
    });

  const formatDate = (date) =>
    date ? new Date(date).toLocaleString("en-IN") : "-";

  const filteredOrders = useMemo(() => {
    const searchText = search.trim().toLowerCase();

    return orders.filter((order) => {
      const matchesSearch =
        String(order.orderId || "")
          .toLowerCase()
          .includes(searchText) ||
        (order.customerName || "")
          .toLowerCase()
          .includes(searchText) ||
        (order.customerEmail || "")
          .toLowerCase()
          .includes(searchText);

      const matchesStatus =
        statusFilter === "ALL" ||
        order.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [orders, search, statusFilter]);

  const totalPages = Math.ceil(
    filteredOrders.length / PAGE_SIZE
  );

  useEffect(() => {
    if (totalPages === 0) {
      setCurrentPage(1);
    } else if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);

  const startIndex = (currentPage - 1) * PAGE_SIZE;

  const endIndex = Math.min(
    startIndex + PAGE_SIZE,
    filteredOrders.length
  );

  const paginatedOrders = filteredOrders.slice(
    startIndex,
    endIndex
  );

  const inProgressCount = orders.filter((order) =>
    ["PROCESSING", "SHIPPED", "OUT_FOR_DELIVERY"].includes(
      order.status
    )
  ).length;

  const deliveredCount = orders.filter(
    (order) => order.status === "DELIVERED"
  ).length;

  // Calculate the product subtotal using the saved order items.
  const calculateSubtotal = (items = []) =>
    items.reduce((total, item) => {
      const itemSubtotal =
        item.subtotal != null
          ? Number(item.subtotal)
          : Number(item.unitPrice || 0) *
            Number(item.quantity || 0);

      return total + itemSubtotal;
    }, 0);

  const calculateCouponDiscount = (order) => {
    if (!order?.couponCode) return 0;

    const subtotal = calculateSubtotal(order.items || []);
    const totalAmount = Number(order.totalAmount || 0);

    return Math.max(0, subtotal - totalAmount);
  };

  return (
    <>
      <Navbar />

      <main className="admin-orders-page">

        <div className="admin-orders-header">
          <div>
            <span className="eyebrow">ADMINISTRATION</span>
            <h1>Order Management</h1>
            <p>View orders and manage their delivery progress.</p>
          </div>

          <button
            onClick={fetchOrders}
            disabled={loading}
          >
            {loading ? "Refreshing..." : "Refresh Orders"}
          </button>
        </div>

        <div className="admin-orders-summary">
          <div>
            <span>Total Orders</span>
            <strong>{orders.length}</strong>
          </div>

          <div>
            <span>In Progress</span>
            <strong>{inProgressCount}</strong>
          </div>

          <div>
            <span>Delivered</span>
            <strong>{deliveredCount}</strong>
          </div>
        </div>

        {error && (
          <div className="admin-orders-error">
            {error}
            <button onClick={fetchOrders}>Retry</button>
          </div>
        )}

        {!error && (
          <>
            <section className="admin-orders-toolbar">

              <input
                type="search"
                placeholder="Search by order ID, customer name or email..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />

              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="ALL">All Statuses</option>
                <option value="PENDING">Pending</option>
                <option value="CONFIRMED">Confirmed</option>
                <option value="PROCESSING">Processing</option>
                <option value="SHIPPED">Shipped</option>
                <option value="OUT_FOR_DELIVERY">
                  Out for Delivery
                </option>
                <option value="DELIVERED">Delivered</option>
                <option value="CANCELLED">Cancelled</option>
                <option value="REFUNDED">Refunded</option>
              </select>

            </section>

            <section className="admin-orders-table-card">

              <div className="admin-orders-table-heading">
                <h2>All Orders</h2>
                <span>{filteredOrders.length} records</span>
              </div>

              {loading ? (
                <p>Loading orders...</p>
              ) : filteredOrders.length === 0 ? (
                <p>No orders found.</p>
              ) : (
                <>
                  <div className="admin-orders-table-scroll">
                    <table className="admin-orders-table">

                      <thead>
                        <tr>
                          <th>Order</th>
                          <th>Customer</th>
                          <th>Amount</th>
                          <th>Payment</th>
                          <th>Order Status</th>
                          <th>Date</th>
                          <th>Actions</th>
                        </tr>
                      </thead>

                      <tbody>
                        {paginatedOrders.map((order) => {
                          const nextStatus =
                            nextStatuses[order.status];

                          return (
                            <tr key={order.orderId}>

                              <td>#{order.orderId}</td>

                              <td>
                                <strong>{order.customerName}</strong>
                                <small>{order.customerEmail}</small>
                              </td>

                              <td>
                                {formatCurrency(order.totalAmount)}
                              </td>

                              <td>
                                <strong>
                                  {order.paymentMethod || "-"}
                                </strong>
                                <small>
                                  {order.paymentStatus || "-"}
                                </small>
                              </td>

                              <td>
                                <span
                                  className={`admin-order-status ${order.status?.toLowerCase()}`}
                                >
                                  {order.status?.replaceAll("_", " ")}
                                </span>
                              </td>

                              <td>{formatDate(order.orderDate)}</td>

                              <td>
                                <div className="admin-order-actions">

                                  <button
                                    type="button"
                                    className="admin-order-details-btn"
                                    onClick={() =>
                                      viewOrderDetails(order.orderId)
                                    }
                                  >
                                    View Details
                                  </button>

                                  {nextStatus ? (
                                    <button
                                      className="admin-order-update-btn"
                                      disabled={
                                        updatingId === order.orderId
                                      }
                                      onClick={() =>
                                        updateStatus(
                                          order.orderId,
                                          order.status
                                        )
                                      }
                                    >
                                      {updatingId === order.orderId
                                        ? "Updating..."
                                        : `Mark ${nextStatus.replaceAll(
                                            "_",
                                            " "
                                          )}`}
                                    </button>
                                  ) : (
                                    <span className="admin-order-no-action">
                                      {order.status === "DELIVERED"
                                        ? "Completed"
                                        : "No action"}
                                    </span>
                                  )}

                                </div>
                              </td>

                            </tr>
                          );
                        })}
                      </tbody>

                    </table>
                  </div>

                  <div className="admin-orders-pagination">

                    <span className="admin-orders-pagination-info">
                      Showing {startIndex + 1}–{endIndex} of{" "}
                      {filteredOrders.length} orders
                    </span>

                    <div className="admin-orders-pagination-controls">

                      <button
                        onClick={() =>
                          setCurrentPage((page) => page - 1)
                        }
                        disabled={currentPage === 1}
                      >
                        Previous
                      </button>

                      {Array.from(
                        { length: totalPages },
                        (_, index) => index + 1
                      ).map((page) => (
                        <button
                          key={page}
                          className={
                            currentPage === page ? "selected" : ""
                          }
                          onClick={() => setCurrentPage(page)}
                          aria-current={
                            currentPage === page ? "page" : undefined
                          }
                        >
                          {page}
                        </button>
                      ))}

                      <button
                        onClick={() =>
                          setCurrentPage((page) => page + 1)
                        }
                        disabled={
                          totalPages === 0 ||
                          currentPage === totalPages
                        }
                      >
                        Next
                      </button>

                    </div>
                  </div>
                </>
              )}

            </section>
          </>
        )}

      </main>

      {(detailsLoading || detailsError || selectedOrder) && (
        <div
          className="admin-order-modal-overlay"
          onClick={(e) => {
            if (e.target === e.currentTarget) {
              closeOrderDetails();
            }
          }}
        >
          <div
            className="admin-order-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="admin-order-modal-title"
          >

            <div className="admin-order-modal-header">
              <h2 id="admin-order-modal-title">
                {selectedOrder
                  ? `Order #${selectedOrder.orderId}`
                  : "Order Details"}
              </h2>

              <button
                type="button"
                onClick={closeOrderDetails}
                className="admin-order-modal-close"
                aria-label="Close order details"
              >
                ×
              </button>
            </div>

            {detailsLoading && (
              <p>Loading order details...</p>
            )}

            {detailsError && (
              <div className="admin-orders-error">
                {detailsError}
              </div>
            )}

            {selectedOrder && (
              <>
                <section className="admin-order-detail-section">
                  <h3>Customer Information</h3>

                  <p>
                    <strong>Name:</strong>{" "}
                    {selectedOrder.customerName}
                  </p>

                  <p>
                    <strong>Email:</strong>{" "}
                    {selectedOrder.customerEmail}
                  </p>

                  <p>
                    <strong>Order Date:</strong>{" "}
                    {formatDate(selectedOrder.orderDate)}
                  </p>

                  <p>
                    <strong>Status:</strong>{" "}
                    {selectedOrder.status?.replaceAll("_", " ")}
                  </p>
                </section>

                <section className="admin-order-detail-section">
                  <h3>Shipping Information</h3>

                  <p>
                    {selectedOrder.shippingAddress || "Not available"}
                  </p>

                  <p>
                    <strong>Coupon:</strong>{" "}
                    {selectedOrder.couponCode || "No coupon applied"}
                  </p>
                </section>

                <section className="admin-order-detail-section">
                  <h3>Products Ordered</h3>

                  <div className="admin-order-items">
                    {selectedOrder.items?.map((item) => (
                      <div
                        className="admin-order-item"
                        key={item.itemId}
                      >
                        {item.imageUrl && (
                          <img
                            src={item.imageUrl}
                            alt={item.productName}
                          />
                        )}

                        <div className="admin-order-item-info">
                          <strong>{item.productName}</strong>
                          <small>SKU: {item.sku}</small>
                          <small>Quantity: {item.quantity}</small>
                          <small>
                            Unit Price:{" "}
                            {formatCurrency(item.unitPrice)}
                          </small>
                        </div>

                        <strong>
                          {formatCurrency(
                            item.subtotal ??
                            Number(item.unitPrice || 0) *
                              Number(item.quantity || 0)
                          )}
                        </strong>
                      </div>
                    ))}
                  </div>

                  <div
                    className="admin-order-price-breakdown"
                    style={{
                      marginTop: "24px",
                      marginLeft: "auto",
                      maxWidth: "380px",
                      padding: "18px",
                      border: "1px solid #e8e5f2",
                      borderRadius: "12px"
                    }}
                  >
                    <h3 style={{ marginBottom: "16px" }}>
                      Price Breakdown
                    </h3>

                    <div
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        gap: "16px",
                        marginBottom: "12px"
                      }}
                    >
                      <span>Product Subtotal</span>
                      <strong>
                        {formatCurrency(
                          calculateSubtotal(selectedOrder.items || [])
                        )}
                      </strong>
                    </div>

                    <div
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        gap: "16px",
                        marginBottom: "12px"
                      }}
                    >
                      <span>
                        Coupon Discount
                        {selectedOrder.couponCode
                          ? ` (${selectedOrder.couponCode})`
                          : ""}
                      </span>

                      <strong style={{ color: "#16a34a" }}>
                        -{formatCurrency(
                          calculateCouponDiscount(selectedOrder)
                        )}
                      </strong>
                    </div>

                    <div
                      style={{
                        borderTop: "1px solid #e8e5f2",
                        paddingTop: "14px",
                        marginTop: "14px",
                        display: "flex",
                        justifyContent: "space-between",
                        gap: "16px",
                        fontSize: "18px"
                      }}
                    >
                      <strong>Final Amount</strong>
                      <strong>
                        {formatCurrency(selectedOrder.totalAmount)}
                      </strong>
                    </div>
                  </div>
                </section>

                <section className="admin-order-detail-section">
                  <h3>Payment Information</h3>

                  <p>
                    <strong>Method:</strong>{" "}
                    {selectedOrder.paymentMethod || "-"}
                  </p>

                  <p>
                    <strong>Status:</strong>{" "}
                    {selectedOrder.paymentStatus || "-"}
                  </p>

                  <p>
                    <strong>Transaction ID:</strong>{" "}
                    {selectedOrder.transactionId || "-"}
                  </p>

                  <p>
                    <strong>Payment Date:</strong>{" "}
                    {formatDate(selectedOrder.paymentDate)}
                  </p>
                </section>
              </>
            )}

          </div>
        </div>
      )}

      <Footer />
    </>
  );
}

export default AdminOrders;