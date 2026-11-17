
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Minus,
  Plus,
  Trash2,
  ShoppingBag,
  ArrowRight
} from "lucide-react";

import api from "../services/api";
import Navbar from "../components/Navbar";

function Cart() {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [updating, setUpdating] = useState(null);
  const [clearing, setClearing] = useState(false);

  const navigate = useNavigate();

  const fallbackImages = {
    1: "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=500",
    2: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500"
  };

  const defaultImage =
    "https://placehold.co/400x400?text=Product";

  const fetchCart = useCallback(async (showLoader = true) => {
    try {
      if (showLoader) setLoading(true);

      setError("");

      const response = await api.get("/cart");
      setCart(response.data);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Unable to load your cart. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const formatPrice = (amount) =>
    new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(Number(amount) || 0);

  const getProductImage = (item) =>
    item.imageUrl ||
    fallbackImages[item.productId] ||
    defaultImage;

  const updateQuantity = async (itemId, quantity) => {
    if (quantity < 1 || updating !== null) return;

    try {
      setUpdating(itemId);

      const response = await api.put(
        `/cart/update/${itemId}`,
        null,
        {
          params: { quantity }
        }
      );

      setCart(response.data);
      setError("");
    } catch (err) {
      alert(
        err.response?.data?.message ||
        "Unable to update quantity."
      );
    } finally {
      setUpdating(null);
    }
  };

  const removeItem = async (itemId) => {
    if (updating !== null) return;

    if (!window.confirm("Remove this product from your cart?")) {
      return;
    }

    try {
      setUpdating(itemId);

      await api.delete(`/cart/remove/${itemId}`);

      await fetchCart(false);
    } catch (err) {
      alert(
        err.response?.data?.message ||
        "Unable to remove item."
      );
    } finally {
      setUpdating(null);
    }
  };

  const clearCart = async () => {
    if (clearing) return;

    if (!window.confirm("Are you sure you want to clear your cart?")) {
      return;
    }

    try {
      setClearing(true);

      await api.delete("/cart/clear");

      await fetchCart(false);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Unable to clear cart."
      );
    } finally {
      setClearing(false);
    }
  };

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="cart-message">
          Loading your cart...
        </div>
      </>
    );
  }

  if (error && !cart) {
    return (
      <>
        <Navbar />

        <div className="cart-message">
          <p>{error}</p>

          <button onClick={() => fetchCart()}>
            Try Again
          </button>
        </div>
      </>
    );
  }

  const items = cart?.items || [];
  const totalItems = cart?.totalItems || 0;
  const totalAmount = cart?.totalAmount || 0;

  return (
    <>
      <Navbar />

      <main className="cart-page">
        <div className="cart-heading">
          <div>
            <span className="cart-eyebrow">
              YOUR SHOPPING BAG
            </span>

            <h1>My Cart</h1>

            <p>
              {totalItems} items in your cart
            </p>
          </div>

          {items.length > 0 && (
            <button
              className="clear-cart-btn"
              onClick={clearCart}
              disabled={clearing || updating !== null}
            >
              <Trash2 size={16} />
              {clearing ? "Clearing..." : "Clear Cart"}
            </button>
          )}
        </div>

        {error && (
          <div className="cart-message">
            {error}
          </div>
        )}

        {items.length === 0 ? (
          <div className="empty-cart">
            <ShoppingBag size={65} />

            <h2>Your cart is empty</h2>

            <p>
              Looks like you haven't added anything yet.
            </p>

            <button onClick={() => navigate("/products")}>
              Continue Shopping
              <ArrowRight size={18} />
            </button>
          </div>
        ) : (
          <div className="cart-layout">
            <section className="cart-items">
              {items.map((item) => (
                <article
                  className="cart-item"
                  key={item.id}
                >
                  <img
                    src={getProductImage(item)}
                    alt={item.productName || "Product"}
                    onError={(event) => {
                      event.currentTarget.onerror = null;
                      event.currentTarget.src = defaultImage;
                    }}
                  />

                  <div className="cart-item-details">
                    <span className="cart-sku">
                      {item.sku}
                    </span>

                    <h3>{item.productName}</h3>

                    <p className="cart-item-price">
                      {formatPrice(item.price)}
                    </p>

                    <div className="quantity-control">
                      <button
                        disabled={
                          item.quantity <= 1 ||
                          updating !== null ||
                          clearing
                        }
                        onClick={() =>
                          updateQuantity(
                            item.id,
                            item.quantity - 1
                          )
                        }
                        aria-label="Decrease quantity"
                      >
                        <Minus size={15} />
                      </button>

                      <span>
                        {item.quantity}
                      </span>

                      <button
                        disabled={
                          updating !== null ||
                          clearing
                        }
                        onClick={() =>
                          updateQuantity(
                            item.id,
                            item.quantity + 1
                          )
                        }
                        aria-label="Increase quantity"
                      >
                        <Plus size={15} />
                      </button>
                    </div>
                  </div>

                  <div className="cart-item-actions">
                    <strong>
                      {formatPrice(item.subtotal)}
                    </strong>

                    <button
                      className="remove-item-btn"
                      disabled={
                        updating !== null ||
                        clearing
                      }
                      onClick={() => removeItem(item.id)}
                    >
                      <Trash2 size={17} />
                      Remove
                    </button>
                  </div>
                </article>
              ))}
            </section>

            <aside className="cart-summary">
              <h2>Order Summary</h2>

              <div className="summary-row">
                <span>Total Items</span>
                <span>{totalItems}</span>
              </div>

              <div className="summary-row">
                <span>Subtotal</span>
                <span>{formatPrice(totalAmount)}</span>
              </div>

              <div className="summary-row">
                <span>Shipping</span>
                <span className="free-shipping">
                  FREE
                </span>
              </div>

              <div className="summary-divider" />

              <div className="summary-total">
                <span>Total</span>
                <strong>
                  {formatPrice(totalAmount)}
                </strong>
              </div>

              <button
                className="checkout-btn"
                disabled={updating !== null || clearing}
                onClick={() => navigate("/checkout")}
              >
                Proceed to Checkout
                <ArrowRight size={18} />
              </button>

              <button
                className="continue-shopping-btn"
                onClick={() => navigate("/products")}
              >
                Continue Shopping
              </button>
            </aside>
          </div>
        )}
      </main>
    </>
  );
}

export default Cart;
