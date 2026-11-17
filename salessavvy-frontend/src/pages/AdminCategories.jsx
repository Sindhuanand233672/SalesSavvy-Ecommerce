
import { useEffect, useMemo, useState } from "react";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import api from "../services/api";
import "../styles/admin-categories.css";

const PAGE_SIZE = 10;

const emptyForm = {
  name: "",
  description: "",
  active: true,
};

function getErrorMessage(error, fallback) {
  const data = error.response?.data;

  if (typeof data === "string") return data;
  if (data?.message) return data.message;
  if (data?.detail) return data.detail;

  return fallback;
}

function AdminCategories() {
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({ ...emptyForm });
  const [editingId, setEditingId] = useState(null);

  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("success");

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);

  const showMessage = (text, type = "success") => {
    setMessage(text);
    setMessageType(type);
  };

  const fetchCategories = async () => {
    try {
      setLoading(true);

      const response = await api.get("/categories/admin/all");

      setCategories(
        Array.isArray(response.data) ? response.data : []
      );
    } catch (error) {
      showMessage(
        getErrorMessage(error, "Unable to load categories."),
        "error"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  // Reset to first page when search or filter changes
  useEffect(() => {
    setCurrentPage(1);
  }, [search, statusFilter]);

  const activeCount = categories.filter(
    (category) => category.active === true
  ).length;

  const inactiveCount = categories.filter(
    (category) => category.active === false
  ).length;

  const filteredCategories = useMemo(() => {
    const searchText = search.trim().toLowerCase();

    return categories.filter((category) => {
      const name = (category.name || "").toLowerCase();
      const description = (category.description || "").toLowerCase();

      const matchesSearch =
        name.includes(searchText) ||
        description.includes(searchText);

      const matchesStatus =
        statusFilter === "ALL" ||
        (statusFilter === "ACTIVE" && category.active === true) ||
        (statusFilter === "INACTIVE" && category.active === false);

      return matchesSearch && matchesStatus;
    });
  }, [categories, search, statusFilter]);

  // Pagination calculations
  const totalPages = Math.ceil(
    filteredCategories.length / PAGE_SIZE
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
    filteredCategories.length
  );

  const paginatedCategories = filteredCategories.slice(
    startIndex,
    endIndex
  );

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const openAddForm = () => {
    setEditingId(null);
    setForm({ ...emptyForm });
    setShowForm(true);
    setMessage("");
  };

  const openEditForm = (category) => {
    setEditingId(category.id);

    setForm({
      name: category.name || "",
      description: category.description || "",
      active: category.active === true,
    });

    setShowForm(true);
    setMessage("");

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  const closeForm = () => {
    if (saving) return;

    setShowForm(false);
    setEditingId(null);
    setForm({ ...emptyForm });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const name = form.name.trim();
    const description = form.description.trim();

    if (!name) {
      showMessage("Please enter a category name.", "error");
      return;
    }

    if (name.length > 100) {
      showMessage(
        "Category name cannot exceed 100 characters.",
        "error"
      );
      return;
    }

    if (description.length > 500) {
      showMessage(
        "Description cannot exceed 500 characters.",
        "error"
      );
      return;
    }

    const payload = {
      name,
      description,
      active: form.active,
    };

    const isEditing = editingId !== null;

    try {
      setSaving(true);
      setMessage("");

      if (isEditing) {
        await api.put(`/categories/${editingId}`, payload);
      } else {
        await api.post("/categories", payload);
      }

      closeForm();
      await fetchCategories();

      if (!isEditing) {
        setCurrentPage(1);
      }

      showMessage(
        isEditing
          ? "Category updated successfully."
          : "Category created successfully."
      );
    } catch (error) {
      showMessage(
        getErrorMessage(error, "Unable to save category."),
        "error"
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDeactivate = async (category) => {
    const confirmed = window.confirm(
      `Are you sure you want to deactivate "${category.name}"?`
    );

    if (!confirmed) return;

    try {
      await api.delete(`/categories/${category.id}`);

      await fetchCategories();

      showMessage("Category deactivated successfully.");
    } catch (error) {
      showMessage(
        getErrorMessage(error, "Unable to deactivate category."),
        "error"
      );
    }
  };

  const handleReactivate = async (category) => {
    try {
      await api.put(`/categories/${category.id}`, {
        name: category.name,
        description: category.description || "",
        active: true,
      });

      await fetchCategories();

      showMessage("Category activated successfully.");
    } catch (error) {
      showMessage(
        getErrorMessage(error, "Unable to activate category."),
        "error"
      );
    }
  };

  return (
    <>
      <Navbar />

      <main className="admin-categories-page">
        <div className="admin-categories-container">

          <div className="categories-heading">
            <div>
              <span className="categories-eyebrow">
                ADMINISTRATION
              </span>

              <h1>Category Management</h1>

              <p>
                Add, update and manage your store categories.
              </p>
            </div>

            <button
              type="button"
              className="category-primary-btn"
              onClick={openAddForm}
            >
              + Add Category
            </button>
          </div>

          {message && (
            <div
              className={`category-alert ${messageType}`}
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

          <section className="category-stats">
            <div className="category-stat-card">
              <span>Total Categories</span>
              <strong>{categories.length}</strong>
            </div>

            <div className="category-stat-card">
              <span>Active Categories</span>
              <strong>{activeCount}</strong>
            </div>

            <div className="category-stat-card">
              <span>Inactive Categories</span>
              <strong>{inactiveCount}</strong>
            </div>
          </section>

          {showForm && (
            <section className="category-form-card">
              <div className="category-form-heading">
                <h2>
                  {editingId !== null
                    ? "Edit Category"
                    : "Add New Category"}
                </h2>

                <button
                  type="button"
                  className="category-close-btn"
                  onClick={closeForm}
                  aria-label="Close form"
                  disabled={saving}
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleSubmit}>
                <div className="category-form-grid">
                  <div className="category-field">
                    <label htmlFor="categoryName">
                      Category Name *
                    </label>

                    <input
                      id="categoryName"
                      name="name"
                      type="text"
                      value={form.name}
                      onChange={handleChange}
                      placeholder="Enter category name"
                      maxLength={100}
                      required
                    />
                  </div>

                  <div className="category-field">
                    <label htmlFor="categoryDescription">
                      Description
                    </label>

                    <textarea
                      id="categoryDescription"
                      name="description"
                      value={form.description}
                      onChange={handleChange}
                      placeholder="Describe this category"
                      maxLength={500}
                      rows={4}
                    />
                  </div>
                </div>

                {editingId !== null && (
                  <label className="category-active-toggle">
                    <input
                      type="checkbox"
                      checked={form.active}
                      onChange={(event) =>
                        setForm((previous) => ({
                          ...previous,
                          active: event.target.checked,
                        }))
                      }
                    />

                    Category is active
                  </label>
                )}

                <div className="category-form-actions">
                  <button
                    type="button"
                    className="category-cancel-btn"
                    onClick={closeForm}
                    disabled={saving}
                  >
                    Cancel
                  </button>

                  <button
                    type="submit"
                    className="category-primary-btn"
                    disabled={saving}
                  >
                    {saving
                      ? "Saving..."
                      : editingId !== null
                        ? "Update Category"
                        : "Add Category"}
                  </button>
                </div>
              </form>
            </section>
          )}

          <section className="category-toolbar">
            <input
              type="text"
              placeholder="Search by category name or description..."
              value={search}
              onChange={(event) => setSearch(event.target.value)}
            />

            <select
              value={statusFilter}
              onChange={(event) =>
                setStatusFilter(event.target.value)
              }
            >
              <option value="ALL">All Categories</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </select>

            <button
              type="button"
              className="category-refresh-btn"
              onClick={fetchCategories}
              disabled={loading}
            >
              {loading ? "Loading..." : "Refresh"}
            </button>
          </section>

          <section className="category-table-card">
            <h2>
              All Categories ({filteredCategories.length})
            </h2>

            {loading ? (
              <div className="category-empty-state">
                Loading categories...
              </div>
            ) : filteredCategories.length === 0 ? (
              <div className="category-empty-state">
                No categories found.
              </div>
            ) : (
              <>
                <div className="category-table-wrapper">
                  <table className="category-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>Category Name</th>
                        <th>Description</th>
                        <th>Status</th>
                        <th>Created At</th>
                        <th>Actions</th>
                      </tr>
                    </thead>

                    <tbody>
                      {paginatedCategories.map((category) => (
                        <tr key={category.id}>
                          <td>#{category.id}</td>

                          <td className="category-name-cell">
                            {category.name}
                          </td>

                          <td className="category-description-cell">
                            {category.description || "—"}
                          </td>

                          <td>
                            <span
                              className={`category-status ${
                                category.active ? "active" : "inactive"
                              }`}
                            >
                              {category.active ? "Active" : "Inactive"}
                            </span>
                          </td>

                          <td>
                            {category.createdAt
                              ? new Date(
                                  category.createdAt
                                ).toLocaleDateString()
                              : "—"}
                          </td>

                          <td>
                            <div className="category-action-buttons">
                              <button
                                type="button"
                                className="category-edit-btn"
                                onClick={() => openEditForm(category)}
                              >
                                Edit
                              </button>

                              {category.active ? (
                                <button
                                  type="button"
                                  className="category-deactivate-btn"
                                  onClick={() =>
                                    handleDeactivate(category)
                                  }
                                >
                                  Deactivate
                                </button>
                              ) : (
                                <button
                                  type="button"
                                  className="category-activate-btn"
                                  onClick={() =>
                                    handleReactivate(category)
                                  }
                                >
                                  Activate
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Pagination */}
                <div className="category-pagination">

                  <span className="category-pagination-info">
                    Showing {startIndex + 1}–{endIndex} of{" "}
                    {filteredCategories.length} categories
                  </span>

                  <div className="category-pagination-controls">

                    <button
                      type="button"
                      className="category-pagination-btn"
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
                        type="button"
                        key={page}
                        className={`category-page-number ${
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
                      type="button"
                      className="category-pagination-btn"
                      onClick={() =>
                        setCurrentPage((page) => page + 1)
                      }
                      disabled={currentPage === totalPages}
                    >
                      Next
                    </button>

                  </div>
                </div>
              </>
            )}
          </section>

        </div>
      </main>

      <Footer />
    </>
  );
}

export default AdminCategories;
