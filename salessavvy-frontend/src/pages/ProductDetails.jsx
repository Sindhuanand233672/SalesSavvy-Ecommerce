
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  ArrowLeft,
  Heart,
  ShoppingCart,
  Minus,
  Plus,
  Star
} from "lucide-react";

import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import api from "../services/api";
import { useAuth } from "../context/AuthContext";

import "../styles/product-details.css";
import "../styles/product-reviews.css";

function ProductDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isLoggedIn, isAdmin } = useAuth();

  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [addingToCart, setAddingToCart] = useState(false);
  const [addingToWishlist, setAddingToWishlist] = useState(false);

  // Review states
  const [reviews, setReviews] = useState([]);
  const [reviewsLoading, setReviewsLoading] = useState(true);

  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");

  const [submittingReview, setSubmittingReview] = useState(false);
  const [reviewMessage, setReviewMessage] = useState("");
  const [reviewError, setReviewError] = useState("");

  // Fetch product details
  useEffect(() => {
    let cancelled = false;

    const fetchProduct = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await api.get(`/products/${id}`);

        if (!cancelled) {
          setProduct(response.data);
          setQuantity(1);
        }
      } catch (err) {
        console.error("Product details error:", err);

        if (!cancelled) {
          setError(
            err.response?.data?.message ||
            "Unable to load product details."
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    fetchProduct();

    return () => {
      cancelled = true;
    };
  }, [id]);

  // Fetch only approved reviews
  const fetchReviews = async () => {
    try {
      setReviewsLoading(true);

      const response = await api.get(`/reviews/product/${id}`);

      setReviews(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error("Fetch reviews error:", err);
      setReviews([]);
    } finally {
      setReviewsLoading(false);
    }
  };

  useEffect(() => {
    fetchReviews();
  }, [id]);

  // Add product to cart
  const handleAddToCart = async () => {
    if (!isLoggedIn) {
      navigate("/login");
      return;
    }

    try {
      setAddingToCart(true);

      await api.post("/cart/add", {
        productId: product.id,
        quantity
      });

      alert("Product added to cart successfully!");
    } catch (err) {
      console.error("Add to cart error:", err);

      alert(
        err.response?.data?.message ||
        "Unable to add product to cart."
      );
    } finally {
      setAddingToCart(false);
    }
  };

  // Add product to wishlist
  const handleAddToWishlist = async () => {
    if (!isLoggedIn) {
      navigate("/login");
      return;
    }

    try {
      setAddingToWishlist(true);

      await api.post(`/wishlist/add/${product.id}`);

      alert("Product added to wishlist successfully!");
    } catch (err) {
      console.error("Add to wishlist error:", err);

      alert(
        err.response?.data?.message ||
        "Unable to add product to wishlist."
      );
    } finally {
      setAddingToWishlist(false);
    }
  };

  // Submit product review
  const handleSubmitReview = async (e) => {
    e.preventDefault();

    setReviewMessage("");
    setReviewError("");

    if (!isLoggedIn) {
      navigate("/login");
      return;
    }

    if (!comment.trim()) {
      setReviewError("Please enter your review comment.");
      return;
    }

    if (comment.trim().length < 5) {
      setReviewError("Your review must contain at least 5 characters.");
      return;
    }

    if (comment.trim().length > 1000) {
      setReviewError("Your review cannot exceed 1000 characters.");
      return;
    }

    try {
      setSubmittingReview(true);

      await api.post("/reviews", {
        productId: Number(id),
        rating,
        comment: comment.trim()
      });

      setComment("");
      setRating(5);

      setReviewMessage(
        "Your review has been submitted successfully! It will appear publicly after admin approval."
      );

      setReviewError("");

      // Refresh public reviews.
      // The newly submitted review remains hidden until approved.
      await fetchReviews();

    } catch (err) {
      console.error("Submit review error:", err);

      const status = err.response?.status;
      const backendMessage = err.response?.data?.message;

      if (status === 403) {
        setReviewError(
          backendMessage ||
          "You can review this product only after it has been delivered to you."
        );
      } else if (status === 409) {
        setReviewError(
          backendMessage ||
          "You have already reviewed this product."
        );
      } else if (status === 400) {
        setReviewError(
          backendMessage ||
          "Please check your rating and review comment."
        );
      } else if (status === 401 || status === 403) {
        setReviewError("Please log in with your customer account.");
      } else {
        setReviewError(
          backendMessage ||
          "Unable to submit your review. Please try again."
        );
      }
    } finally {
      setSubmittingReview(false);
    }
  };

  const stock = product?.stockQuantity ?? 0;

  const category =
    typeof product?.category === "string"
      ? product.category
      : product?.category?.name || "General";

  // Average rating uses approved reviews returned by the backend.
  const averageRating =
    reviews.length > 0
      ? reviews.reduce(
          (total, review) => total + Number(review.rating || 0),
          0
        ) / reviews.length
      : 0;

  // Render rating stars
  const renderStars = (value, size = 18) => {
    return (
      <div className="review-stars" aria-label={`${value} out of 5 stars`}>
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            size={size}
            fill={star <= Math.round(value) ? "#f5a623" : "none"}
            color={star <= Math.round(value) ? "#f5a623" : "#b8b8b8"}
          />
        ))}
      </div>
    );
  };

  return (
    <>
      <Navbar />

      <main className="product-details-page">

        <button
          type="button"
          className="back-to-shop"
          onClick={() => navigate(-1)}
        >
          <ArrowLeft size={18} />
          Back to Products
        </button>

        {loading && (
          <p className="details-message">
            Loading product...
          </p>
        )}

        {!loading && error && (
          <p className="details-message error-message">
            {error}
          </p>
        )}

        {!loading && !error && product && (
          <>
            {/* PRODUCT DETAILS */}
            <div className="product-details-container">

              <div className="details-image">
                <img
                  src={
                    product.imageUrl ||
                    "https://placehold.co/600x600?text=Product"
                  }
                  alt={product.name}
                  onError={(e) => {
                    e.currentTarget.src =
                      "https://placehold.co/600x600?text=Product";
                  }}
                />
              </div>

              <div className="details-info">

                <span className="details-category">
                  {category}
                </span>

                <h1>{product.name}</h1>

                <p className="details-sku">
                  SKU: {product.sku}
                </p>

                <h2 className="details-price">
                  ₹{Number(product.price).toLocaleString("en-IN")}
                </h2>

                <p className="details-description">
                  {product.description ||
                    "No product description available."}
                </p>

                <p
                  className={
                    stock > 0
                      ? "stock-available"
                      : "stock-unavailable"
                  }
                >
                  {stock > 0
                    ? `${stock} items in stock`
                    : "Out of stock"}
                </p>

                {stock > 0 && (
                  <div className="quantity-selector">

                    <span>Quantity:</span>

                    <button
                      type="button"
                      onClick={() =>
                        setQuantity((q) => Math.max(1, q - 1))
                      }
                      disabled={quantity <= 1}
                      aria-label="Decrease quantity"
                    >
                      <Minus size={16} />
                    </button>

                    <strong>{quantity}</strong>

                    <button
                      type="button"
                      onClick={() =>
                        setQuantity((q) => Math.min(stock, q + 1))
                      }
                      disabled={quantity >= stock}
                      aria-label="Increase quantity"
                    >
                      <Plus size={16} />
                    </button>

                  </div>
                )}

                {!isAdmin && (
                  <div className="details-actions">

                    <button
                      type="button"
                      className="details-cart-btn"
                      onClick={handleAddToCart}
                      disabled={stock <= 0 || addingToCart}
                    >
                      <ShoppingCart size={19} />
                      {addingToCart ? "Adding..." : "Add to Cart"}
                    </button>

                    <button
                      type="button"
                      className="details-wishlist-btn"
                      onClick={handleAddToWishlist}
                      disabled={addingToWishlist}
                      aria-label="Add to wishlist"
                    >
                      <Heart size={20} />
                    </button>

                  </div>
                )}

              </div>
            </div>

            {/* PRODUCT REVIEWS SECTION */}
            <section className="product-reviews-section">

              <div className="reviews-heading">

                <div>
                  <h2>Customer Reviews & Ratings</h2>
                  <p className="reviews-subtitle">
                    Genuine feedback from our customers
                  </p>
                </div>

                <div className="average-rating">

                  <strong>
                    {reviews.length > 0
                      ? averageRating.toFixed(1)
                      : "0.0"}
                  </strong>

                  {renderStars(averageRating)}

                  <span>
                    ({reviews.length}{" "}
                    {reviews.length === 1 ? "review" : "reviews"})
                  </span>

                </div>

              </div>

              {/* CUSTOMER REVIEW FORM */}
              {!isAdmin && (
                <div className="write-review-card">

                  <h3>Write a Review</h3>

                  {!isLoggedIn ? (
                    <div className="review-login-prompt">
                      <p>
                        Please log in to share your experience with this product.
                      </p>

                      <button
                        type="button"
                        className="submit-review-btn"
                        onClick={() => navigate("/login")}
                      >
                        Login to Write a Review
                      </button>
                    </div>
                  ) : (
                    <form onSubmit={handleSubmitReview}>

                      <label>
                        Rate this product
                      </label>

                      <div className="rating-selector">
                        {[1, 2, 3, 4, 5].map((star) => (
                          <button
                            type="button"
                            key={star}
                            onClick={() => setRating(star)}
                            aria-label={`Rate ${star} out of 5`}
                            aria-pressed={rating === star}
                          >
                            <Star
                              size={30}
                              fill={star <= rating ? "#f5a623" : "none"}
                              color={star <= rating ? "#f5a623" : "#b8b8b8"}
                            />
                          </button>
                        ))}
                      </div>

                      <label htmlFor="review-comment">
                        Your Review
                      </label>

                      <textarea
                        id="review-comment"
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                        placeholder="Share your experience with this product..."
                        maxLength={1000}
                        minLength={5}
                        rows={4}
                        required
                      />

                      <div className="review-character-count">
                        {comment.length}/1000
                      </div>

                      {reviewMessage && (
                        <p className="review-success" role="status">
                          {reviewMessage}
                        </p>
                      )}

                      {reviewError && (
                        <p className="review-error" role="alert">
                          {reviewError}
                        </p>
                      )}

                      <button
                        type="submit"
                        className="submit-review-btn"
                        disabled={submittingReview}
                      >
                        {submittingReview
                          ? "Submitting..."
                          : "Submit Review"}
                      </button>

                      <p className="review-note">
                        Reviews are visible publicly after admin approval.
                        Only customers who have received the product can submit a review.
                      </p>

                    </form>
                  )}

                </div>
              )}

              {isAdmin && (
                <div className="admin-review-note">
                  You are viewing this page as an administrator.
                  Review moderation is available in the admin review section.
                </div>
              )}

              {/* APPROVED CUSTOMER REVIEWS */}
              <div className="customer-reviews-list">

                <h3>Customer Feedback</h3>

                {reviewsLoading && (
                  <p className="no-reviews">
                    Loading reviews...
                  </p>
                )}

                {!reviewsLoading && reviews.length === 0 && (
                  <p className="no-reviews">
                    No approved reviews yet. Be the first to review this product!
                  </p>
                )}

                {!reviewsLoading && reviews.map((review) => (
                  <div className="customer-review-card" key={review.id}>

                    <div className="customer-review-top">

                      <strong>
                        {review.reviewerName || "Customer"}
                      </strong>

                      <span>
                        {review.createdAt
                          ? new Date(review.createdAt).toLocaleDateString("en-IN")
                          : ""}
                      </span>

                    </div>

                    {renderStars(review.rating)}

                    <p>{review.comment}</p>

                  </div>
                ))}

              </div>

            </section>
          </>
        )}

      </main>

      <Footer />
    </>
  );
}

export default ProductDetails;
