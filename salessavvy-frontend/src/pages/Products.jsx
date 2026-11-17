
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import ProductCard from "../components/ProductCard";
import api from "../services/api";
import "../styles/home.css";

function Products() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [searchParams, setSearchParams] = useSearchParams();

  const [searchTerm, setSearchTerm] = useState(
    searchParams.get("search") || ""
  );

  const [selectedCategory, setSelectedCategory] = useState(
    searchParams.get("category") || "All"
  );

  const [sortBy, setSortBy] = useState("default");

  // Sync filters with URL parameters
  useEffect(() => {
    setSearchTerm(searchParams.get("search") || "");
    setSelectedCategory(searchParams.get("category") || "All");
  }, [searchParams]);

  // Fetch products
  useEffect(() => {
    api.get("/products")
      .then((response) => {
        const data = response.data;

        const productList = Array.isArray(data)
          ? data
          : Array.isArray(data?.content)
          ? data.content
          : [];

        const formattedProducts = productList.map((product) => ({
          ...product,
          image: product.imageUrl,
          category:
            typeof product.category === "string"
              ? product.category
              : product.category?.name || "General"
        }));

        setProducts(formattedProducts);
      })
      .catch((err) => {
        console.error("Product loading error:", err);
        setError("Unable to load products. Please try again.");
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  // Get available categories
  const categories = useMemo(() => {
    return [
      "All",
      ...new Set(products.map((product) => product.category))
    ];
  }, [products]);

  // Search, filter and sort products
  const filteredProducts = useMemo(() => {
    let result = [...products];

    // Search by name, SKU or description
    if (searchTerm.trim()) {
      const search = searchTerm.trim().toLowerCase();

      result = result.filter((product) =>
        product.name?.toLowerCase().includes(search) ||
        product.sku?.toLowerCase().includes(search) ||
        product.description?.toLowerCase().includes(search)
      );
    }

    // Filter by category
    if (selectedCategory !== "All") {
      result = result.filter(
        (product) => product.category === selectedCategory
      );
    }

    // Sort products
    if (sortBy === "lowToHigh") {
      result.sort(
        (a, b) => Number(a.price) - Number(b.price)
      );
    } else if (sortBy === "highToLow") {
      result.sort(
        (a, b) => Number(b.price) - Number(a.price)
      );
    } else if (sortBy === "newest") {
      result.sort(
        (a, b) =>
          new Date(b.createdAt || 0) -
          new Date(a.createdAt || 0)
      );
    }

    return result;
  }, [products, searchTerm, selectedCategory, sortBy]);

  // Update search and URL
  const updateSearch = (value) => {
    setSearchTerm(value);

    const next = new URLSearchParams(searchParams);

    if (value.trim()) {
      next.set("search", value);
    } else {
      next.delete("search");
    }

    setSearchParams(next, { replace: true });
  };

  // Update category and URL
  const updateCategory = (value) => {
    setSelectedCategory(value);

    const next = new URLSearchParams(searchParams);

    if (value !== "All") {
      next.set("category", value);
    } else {
      next.delete("category");
    }

    setSearchParams(next, { replace: true });
  };

  // Clear all filters
  const clearFilters = () => {
    setSearchTerm("");
    setSelectedCategory("All");
    setSortBy("default");
    setSearchParams({});
  };

  return (
    <>
      <Navbar />

      <main className="section products-section">
        <div className="section-heading">
          <div>
            <span className="eyebrow">
              EXPLORE OUR COLLECTION
            </span>
            <h2>All Products</h2>
          </div>
        </div>

        {!loading && !error && (
          <div className="product-filters">
            <div className="product-search">
              <input
                type="text"
                placeholder="Search products by name, SKU..."
                value={searchTerm}
                onChange={(e) => updateSearch(e.target.value)}
              />
            </div>

            <div className="filter-group">
              <select
                value={selectedCategory}
                onChange={(e) => updateCategory(e.target.value)}
              >
                {categories.map((category) => (
                  <option key={category} value={category}>
                    {category === "All"
                      ? "All Categories"
                      : category}
                  </option>
                ))}
              </select>

              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
              >
                <option value="default">Sort By</option>
                <option value="lowToHigh">
                  Price: Low to High
                </option>
                <option value="highToLow">
                  Price: High to Low
                </option>
                <option value="newest">
                  Newest First
                </option>
              </select>

              <button
                className="clear-filter-btn"
                onClick={clearFilters}
              >
                Clear Filters
              </button>
            </div>
          </div>
        )}

        {loading && <p>Loading products...</p>}

        {!loading && error && <p>{error}</p>}

        {!loading && !error && (
          <p className="product-result-count">
            Showing {filteredProducts.length} of{" "}
            {products.length} products
          </p>
        )}

        {!loading &&
          !error &&
          filteredProducts.length === 0 && (
            <div className="no-products">
              <h3>No products found</h3>
              <p>Try changing your search or filters.</p>
              <button onClick={clearFilters}>
                Clear Filters
              </button>
            </div>
          )}

        {!loading &&
          !error &&
          filteredProducts.length > 0 && (
            <div className="product-grid">
              {filteredProducts.map((product) => (
                <ProductCard
                  product={product}
                  key={product.id}
                />
              ))}
            </div>
          )}
      </main>

      <Footer />
    </>
  );
}

export default Products;
