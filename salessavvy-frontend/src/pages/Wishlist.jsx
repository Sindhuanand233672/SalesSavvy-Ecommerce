
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Heart,
  ShoppingCart,
  Trash2,
  ArrowRight,
  RefreshCw
} from "lucide-react";

import api from "../services/api";
import Navbar from "../components/Navbar";
import "../styles/wishlist.css";

function Wishlist() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);
  const [clearing, setClearing] = useState(false);

  const navigate = useNavigate();

  const formatPrice = (price) =>
    new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(Number(price) || 0);

  const fetchWishlist = useCallback(async (showLoader = true) => {
    try {
      if (showLoader) setLoading(true);

      setError("");

      const response = await api.get("/wishlist");

      setItems(response.data?.items || []);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Unable to load your wishlist."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchWishlist();

  }, [fetchWishlist]);

  const removeItem = async (productId) => {
    if (busyId !== null || clearing) return;

    try {
      setBusyId(productId);

      await api.delete(`/wishlist/remove/${productId}`);

      setItems((previous) =>
        previous.filter(
          (item) => item.productId !== productId
        )
      );

      setError("");
    } catch (err) {
      alert(
        err.response?.data?.message ||
        "Unable to remove product."
      );
    } finally {
      setBusyId(null);
    }
  };

  const clearWishlist = async () => {
    if (clearing || busyId !== null) return;

    if (
      !window.confirm(
        "Are you sure you want to clear your wishlist?"
      )
    ) {
      return;
    }

    try {
      setClearing(true);

      await api.delete("/wishlist/clear");

      setItems([]);
      setError("");
    } catch (err) {
      alert(
        err.response?.data?.message ||
        "Unable to clear wishlist."
      );
    } finally {
      setClearing(false);
    }
  };

  const moveToCart = async (item) => {
    if (busyId !== null || clearing) return;

    if (Number(item.stockQuantity) <= 0) {
      alert("This product is currently out of stock.");
      return;
    }

    try {
      setBusyId(item.productId);

      await api.post("/cart/add", {
        productId: item.productId,
        quantity: 1
      });

      await api.delete(
        `/wishlist/remove/${item.productId}`
      );

      setItems((previous) =>
        previous.filter(
          (product) =>
            product.productId !== item.productId
        )
      );

      alert("Product added to cart successfully!");
    } catch (err) {
      alert(
        err.response?.data?.message ||
        "Unable to move product to cart."
      );

      // If adding to cart succeeded but removing from
      // wishlist failed, refresh to show the actual state.
      await fetchWishlist(false);
    } finally {
      setBusyId(null);
    }
  };

  return (
    <>
      <Navbar />

      <main className="wishlist-page">

        <div className="wishlist-heading">
          <div>
            <span className="wishlist-eyebrow">
              YOUR COLLECTION
            </span>

            <h1>
              My Wishlist
              <Heart size={27} />
            </h1>

            <p>
              All the products you love, saved in one place.
            </p>
          </div>

          <button
            className="wishlist-refresh"
            onClick={() => fetchWishlist()}
            disabled={loading || busyId !== null || clearing}
          >
            <RefreshCw size={17} />
            Refresh
          </button>
        </div>

        {loading ? (
          <div className="wishlist-message">
            Loading your wishlist...
          </div>
        ) : error ? (
          <div className="wishlist-message">
            <p>{error}</p>

            <button onClick={() => fetchWishlist()}>
              Try Again
            </button>
          </div>
        ) : items.length === 0 ? (
          <div className="wishlist-empty">
            <Heart size={65} />

            <h2>Your wishlist is empty</h2>

            <p>
              Save products you love and find them here later.
            </p>

            <button onClick={() => navigate("/products")}>
              Explore Products
              <ArrowRight size={18} />
            </button>
          </div>
        ) : (
          <>
            <div className="wishlist-toolbar">
              <span>
                {items.length} saved product(s)
              </span>

              <button
                onClick={clearWishlist}
                disabled={clearing || busyId !== null}
              >
                <Trash2 size={16} />
                {clearing ? "Clearing..." : "Clear Wishlist"}
              </button>
            </div>

            <section className="wishlist-grid">

              {items.map((item) => {
                const outOfStock =
                  Number(item.stockQuantity) <= 0;

                return (
                  <article
                    className="wishlist-card"
                    key={item.id}
                  >

                    <div className="wishlist-image">
                      {item.imageUrl ? (
                        <img
                          src={item.imageUrl}
                          alt={item.productName || "Product"}
                          onError={(event) => {
                            event.currentTarget.onerror = null;
                            event.currentTarget.style.display = "none";
                          }}
                        />
                      ) : (
                        <ShoppingCart size={45} />
                      )}
                    </div>

                    <div className="wishlist-product-info">

                      <span className="wishlist-sku">
                        SKU: {item.sku || "N/A"}
                      </span>

                      <h3>{item.productName}</h3>

                      <strong>
                        {formatPrice(item.price)}
                      </strong>

                      <p
                        className={
                          outOfStock
                            ? "wishlist-stock-unavailable"
                            : "wishlist-stock-available"
                        }
                      >
                        {outOfStock
                          ? "Out of stock"
                          : `${item.stockQuantity} items in stock`}
                      </p>

                    </div>

                    <div className="wishlist-actions">

                      <button
                        className="wishlist-cart-btn"
                        disabled={
                          busyId !== null ||
                          clearing ||
                          outOfStock
                        }
                        onClick={() => moveToCart(item)}
                      >
                        <ShoppingCart size={17} />

                        {busyId === item.productId
                          ? "Please wait..."
                          : "Add to Cart"}
                      </button>

                      <button
                        className="wishlist-remove-btn"
                        disabled={
                          busyId !== null || clearing
                        }
                        onClick={() =>
                          removeItem(item.productId)
                        }
                        aria-label="Remove from wishlist"
                        title="Remove from wishlist"
                      >
                        <Trash2 size={18} />
                      </button>

                    </div>

                  </article>
                );
              })}

            </section>
          </>
        )}

      </main>
    </>
  );
}

export default Wishlist;
