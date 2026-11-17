
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import api from "../services/api";
import "../styles/admin-customers.css";

function AdminCustomers() {
  const navigate = useNavigate();

  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [currentPage, setCurrentPage] = useState(1);
  const [processingId, setProcessingId] = useState(null);

  const pageSize = 10;

  // Fetch all customers
  const fetchCustomers = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/admin/customers");
      const data = response.data;

      setCustomers(
        Array.isArray(data) ? data : data?.content || []
      );
    } catch (err) {
      console.error("Admin customers error:", err);

      setError(
        err.response?.data?.message ||
          "Unable to load customers."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCustomers();
  }, []);

  // Activate or deactivate customer
  const handleToggleStatus = async (customer) => {
    const newStatus = !customer.active;

    const confirmed = window.confirm(
      `Are you sure you want to ${
        newStatus ? "activate" : "deactivate"
      } ${customer.fullName}?`
    );

    if (!confirmed) return;

    try {
      setProcessingId(customer.id);

      await api.put(
        `/admin/customers/${customer.id}/status`,
        null,
        {
          params: {
            active: newStatus,
          },
        }
      );

      await fetchCustomers();
    } catch (err) {
      console.error("Customer status update failed:", err);

      window.alert(
        err.response?.data?.message ||
          "Unable to update customer status."
      );
    } finally {
      setProcessingId(null);
    }
  };

  // Search and filter customers
  const filteredCustomers = useMemo(() => {
    return customers.filter((customer) => {
      const searchText = search.toLowerCase().trim();

      const matchesSearch =
        (customer.fullName || "")
          .toLowerCase()
          .includes(searchText) ||
        (customer.email || "")
          .toLowerCase()
          .includes(searchText) ||
        (customer.phoneNumber || "").includes(searchText);

      const matchesStatus =
        statusFilter === "ALL" ||
        (statusFilter === "ACTIVE" &&
          customer.active === true) ||
        (statusFilter === "INACTIVE" &&
          customer.active === false);

      return matchesSearch && matchesStatus;
    });
  }, [customers, search, statusFilter]);

  // Pagination
  const totalPages = Math.ceil(
    filteredCustomers.length / pageSize
  );

  const paginatedCustomers = filteredCustomers.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );

  // Customer statistics
  const activeCount = customers.filter(
    (customer) => customer.active === true
  ).length;

  const inactiveCount = customers.filter(
    (customer) => customer.active === false
  ).length;

  // Format joined date
  const formatDate = (date) => {
    return date
      ? new Date(date).toLocaleDateString("en-IN", {
          day: "2-digit",
          month: "short",
          year: "numeric",
        })
      : "-";
  };

  const handleSearch = (value) => {
    setSearch(value);
    setCurrentPage(1);
  };

  const handleFilter = (value) => {
    setStatusFilter(value);
    setCurrentPage(1);
  };

  return (
    <>
      <Navbar />

      <main className="admin-customers-page">
        {/* Page Header */}
        <div className="admin-customers-header">
          <div>
            <span className="admin-customers-eyebrow">
              ADMINISTRATION
            </span>

            <h1>Customer Management</h1>

            <p>
              View and manage registered store customers.
            </p>
          </div>

          <button
            type="button"
            className="admin-customers-refresh"
            onClick={fetchCustomers}
            disabled={loading}
          >
            {loading ? "Loading..." : "Refresh Customers"}
          </button>
        </div>

        {/* Customer Statistics */}
        <section className="admin-customers-stats">
          <div className="admin-customer-stat">
            <span>Total Customers</span>
            <strong>{customers.length}</strong>
          </div>

          <div className="admin-customer-stat">
            <span>Active Customers</span>
            <strong>{activeCount}</strong>
          </div>

          <div className="admin-customer-stat">
            <span>Inactive Customers</span>
            <strong>{inactiveCount}</strong>
          </div>
        </section>

        {/* Customer Management Card */}
        <section className="admin-customers-card">
          {/* Search and Filter */}
          <div className="admin-customers-controls">
            <input
              type="text"
              placeholder="Search by name, email or phone..."
              value={search}
              onChange={(e) =>
                handleSearch(e.target.value)
              }
            />

            <select
              value={statusFilter}
              onChange={(e) =>
                handleFilter(e.target.value)
              }
            >
              <option value="ALL">All Customers</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </select>
          </div>

          {/* Loading */}
          {loading && (
            <p className="admin-customers-message">
              Loading customers...
            </p>
          )}

          {/* Error */}
          {!loading && error && (
            <div className="admin-customers-error">
              {error}

              <button
                type="button"
                onClick={fetchCustomers}
              >
                Retry
              </button>
            </div>
          )}

          {/* Customer Table */}
          {!loading && !error && (
            <>
              <div className="admin-customers-table-heading">
                <h2>All Customers</h2>
                <span>
                  {filteredCustomers.length} records
                </span>
              </div>

              {paginatedCustomers.length === 0 ? (
                <p className="admin-customers-message">
                  No customers found.
                </p>
              ) : (
                <div className="admin-customers-table-scroll">
                  <table className="admin-customers-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>Customer</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Joined Date</th>
                        <th>Status</th>
                        <th>Actions</th>
                      </tr>
                    </thead>

                    <tbody>
                      {paginatedCustomers.map((customer) => (
                        <tr key={customer.id}>
                          <td>#{customer.id}</td>

                          <td>
                            <strong>
                              {customer.fullName || "-"}
                            </strong>
                          </td>

                          <td>{customer.email || "-"}</td>

                          <td>
                            {customer.phoneNumber || "-"}
                          </td>

                          <td>
                            {formatDate(customer.createdAt)}
                          </td>

                          <td>
                            <span
                              className={`admin-customer-status ${
                                customer.active
                                  ? "active"
                                  : "inactive"
                              }`}
                            >
                              {customer.active
                                ? "Active"
                                : "Inactive"}
                            </span>
                          </td>

                          <td>
                            <div
                              className="admin-customer-actions"
                            >
                              {/* View Customer Details */}
                              <button
                                type="button"
                                className="admin-customer-view"
                                onClick={() =>
                                  navigate(
                                    `/admin/customers/${customer.id}`
                                  )
                                }
                              >
                                View Details
                              </button>

                              {/* Activate / Deactivate */}
                              <button
                                type="button"
                                className={`admin-customer-action ${
                                  customer.active
                                    ? "deactivate"
                                    : "activate"
                                }`}
                                onClick={() =>
                                  handleToggleStatus(customer)
                                }
                                disabled={
                                  processingId === customer.id
                                }
                              >
                                {processingId === customer.id
                                  ? "Processing..."
                                  : customer.active
                                  ? "Deactivate"
                                  : "Activate"}
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}

              {/* Pagination */}
              {filteredCustomers.length > 0 && (
                <div className="admin-customers-pagination">
                  <span>
                    Showing{" "}
                    {(currentPage - 1) * pageSize + 1}–
                    {Math.min(
                      currentPage * pageSize,
                      filteredCustomers.length
                    )}{" "}
                    of {filteredCustomers.length} customers
                  </span>

                  <div>
                    <button
                      type="button"
                      disabled={currentPage === 1}
                      onClick={() =>
                        setCurrentPage((page) => page - 1)
                      }
                    >
                      Previous
                    </button>

                    <span>
                      {currentPage} / {totalPages}
                    </span>

                    <button
                      type="button"
                      disabled={
                        currentPage === totalPages
                      }
                      onClick={() =>
                        setCurrentPage((page) => page + 1)
                      }
                    >
                      Next
                    </button>
                  </div>
                </div>
              )}
            </>
          )}
        </section>
      </main>

      <Footer />
    </>
  );
}

export default AdminCustomers;
