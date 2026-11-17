import { useState } from "react";
import { Heart, ShoppingCart } from "lucide-react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import { useAuth } from "../context/AuthContext";

function ProductCard({ product }) {
  const navigate = useNavigate();
  const { isLoggedIn, isAdmin } = useAuth();

  const [adding, setAdding] = useState(false);
  const [addingToWishlist, setAddingToWishlist] = useState(false);

  const handleAddToCart = async () => {
    if (!isLoggedIn) {
      navigate("/login");
      return;
    }

    if (isAdmin) return;

    try {
      setAdding(true);
      await api.post("/cart/add", {
        productId: product.id,
        quantity: 1
      });
      alert("Product added to cart successfully!");
    } catch (error) {
      console.error("Add to cart error:", error);
      alert(
        error.response?.data?.message ||
        "Unable to add product to cart. Please try again."
      );
    } finally {
      setAdding(false);
    }
  };

  const handleAddToWishlist = async () => {
    if (!isLoggedIn) {
      navigate("/login");
      return;
    }

    if (isAdmin) return;

    try {
      setAddingToWishlist(true);
      await api.post(`/wishlist/add/${product.id}`);
      alert("Product added to wishlist successfully!");
    } catch (error) {
      console.error("Add to wishlist error:", error);
      alert(
        error.response?.data?.message ||
        "Unable to add product to wishlist. Please try again."
      );
    } finally {
      setAddingToWishlist(false);
    }
  };

  return (
    <div className="product-card">
      <div className="product-image">
        <img
          src={
            product.imageUrl ||
            product.image ||
            "https://placehold.co/400x400?text=Product"
          }
          alt={product.name}
          style={{ cursor: "pointer" }}
          onClick={() => navigate(`/products/${product.id}`)}
          onError={(event) => {
            event.currentTarget.onerror = null;
            event.currentTarget.src =
              "https://placehold.co/400x400?text=Product";
          }}
        />

        {!isAdmin && (
          <button
            className="wishlist-btn"
            aria-label="Add to wishlist"
            onClick={handleAddToWishlist}
            disabled={addingToWishlist}
          >
            <Heart size={19} fill="none" />
          </button>
        )}
      </div>

      <div className="product-info">
        <p className="product-category">
          {product.category?.name || product.category || "General"}
        </p>

        <h3
          style={{ cursor: "pointer" }}
          onClick={() => navigate(`/products/${product.id}`)}
        >
          {product.name}
        </h3>

        <div className="product-bottom">
          <strong>
            ₹{Number(product.price).toLocaleString("en-IN")}
          </strong>

          {!isAdmin && (
            <button
              className="add-cart-btn"
              aria-label="Add to cart"
              onClick={handleAddToCart}
              disabled={adding}
            >
              <ShoppingCart size={18} />
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export default ProductCard;
