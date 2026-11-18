
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import ProductCard from "../components/ProductCard";
import api from "../services/api";
import "../styles/home.css";

const PRODUCTS_PER_PAGE = 6;

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
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [currentPage, setCurrentPage] = useState(1);

  // Keep search and category synchronized with the URL.
  useEffect(() => {
    setSearchTerm(searchParams.get("search") || "");
    setSelectedCategory(searchParams.get("category") || "All");
    setCurrentPage(1);
  }, [searchParams]);

  // Fetch products from the backend.
  useEffect(() => {
    let isMounted = true;

    const fetchProducts = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await api.get("/products");
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
              : product.category?.name || "General",
        }));

        if (isMounted) {
          setProducts(formattedProducts);
        }
      } catch (err) {
        console.error("Product loading error:", err);

        if (isMounted) {
          setError("Unable to load products. Please try again.");
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    fetchProducts();

    return () => {
      isMounted = false;
    };
  }, []);

  // Get available product categories.
  const categories = useMemo(() => {
    return [
      "All",
      ...new Set(products.map((product) => product.category)),
    ];
  }, [products]);

  // Filter and sort products.
  const filteredProducts = useMemo(() => {
    let result = [...products];

    // Search by product name, SKU or description.
    if (searchTerm.trim()) {
      const search = searchTerm.trim().toLowerCase();

      result = result.filter(
        (product) =>
          product.name?.toLowerCase().includes(search) ||
          product.sku?.toLowerCase().includes(search) ||
          product.description?.toLowerCase().includes(search)
      );
    }

    // Filter by category.
    if (selectedCategory !== "All") {
      result = result.filter(
        (product) => product.category === selectedCategory
      );
    }

    // Filter by minimum price.
    if (minPrice !== "") {
      const minimum = Number(minPrice);

      result = result.filter(
        (product) => Number(product.price) >= minimum
      );
    }

    // Filter by maximum price.
    if (maxPrice !== "") {
      const maximum = Number(maxPrice);

      result = result.filter(
        (product) => Number(product.price) <= maximum
      );
    }

    // Sorting.
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
    } else if (sortBy === "nameAZ") {
      result.sort((a, b) =>
        (a.name || "").localeCompare(b.name || "")
      );
    } else if (sortBy === "nameZA") {
      result.sort((a, b) =>
        (b.name || "").localeCompare(a.name || "")
      );
    }

    return result;
  }, [
    products,
    searchTerm,
    selectedCategory,
    minPrice,
    maxPrice,
    sortBy,
  ]);

  // Calculate pagination.
  const totalPages = Math.ceil(
    filteredProducts.length / PRODUCTS_PER_PAGE
  );

  const paginatedProducts = useMemo(() => {
    const startIndex = (currentPage - 1) * PRODUCTS_PER_PAGE;

    return filteredProducts.slice(
      startIndex,
      startIndex + PRODUCTS_PER_PAGE
    );
  }, [filteredProducts, currentPage]);

  // Keep the current page valid if the result count changes.
  useEffect(() => {
    if (totalPages > 0 && currentPage > totalPages) {
      setCurrentPage(totalPages);
    } else if (totalPages === 0 && currentPage !== 1) {
      setCurrentPage(1);
    }
  }, [currentPage, totalPages]);

  // Update search and URL.
  const updateSearch = (value) => {
    setSearchTerm(value);
    setCurrentPage(1);

    const next = new URLSearchParams(searchParams);

    if (value.trim()) {
      next.set("search", value);
    } else {
      next.delete("search");
    }

    setSearchParams(next, { replace: true });
  };

  // Update category and URL.
  const updateCategory = (value) => {
    setSelectedCategory(value);
    setCurrentPage(1);

    const next = new URLSearchParams(searchParams);

    if (value !== "All") {
      next.set("category", value);
    } else {
      next.delete("category");
    }

    setSearchParams(next, { replace: true });
  };

  // Update sorting.
  const updateSort = (value) => {
    setSortBy(value);
    setCurrentPage(1);
  };

  // Update price filters.
  const updateMinPrice = (value) => {
    setMinPrice(value);
    setCurrentPage(1);
  };

  const updateMaxPrice = (value) => {
    setMaxPrice(value);
    setCurrentPage(1);
  };

  // Clear all filters.
  const clearFilters = () => {
    setSearchTerm("");
    setSelectedCategory("All");
    setSortBy("default");
    setMinPrice("");
    setMaxPrice("");
    setCurrentPage(1);
    setSearchParams({});
  };

  const hasInvalidPriceRange =
    minPrice !== "" &&
    maxPrice !== "" &&
    Number(minPrice) > Number(maxPrice);

  const firstProduct =
    filteredProducts.length === 0
      ? 0
      : (currentPage - 1) * PRODUCTS_PER_PAGE + 1;

  const lastProduct = Math.min(
    currentPage * PRODUCTS_PER_PAGE,
    filteredProducts.length
  );

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
            {/* Search */}
            <div className="product-search">
              <input
                type="text"
                placeholder="Search products by name, SKU or description..."
                value={searchTerm}
                onChange={(e) => updateSearch(e.target.value)}
              />
            </div>

            {/* Category and sorting */}
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
                onChange={(e) => updateSort(e.target.value)}
              >
                <option value="default">Sort By</option>
                <option value="lowToHigh">
                  Price: Low to High
                </option>
                <option value="highToLow">
                  Price: High to Low
                </option>
                <option value="nameAZ">
                  Name: A to Z
                </option>
                <option value="nameZA">
                  Name: Z to A
                </option>
                <option value="newest">
                  Newest First
                </option>
              </select>
            </div>

            {/* Price range */}
            <div className="price-filter-group">
              <span className="price-filter-label">
                Filter by price
              </span>

              <div className="price-filter-inputs">
                <input
                  type="number"
                  min="0"
                  placeholder="Minimum price (₹)"
                  value={minPrice}
                  onChange={(e) => updateMinPrice(e.target.value)}
                />

                <span className="price-range-separator">
                  to
                </span>

                <input
                  type="number"
                  min="0"
                  placeholder="Maximum price (₹)"
                  value={maxPrice}
                  onChange={(e) => updateMaxPrice(e.target.value)}
                />
              </div>

              {hasInvalidPriceRange && (
                <p className="price-filter-error">
                  Minimum price cannot be greater than maximum price.
                </p>
              )}
            </div>

            <div className="filter-actions">
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

        {!loading && error && (
          <div className="products-error">
            <p>{error}</p>
            <button onClick={() => window.location.reload()}>
              Try Again
            </button>
          </div>
        )}

        {!loading && !error && (
          <p className="product-result-count">
            Showing {firstProduct}–{lastProduct} of{" "}
            {filteredProducts.length} products
          </p>
        )}

        {!loading &&
          !error &&
          !hasInvalidPriceRange &&
          filteredProducts.length === 0 && (
            <div className="no-products">
              <h3>No products found</h3>
              <p>
                Try changing your search, category or price range.
              </p>
              <button onClick={clearFilters}>
                Clear Filters
              </button>
            </div>
          )}

        {!loading &&
          !error &&
          !hasInvalidPriceRange &&
          filteredProducts.length > 0 && (
            <>
              <div className="product-grid">
                {paginatedProducts.map((product) => (
                  <ProductCard
                    product={product}
                    key={product.id}
                  />
                ))}
              </div>

              {totalPages > 1 && (
                <nav
                  className="products-pagination"
                  aria-label="Product pages"
                >
                  <button
                    className="pagination-btn"
                    onClick={() =>
                      setCurrentPage((page) => page - 1)
                    }
                    disabled={currentPage === 1}
                  >
                    Previous
                  </button>

                  <div className="pagination-numbers">
                    {Array.from(
                      { length: totalPages },
                      (_, index) => index + 1
                    ).map((page) => (
                      <button
                        key={page}
                        className={`pagination-number ${
                          currentPage === page ? "active" : ""
                        }`}
                        onClick={() => setCurrentPage(page)}
                        aria-current={
                          currentPage === page ? "page" : undefined
                        }
                      >
                        {page}
                      </button>
                    ))}
                  </div>

                  <button
                    className="pagination-btn"
                    onClick={() =>
                      setCurrentPage((page) => page + 1)
                    }
                    disabled={currentPage === totalPages}
                  >
                    Next
                  </button>
                </nav>
              )}
            </>
          )}

        {!loading && !error && hasInvalidPriceRange && (
          <div className="no-products">
            <h3>Invalid price range</h3>
            <p>
              Please enter a minimum price that is less than or
              equal to the maximum price.
            </p>
          </div>
        )}
      </main>

      <Footer />
    </>
  );
}

export default Products;
