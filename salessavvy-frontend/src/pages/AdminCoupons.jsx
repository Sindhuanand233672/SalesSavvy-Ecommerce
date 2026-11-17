
import { useEffect, useMemo, useState } from "react";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import api from "../services/api";
import "../styles/admin-coupons.css";

const emptyForm = {
  code: "",
  description: "",
  discountType: "PERCENTAGE",
  discountValue: "",
  minimumOrderAmount: "0",
  maximumDiscountAmount: "",
  startDate: "",
  expiryDate: "",
  usageLimit: "100",
};

function getErrorMessage(error, fallback) {
  const data = error.response?.data;

  if (typeof data === "string") return data;
  if (data?.message) return data.message;
  if (data?.detail) return data.detail;

  return fallback;
}

function formatDate(value) {
  if (!value) return "—";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) return value;

  return date.toLocaleString();
}

function AdminCoupons() {
  const [coupons, setCoupons] = useState([]);
  const [form, setForm] = useState({ ...emptyForm });
  const [editingId, setEditingId] = useState(null);

  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [actionId, setActionId] = useState(null);

  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("success");

  const showMessage = (text, type = "success") => {
    setMessage(text);
    setMessageType(type);
  };

  const fetchCoupons = async () => {
    try {
      setLoading(true);

      const response = await api.get("/coupons");

      setCoupons(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      showMessage(
        getErrorMessage(error, "Unable to load coupons."),
        "error"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCoupons();
  }, []);

  const activeCount = coupons.filter(
    (coupon) => coupon.active === true
  ).length;

  const inactiveCount = coupons.filter(
    (coupon) => coupon.active === false
  ).length;

  const filteredCoupons = useMemo(() => {
    const searchText = search.trim().toLowerCase();

    return coupons.filter((coupon) => {
      const code = (coupon.code || "").toLowerCase();
      const description = (coupon.description || "").toLowerCase();

      const matchesSearch =
        code.includes(searchText) ||
        description.includes(searchText);

      const matchesStatus =
        statusFilter === "ALL" ||
        (statusFilter === "ACTIVE" && coupon.active === true) ||
        (statusFilter === "INACTIVE" && coupon.active === false);

      return matchesSearch && matchesStatus;
    });
  }, [coupons, search, statusFilter]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: name === "code" ? value.toUpperCase() : value,
    }));
  };

  const openAddForm = () => {
    setEditingId(null);
    setForm({ ...emptyForm });
    setShowForm(true);
    setMessage("");
  };

  const openEditForm = (coupon) => {
    setEditingId(coupon.id);

    setForm({
      code: coupon.code || "",
      description: coupon.description || "",
      discountType: coupon.discountType || "PERCENTAGE",
      discountValue: String(coupon.discountValue ?? ""),
      minimumOrderAmount: String(coupon.minimumOrderAmount ?? "0"),
      maximumDiscountAmount:
        coupon.maximumDiscountAmount == null
          ? ""
          : String(coupon.maximumDiscountAmount),
      startDate: coupon.startDate
        ? coupon.startDate.substring(0, 16)
        : "",
      expiryDate: coupon.expiryDate
        ? coupon.expiryDate.substring(0, 16)
        : "",
      usageLimit: String(coupon.usageLimit ?? "100"),
    });

    setShowForm(true);
    setMessage("");

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingId(null);
    setForm({ ...emptyForm });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const code = form.code.trim().toUpperCase();
    const description = form.description.trim();
    const discountValue = Number(form.discountValue);
    const minimumOrderAmount = Number(form.minimumOrderAmount);
    const maximumDiscountAmount =
      form.maximumDiscountAmount.trim() === ""
        ? null
        : Number(form.maximumDiscountAmount);
    const usageLimit = Number(form.usageLimit);

    if (!code || !description) {
      showMessage("Please enter the coupon code and description.", "error");
      return;
    }

    if (!Number.isFinite(discountValue) || discountValue <= 0) {
      showMessage("Discount value must be greater than zero.", "error");
      return;
    }

    if (
      form.discountType === "PERCENTAGE" &&
      discountValue > 100
    ) {
      showMessage("Percentage discount cannot exceed 100%.", "error");
      return;
    }

    if (
      !Number.isFinite(minimumOrderAmount) ||
      minimumOrderAmount < 0
    ) {
      showMessage("Minimum order amount cannot be negative.", "error");
      return;
    }

    if (
      maximumDiscountAmount !== null &&
      (!Number.isFinite(maximumDiscountAmount) ||
        maximumDiscountAmount <= 0)
    ) {
      showMessage("Maximum discount must be greater than zero.", "error");
      return;
    }

    if (
      !form.startDate ||
      !form.expiryDate ||
      new Date(form.expiryDate) <= new Date(form.startDate)
    ) {
      showMessage("Expiry date must be after the start date.", "error");
      return;
    }

    if (!Number.isInteger(usageLimit) || usageLimit < 1) {
      showMessage("Usage limit must be at least 1.", "error");
      return;
    }

    const payload = {
      code,
      description,
      discountType: form.discountType,
      discountValue,
      minimumOrderAmount,
      maximumDiscountAmount,
      startDate: form.startDate,
      expiryDate: form.expiryDate,
      usageLimit,
    };

    const isEditing = editingId !== null;

    try {
      setSaving(true);
      setMessage("");

      if (isEditing) {
        await api.put(`/coupons/${editingId}`, payload);
      } else {
        await api.post("/coupons", payload);
      }

      closeForm();
      await fetchCoupons();

      showMessage(
        isEditing
          ? "Coupon updated successfully."
          : "Coupon created successfully."
      );
    } catch (error) {
      showMessage(
        getErrorMessage(error, "Unable to save coupon."),
        "error"
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDeactivate = async (coupon) => {
    const confirmed = window.confirm(
      `Are you sure you want to deactivate coupon "${coupon.code}"?`
    );

    if (!confirmed) return;

    try {
      setActionId(coupon.id);

      await api.delete(`/coupons/${coupon.id}`);
      await fetchCoupons();

      showMessage("Coupon deactivated successfully.");
    } catch (error) {
      showMessage(
        getErrorMessage(error, "Unable to deactivate coupon."),
        "error"
      );
    } finally {
      setActionId(null);
    }
  };

  const handleActivate = async (coupon) => {
    try {
      setActionId(coupon.id);

      await api.put(`/coupons/${coupon.id}/activate`);
      await fetchCoupons();

      showMessage("Coupon activated successfully.");
    } catch (error) {
      showMessage(
        getErrorMessage(error, "Unable to activate coupon."),
        "error"
      );
    } finally {
      setActionId(null);
    }
  };

  const formatDiscount = (coupon) => {
    if (coupon.discountType === "PERCENTAGE") {
      return `${coupon.discountValue}%`;
    }

    return `₹${Number(coupon.discountValue).toLocaleString("en-IN")}`;
  };

  return (
    <>
      <Navbar />

      <main className="admin-coupons-page">
        <div className="admin-coupons-container">

          <div className="coupons-heading">
            <div>
              <span className="coupons-eyebrow">
                ADMINISTRATION
              </span>

              <h1>Coupon Management</h1>

              <p>
                Create, update and manage your store discount coupons.
              </p>
            </div>

            <button
              type="button"
              className="coupon-primary-btn"
              onClick={openAddForm}
            >
              + Add Coupon
            </button>
          </div>

          {message && (
            <div
              className={`coupon-alert ${messageType}`}
              role="alert"
            >
              <span>{message}</span>

              <button
                type="button"
                onClick={() => setMessage("")}
                aria-label="Close message"
              >
                ×
              </button>
            </div>
          )}

          <section className="coupon-stats">
            <div className="coupon-stat-card">
              <span>Total Coupons</span>
              <strong>{coupons.length}</strong>
            </div>

            <div className="coupon-stat-card">
              <span>Active Coupons</span>
              <strong>{activeCount}</strong>
            </div>

            <div className="coupon-stat-card">
              <span>Inactive Coupons</span>
              <strong>{inactiveCount}</strong>
            </div>
          </section>

          {showForm && (
            <section className="coupon-form-card">
              <div className="coupon-form-heading">
                <h2>
                  {editingId !== null ? "Edit Coupon" : "Create New Coupon"}
                </h2>

                <button
                  type="button"
                  className="coupon-close-btn"
                  onClick={closeForm}
                  aria-label="Close form"
                  disabled={saving}
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleSubmit}>
                <div className="coupon-form-grid">

                  <div className="coupon-field">
                    <label htmlFor="couponCode">Coupon Code *</label>
                    <input
                      id="couponCode"
                      name="code"
                      type="text"
                      value={form.code}
                      onChange={handleChange}
                      placeholder="Example: SAVE20"
                      maxLength={50}
                      required
                    />
                  </div>

                  <div className="coupon-field">
                    <label htmlFor="couponDescription">
                      Description *
                    </label>
                    <input
                      id="couponDescription"
                      name="description"
                      type="text"
                      value={form.description}
                      onChange={handleChange}
                      placeholder="Describe this offer"
                      maxLength={255}
                      required
                    />
                  </div>

                  <div className="coupon-field">
                    <label htmlFor="discountType">Discount Type *</label>
                    <select
                      id="discountType"
                      name="discountType"
                      value={form.discountType}
                      onChange={handleChange}
                      required
                    >
                      <option value="PERCENTAGE">Percentage (%)</option>
                      <option value="FIXED">Fixed Amount (₹)</option>
                    </select>
                  </div>

                  <div className="coupon-field">
                    <label htmlFor="discountValue">
                      Discount Value *
                    </label>
                    <input
                      id="discountValue"
                      name="discountValue"
                      type="number"
                      min="0.01"
                      step="0.01"
                      value={form.discountValue}
                      onChange={handleChange}
                      placeholder={
                        form.discountType === "PERCENTAGE"
                          ? "Example: 20"
                          : "Example: 200"
                      }
                      required
                    />
                  </div>

                  <div className="coupon-field">
                    <label htmlFor="minimumOrderAmount">
                      Minimum Order Amount (₹) *
                    </label>
                    <input
                      id="minimumOrderAmount"
                      name="minimumOrderAmount"
                      type="number"
                      min="0"
                      step="0.01"
                      value={form.minimumOrderAmount}
                      onChange={handleChange}
                      required
                    />
                  </div>

                  <div className="coupon-field">
                    <label htmlFor="maximumDiscountAmount">
                      Maximum Discount (₹)
                    </label>
                    <input
                      id="maximumDiscountAmount"
                      name="maximumDiscountAmount"
                      type="number"
                      min="0.01"
                      step="0.01"
                      value={form.maximumDiscountAmount}
                      onChange={handleChange}
                      placeholder="Optional"
                    />
                  </div>

                  <div className="coupon-field">
                    <label htmlFor="startDate">Start Date *</label>
                    <input
                      id="startDate"
                      name="startDate"
                      type="datetime-local"
                      value={form.startDate}
                      onChange={handleChange}
                      required
                    />
                  </div>

                  <div className="coupon-field">
                    <label htmlFor="expiryDate">Expiry Date *</label>
                    <input
                      id="expiryDate"
                      name="expiryDate"
                      type="datetime-local"
                      value={form.expiryDate}
                      onChange={handleChange}
                      min={form.startDate || undefined}
                      required
                    />
                  </div>

                  <div className="coupon-field">
                    <label htmlFor="usageLimit">Usage Limit *</label>
                    <input
                      id="usageLimit"
                      name="usageLimit"
                      type="number"
                      min="1"
                      step="1"
                      value={form.usageLimit}
                      onChange={handleChange}
                      required
                    />
                  </div>

                </div>

                <div className="coupon-form-actions">
                  <button
                    type="button"
                    className="coupon-cancel-btn"
                    onClick={closeForm}
                    disabled={saving}
                  >
                    Cancel
                  </button>

                  <button
                    type="submit"
                    className="coupon-primary-btn"
                    disabled={saving}
                  >
                    {saving
                      ? "Saving..."
                      : editingId !== null
                        ? "Update Coupon"
                        : "Create Coupon"}
                  </button>
                </div>
              </form>
            </section>
          )}

          <section className="coupon-toolbar">
            <input
              type="text"
              placeholder="Search by coupon code or description..."
              value={search}
              onChange={(event) => setSearch(event.target.value)}
            />

            <select
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
            >
              <option value="ALL">All Coupons</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </select>

            <button
              type="button"
              className="coupon-refresh-btn"
              onClick={fetchCoupons}
              disabled={loading}
            >
              {loading ? "Loading..." : "Refresh"}
            </button>
          </section>

          <section className="coupon-table-card">
            <h2>All Coupons ({filteredCoupons.length})</h2>

            {loading ? (
              <div className="coupon-empty-state">
                Loading coupons...
              </div>
            ) : filteredCoupons.length === 0 ? (
              <div className="coupon-empty-state">
                No coupons found.
              </div>
            ) : (
              <div className="coupon-table-wrapper">
                <table className="coupon-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Coupon Code</th>
                      <th>Description</th>
                      <th>Discount</th>
                      <th>Min. Order</th>
                      <th>Usage</th>
                      <th>Start Date</th>
                      <th>Expiry Date</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>

                  <tbody>
                    {filteredCoupons.map((coupon) => (
                      <tr key={coupon.id}>
                        <td>#{coupon.id}</td>

                        <td className="coupon-code-cell">
                          {coupon.code}
                        </td>

                        <td className="coupon-description-cell">
                          {coupon.description || "—"}
                        </td>

                        <td className="coupon-discount-cell">
                          {formatDiscount(coupon)}
                        </td>

                        <td>
                          ₹{Number(
                            coupon.minimumOrderAmount || 0
                          ).toLocaleString("en-IN")}
                        </td>

                        <td>
                          {coupon.usageCount ?? 0} / {coupon.usageLimit}
                        </td>

                        <td>{formatDate(coupon.startDate)}</td>

                        <td>{formatDate(coupon.expiryDate)}</td>

                        <td>
                          <span
                            className={`coupon-status ${
                              coupon.active ? "active" : "inactive"
                            }`}
                          >
                            {coupon.active ? "Active" : "Inactive"}
                          </span>
                        </td>

                        <td>
                          <div className="coupon-action-buttons">
                            <button
                              type="button"
                              className="coupon-edit-btn"
                              onClick={() => openEditForm(coupon)}
                              disabled={actionId === coupon.id}
                            >
                              Edit
                            </button>

                            {coupon.active ? (
                              <button
                                type="button"
                                className="coupon-deactivate-btn"
                                onClick={() => handleDeactivate(coupon)}
                                disabled={actionId === coupon.id}
                              >
                                {actionId === coupon.id
                                  ? "Please wait..."
                                  : "Deactivate"}
                              </button>
                            ) : (
                              <button
                                type="button"
                                className="coupon-activate-btn"
                                onClick={() => handleActivate(coupon)}
                                disabled={actionId === coupon.id}
                              >
                                {actionId === coupon.id
                                  ? "Please wait..."
                                  : "Activate"}
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

        </div>
      </main>

      <Footer />
    </>
  );
}

export default AdminCoupons;
