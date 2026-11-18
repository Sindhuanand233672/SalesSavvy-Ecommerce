
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  CheckCircle,
  RefreshCw,
  Star,
  MessageSquare,
  XCircle,
  ClipboardCheck,
  Clock3,
} from "lucide-react";

import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import api from "../services/api";

import "../styles/admin-reviews.css";

function AdminReviews() {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);
  const [success, setSuccess] = useState("");

  const fetchPendingReviews = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      setSuccess("");

      const response = await api.get("/reviews/admin/pending");

      setReviews(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to load pending reviews."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPendingReviews();
  }, [fetchPendingReviews]);

  const averageRating = useMemo(() => {
    if (reviews.length === 0) return "0.0";

    const total = reviews.reduce(
      (sum, review) => sum + Number(review.rating || 0),
      0
    );

    return (total / reviews.length).toFixed(1);
  }, [reviews]);

  const handleAction = async (reviewId, action) => {
    const reject = action === "reject";

    if (
      reject &&
      !window.confirm(
        "Reject and permanently remove this pending review?"
      )
    ) {
      return;
    }

    try {
      setBusyId(reviewId);
      setError("");
      setSuccess("");

      if (reject) {
        await api.delete(`/reviews/admin/${reviewId}/reject`);
      } else {
        await api.put(`/reviews/admin/${reviewId}/approve`);
      }

      setReviews((items) =>
        items.filter((item) => item.id !== reviewId)
      );

      setSuccess(
        reject
          ? "Review rejected and removed successfully."
          : "Review approved successfully."
      );
    } catch (err) {
      setError(
        err.response?.data?.message ||
          `Unable to ${action} review.`
      );
    } finally {
      setBusyId(null);
    }
  };

  return (
    <>
      <Navbar />

      <main className="admin-reviews-page">
        <div className="admin-reviews-header">
          <div>
            <span className="admin-reviews-eyebrow">
              ADMINISTRATION
            </span>

            <h1>Review Management</h1>

            <p>
              Moderate customer feedback before it appears on
              product pages.
            </p>
          </div>

          <button
            className="refresh-reviews-btn"
            onClick={fetchPendingReviews}
            disabled={loading || busyId !== null}
          >
            <RefreshCw
              size={17}
              className={loading ? "review-spin" : ""}
            />
            Refresh
          </button>
        </div>

        <section className="review-summary-grid">
          <article className="review-summary-card pending">
            <span className="review-summary-icon">
              <Clock3 size={21} />
            </span>

            <div>
              <strong>{loading ? "—" : reviews.length}</strong>
              <span>Pending reviews</span>
            </div>
          </article>

          <article className="review-summary-card">
            <span className="review-summary-icon">
              <Star size={21} />
            </span>

            <div>
              <strong>{loading ? "—" : `${averageRating}/5`}</strong>
              <span>Average pending rating</span>
            </div>
          </article>

          <article className="review-summary-card">
            <span className="review-summary-icon">
              <ClipboardCheck size={21} />
            </span>

            <div>
              <strong>Manual</strong>
              <span>Approval required</span>
            </div>
          </article>
        </section>

        <div className="review-list-heading">
          <div>
            <h2>Pending customer reviews</h2>
            <p>
              Approve suitable reviews or reject inappropriate
              submissions.
            </p>
          </div>

          <span>
            {reviews.length} pending
          </span>
        </div>

        {success && (
          <div className="admin-review-success" role="status">
            <CheckCircle size={18} />
            {success}
          </div>
        )}

        {error && (
          <div className="admin-review-error" role="alert">
            <XCircle size={18} />
            {error}
          </div>
        )}

        {loading && (
          <div className="review-empty-state">
            Loading pending reviews...
          </div>
        )}

        {!loading && !error && reviews.length === 0 && (
          <div className="no-pending-reviews">
            <CheckCircle size={46} />

            <h2>You're all caught up!</h2>

            <p>
              There are no customer reviews waiting for approval
              right now.
            </p>

            <button onClick={fetchPendingReviews}>
              Check again
            </button>
          </div>
        )}

        {!loading && reviews.length > 0 && (
          <div className="pending-reviews-list">
            {reviews.map((review) => (
              <article
                className="admin-review-card"
                key={review.id}
              >
                <div className="admin-review-top">
                  <div>
                    <span className="review-product-label">
                      PRODUCT
                    </span>

                    <h3>
                      {review.productName ||
                        `Product #${review.productId}`}
                    </h3>

                    <p>Review #{review.id}</p>
                  </div>

                  <span className="pending-review-badge">
                    Pending approval
                  </span>
                </div>

                <div className="admin-review-details">
                  <p>
                    <strong>Customer</strong>
                    <span>
                      {review.reviewerName || "Customer"}
                    </span>
                  </p>

                  <p>
                    <strong>Rating</strong>

                    <span className="admin-review-stars">
                      {[1, 2, 3, 4, 5].map((number) => (
                        <Star
                          key={number}
                          size={17}
                          fill={
                            number <= Number(review.rating)
                              ? "#f5a623"
                              : "none"
                          }
                          color="#f5a623"
                        />
                      ))}

                      <b>{review.rating}/5</b>
                    </span>
                  </p>

                  <p>
                    <strong>Submitted</strong>

                    <span>
                      {review.createdAt
                        ? new Date(
                            review.createdAt
                          ).toLocaleString("en-IN")
                        : "Date unavailable"}
                    </span>
                  </p>
                </div>

                <div className="admin-review-comment">
                  <strong>
                    <MessageSquare
                      size={16}
                      style={{
                        display: "inline",
                        marginRight: "7px",
                        verticalAlign: "middle",
                      }}
                    />
                    Customer feedback
                  </strong>

                  <p>
                    {review.comment ||
                      "No written comment provided."}
                  </p>
                </div>

                <div className="review-moderation-actions">
                  <button
                    className="approve-review-btn"
                    onClick={() =>
                      handleAction(review.id, "approve")
                    }
                    disabled={busyId !== null}
                  >
                    <CheckCircle size={17} />

                    {busyId === review.id
                      ? "Please wait..."
                      : "Approve review"}
                  </button>

                  <button
                    className="reject-review-btn"
                    onClick={() =>
                      handleAction(review.id, "reject")
                    }
                    disabled={busyId !== null}
                  >
                    <XCircle size={17} />
                    Reject review
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}

        <p className="review-management-note">
          Note: Rejection permanently removes a pending review
          because the current review database stores only an
          approved/not-approved flag, not a separate rejected status.
        </p>
      </main>

      <Footer />
    </>
  );
}

export default AdminReviews;
