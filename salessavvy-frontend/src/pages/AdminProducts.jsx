
import { useEffect, useMemo, useState } from "react";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import api from "../services/api";
import "../styles/admin-products.css";

const PAGE_SIZE = 10;

const emptyForm = {
  name: "",
  sku: "",
  description: "",
  price: "",
  stockQuantity: "",
  imageUrl: "",
  categoryId: ""
};

function AdminProducts() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState(emptyForm);

  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [processingId, setProcessingId] = useState(null);

  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  // Pagination state
  const [currentPage, setCurrentPage] = useState(1);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError("");

      const [productResponse, categoryResponse] = await Promise.all([
        api.get("/products/admin/all"),
        api.get("/categories")
      ]);

      setProducts(
        Array.isArray(productResponse.data)
          ? productResponse.data
          : []
      );

      setCategories(
        Array.isArray(categoryResponse.data)
          ? categoryResponse.data
          : []
      );
    } catch (err) {
      console.error("Admin products loading error:", err);
      setError(
        err.response?.data?.message ||
        "Unable to load products. Please check your connection."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Reset pagination when search or filter changes
  useEffect(() => {
    setCurrentPage(1);
  }, [search, statusFilter]);

  const openAddForm = () => {
    setEditingId(null);
    setForm(emptyForm);
    setMessage("");
    setError("");
    setShowForm(true);
  };

  const openEditForm = (product) => {
    setEditingId(product.id);

    setForm({
      name: product.name || "",
      sku: product.sku || "",
      description: product.description || "",
      price: product.price ?? "",
      stockQuantity: product.stockQuantity ?? "",
      imageUrl: product.imageUrl || "",
      categoryId: product.category?.id?.toString() || ""
    });

    setMessage("");
    setError("");
    setShowForm(true);
  };

  const closeForm = () => {
    if (saving) return;

    setShowForm(false);
    setEditingId(null);
    setForm(emptyForm);
    setError("");
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((previous) => ({
      ...previous,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const price = Number(form.price);
    const stockQuantity = Number(form.stockQuantity);

    if (!form.name.trim() || !form.sku.trim() || !form.categoryId) {
      setError("Please complete all required fields.");
      return;
    }

    if (!Number.isFinite(price) || price <= 0) {
      setError("Price must be greater than zero.");
      return;
    }

    if (!Number.isInteger(stockQuantity) || stockQuantity < 0) {
      setError("Stock quantity must be a non-negative whole number.");
      return;
    }

    const payload = {
      name: form.name.trim(),
      sku: form.sku.trim(),
      description: form.description.trim(),
      price,
      stockQuantity,
      imageUrl: form.imageUrl.trim(),
      category: {
        id: Number(form.categoryId)
      }
    };

    try {
      setSaving(true);
      setError("");
      setMessage("");

      if (editingId !== null) {
        await api.put(`/products/${editingId}`, payload);
        setMessage("Product updated successfully.");
      } else {
        await api.post("/products", payload);
        setMessage("Product added successfully.");
        setCurrentPage(1);
      }

      setShowForm(false);
      setEditingId(null);
      setForm(emptyForm);

      await fetchData();
    } catch (err) {
      console.error("Product save error:", err);

      setError(
        err.response?.data?.message ||
        "Unable to save product. Check the details and try again."
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDeactivate = async (product) => {
    const confirmed = window.confirm(
      `Are you sure you want to deactivate "${product.name}"?`
    );

    if (!confirmed) return;

    try {
      setProcessingId(product.id);
      setError("");
      setMessage("");

      await api.delete(`/products/${product.id}`);

      setMessage("Product deactivated successfully.");
      await fetchData();
    } catch (err) {
      console.error("Product deactivation error:", err);

      setError(
        err.response?.data?.message ||
        "Unable to deactivate product."
      );
    } finally {
      setProcessingId(null);
    }
  };

  const handleActivate = async (product) => {
    const confirmed = window.confirm(
      `Are you sure you want to activate "${product.name}"?`
    );

    if (!confirmed) return;

    try {
      setProcessingId(product.id);
      setError("");
      setMessage("");

      await api.put(`/products/${product.id}/activate`);

      setMessage("Product activated successfully.");
      await fetchData();
    } catch (err) {
      console.error("Product activation error:", err);

      setError(
        err.response?.data?.message ||
        "Unable to activate product."
      );
    } finally {
      setProcessingId(null);
    }
  };

  const filteredProducts = useMemo(() => {
    const searchText = search.trim().toLowerCase();

    return products.filter((product) => {
      const matchesSearch =
        product.name?.toLowerCase().includes(searchText) ||
        product.sku?.toLowerCase().includes(searchText) ||
        product.category?.name?.toLowerCase().includes(searchText);

      const matchesStatus =
        statusFilter === "ALL" ||
        (statusFilter === "ACTIVE" && product.active === true) ||
        (statusFilter === "INACTIVE" && product.active === false);

      return matchesSearch && matchesStatus;
    });
  }, [products, search, statusFilter]);

  // Calculate pagination
  const totalPages = Math.ceil(filteredProducts.length / PAGE_SIZE);

  // Keep current page valid after product changes
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
    filteredProducts.length
  );

  const paginatedProducts = filteredProducts.slice(
    startIndex,
    endIndex
  );

  const formatCurrency = (amount) =>
    Number(amount || 0).toLocaleString("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2
    });

  const getStockStatus = (quantity) => {
    if (quantity === 0) {
      return { label: "Out of Stock", className: "out-of-stock" };
    }

    if (quantity <= 5) {
      return { label: "Low Stock", className: "low-stock" };
    }

    return { label: "In Stock", className: "in-stock" };
  };

  const activeCount = products.filter(
    (p) => p.active === true
  ).length;

  const inactiveCount = products.filter(
    (p) => p.active === false
  ).length;

  const outOfStockCount = products.filter(
    (p) => Number(p.stockQuantity) === 0
  ).length;

  return (
    <>
      <Navbar />

      <main className="admin-products">

        <div className="admin-products-header">
          <div>
            <span className="admin-products-eyebrow">
              ADMINISTRATION
            </span>

            <h1>Product Management</h1>

            <p>Add, update and manage your store products.</p>
          </div>

          <button
            className="admin-add-button"
            onClick={openAddForm}
          >
            + Add Product
          </button>
        </div>

        {message && (
          <div className="admin-success">
            {message}
          </div>
        )}

        {error && (
          <div className="admin-error">
            {error}
          </div>
        )}

        <section className="admin-product-summary">
          <div>
            <span>Total Products</span>
            <strong>{products.length}</strong>
          </div>

          <div>
            <span>Active Products</span>
            <strong>{activeCount}</strong>
          </div>

          <div>
            <span>Inactive Products</span>
            <strong>{inactiveCount}</strong>
          </div>

          <div>
            <span>Out of Stock</span>
            <strong>{outOfStockCount}</strong>
          </div>
        </section>

        <section className="admin-products-toolbar">
          <input
            type="search"
            placeholder="Search by product name, SKU or category..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="ALL">All Products</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>

          <button onClick={fetchData} disabled={loading}>
            {loading ? "Refreshing..." : "Refresh"}
          </button>
        </section>

        {showForm && (
          <section className="admin-product-form-section">

            <div className="admin-product-form-heading">
              <h2>
                {editingId !== null ? "Edit Product" : "Add New Product"}
              </h2>

              <button
                type="button"
                onClick={closeForm}
                disabled={saving}
                aria-label="Close form"
              >
                ✕
              </button>
            </div>

            <form
              onSubmit={handleSubmit}
              className="admin-product-form"
            >

              <label>
                Product Name *
                <input
                  name="name"
                  value={form.name}
                  onChange={handleChange}
                  required
                  maxLength={150}
                />
              </label>

              <label>
                SKU *
                <input
                  name="sku"
                  value={form.sku}
                  onChange={handleChange}
                  required
                  maxLength={50}
                />
              </label>

              <label>
                Price (₹) *
                <input
                  type="number"
                  name="price"
                  value={form.price}
                  onChange={handleChange}
                  min="0.01"
                  step="0.01"
                  required
                />
              </label>

              <label>
                Stock Quantity *
                <input
                  type="number"
                  name="stockQuantity"
                  value={form.stockQuantity}
                  onChange={handleChange}
                  min="0"
                  step="1"
                  required
                />
              </label>

              <label>
                Category *
                <select
                  name="categoryId"
                  value={form.categoryId}
                  onChange={handleChange}
                  required
                >
                  <option value="">Select Category</option>

                  {categories
                    .filter((category) => category.active !== false)
                    .map((category) => (
                      <option
                        key={category.id}
                        value={category.id}
                      >
                        {category.name}
                      </option>
                    ))}
                </select>
              </label>

              <label>
                Image URL
                <input
                  name="imageUrl"
                  type="url"
                  value={form.imageUrl}
                  onChange={handleChange}
                  maxLength={500}
                  placeholder="https://example.com/image.jpg"
                />
              </label>

              <label className="admin-product-description">
                Description
                <textarea
                  name="description"
                  value={form.description}
                  onChange={handleChange}
                  rows="3"
                  maxLength={2000}
                />
              </label>

              <div className="admin-product-form-actions">
                <button
                  type="button"
                  className="admin-cancel-button"
                  onClick={closeForm}
                  disabled={saving}
                >
                  Cancel
                </button>

                <button type="submit" disabled={saving}>
                  {saving
                    ? "Saving..."
                    : editingId !== null
                    ? "Update Product"
                    : "Add Product"}
                </button>
              </div>

            </form>
          </section>
        )}

        <section className="admin-products-table-section">

          <h2>
            All Products ({filteredProducts.length})
          </h2>

          {loading ? (
            <p>Loading products...</p>
          ) : filteredProducts.length === 0 ? (
            <p>No products found.</p>
          ) : (
            <>
              <div className="admin-products-table-wrapper">

                <table className="admin-products-table">

                  <thead>
                    <tr>
                      <th>Product</th>
                      <th>SKU</th>
                      <th>Category</th>
                      <th>Price</th>
                      <th>Stock</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>

                  <tbody>
                    {paginatedProducts.map((product) => {
                      const stockStatus = getStockStatus(
                        Number(product.stockQuantity || 0)
                      );

                      const isProcessing =
                        processingId === product.id;

                      return (
                        <tr key={product.id}>

                          <td>
                            <div className="admin-product-info">
                              {product.imageUrl ? (
                                <img
                                  src={product.imageUrl}
                                  alt={product.name}
                                  onError={(e) => {
                                    e.currentTarget.style.display = "none";
                                  }}
                                />
                              ) : (
                                <div className="admin-product-no-image">
                                  No image
                                </div>
                              )}

                              <strong>{product.name}</strong>
                            </div>
                          </td>

                          <td>{product.sku}</td>

                          <td>{product.category?.name || "-"}</td>

                          <td>{formatCurrency(product.price)}</td>

                          <td>
                            <strong>{product.stockQuantity}</strong>
                            <span className={`admin-stock-status ${stockStatus.className}`}>
                              {stockStatus.label}
                            </span>
                          </td>

                          <td>
                            <span
                              className={`admin-product-status ${
                                product.active ? "active" : "inactive"
                              }`}
                            >
                              {product.active ? "Active" : "Inactive"}
                            </span>
                          </td>

                          <td>
                            <div className="admin-product-actions">

                              <button
                                onClick={() => openEditForm(product)}
                              >
                                Edit
                              </button>

                              {product.active ? (
                                <button
                                  className="admin-deactivate-button"
                                  onClick={() => handleDeactivate(product)}
                                  disabled={isProcessing}
                                >
                                  {isProcessing
                                    ? "Processing..."
                                    : "Deactivate"}
                                </button>
                              ) : (
                                <button
                                  onClick={() => handleActivate(product)}
                                  disabled={isProcessing}
                                  className="admin-activate-button"
                                >
                                  {isProcessing
                                    ? "Processing..."
                                    : "Activate"}
                                </button>
                              )}

                            </div>
                          </td>

                        </tr>
                      );
                    })}
                  </tbody>

                </table>
              </div>

              {/* Pagination */}
              <div className="admin-products-pagination">

                <span className="admin-pagination-info">
                  Showing {startIndex + 1}–{endIndex} of{" "}
                  {filteredProducts.length} products
                </span>

                <div className="admin-pagination-controls">

                  <button
                    className="admin-pagination-button"
                    onClick={() => setCurrentPage((page) => page - 1)}
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
                      className={`admin-pagination-number ${
                        currentPage === page ? "selected" : ""
                      }`}
                      onClick={() => setCurrentPage(page)}
                      aria-current={
                        currentPage === page ? "page" : undefined
                      }
                    >
                      {page}
                    </button>
                  ))}

                  <button
                    className="admin-pagination-button"
                    onClick={() => setCurrentPage((page) => page + 1)}
                    disabled={currentPage === totalPages}
                  >
                    Next
                  </button>

                </div>
              </div>
            </>
          )}
        </section>

      </main>

      <Footer />
    </>
  );
}

export default AdminProducts;
