import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import api from "../services/api";
import "../styles/my-reviews.css";

function MyReviews() {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchMyReviews = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/reviews/my-reviews");
      setReviews(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error("Fetch my reviews error:", err);
      setError(
        err.response?.data?.message ||
        (err.response?.status === 401
          ? "Your session has expired. Please sign in again."
          : "Unable to load your reviews. Please try again.")
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchMyReviews();
  }, [fetchMyReviews]);

  return (
    <>
      <Navbar />

      <main className="my-reviews-page">
        <div className="my-reviews-header">
          <p className="my-reviews-eyebrow">YOUR FEEDBACK</p>
          <h1>My Reviews</h1>
          <p>View the reviews you've shared with our community.</p>
        </div>

        {loading && (
          <p className="reviews-message">Loading your reviews...</p>
        )}

        {!loading && error && (
          <div className="reviews-error" role="alert">
            <p>{error}</p>
            <button type="button" onClick={fetchMyReviews}>
              Try Again
            </button>
          </div>
        )}

        {!loading && !error && reviews.length === 0 && (
          <div className="my-reviews-empty">
            <h2>No reviews yet</h2>
            <p>Your submitted product reviews will appear here.</p>
            <Link to="/products" className="my-reviews-shop-link">
              Explore products
            </Link>
          </div>
        )}

        {!loading && !error && reviews.length > 0 && (
          <div className="my-reviews-list">
            {reviews.map((review) => {
              const rating = Math.min(5, Math.max(0, Number(review.rating) || 0));

              return (
                <article className="my-review-card" key={review.id}>
                  <div className="my-review-top">
                    <div>
                      <h2>
                        {review.productName || `Product #${review.productId}`}
                      </h2>
                      <p>
                        {review.createdAt
                          ? new Date(review.createdAt).toLocaleDateString("en-IN")
                          : "Date unavailable"}
                      </p>
                    </div>

                    <span
                      className={`review-status ${
                        review.approved ? "approved" : "pending"
                      }`}
                    >
                      {review.approved ? "Approved" : "Pending approval"}
                    </span>
                  </div>

                  <div
                    className="my-review-stars"
                    aria-label={`${rating} out of 5 stars`}
                  >
                    {"★".repeat(rating)}
                    {"☆".repeat(5 - rating)}
                  </div>

                  <p className="my-review-comment">{review.comment}</p>

                  {review.productId && (
                    <Link
                      className="my-review-product-link"
                      to={`/products/${review.productId}`}
                    >
                      View product
                    </Link>
                  )}
                </article>
              );
            })}
          </div>
        )}
      </main>

      <Footer />
    </>
  );
}

export default MyReviews;
