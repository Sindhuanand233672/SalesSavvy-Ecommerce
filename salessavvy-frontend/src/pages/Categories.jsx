
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import api from "../services/api";
import "../styles/home.css";

function Categories() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await api.get("/products");

        const products = Array.isArray(response.data)
          ? response.data
          : response.data?.content || [];

        const categoryMap = new Map();

        products.forEach((product) => {
          const category =
            typeof product.category === "string"
              ? product.category
              : product.category?.name || "General";

          categoryMap.set(
            category,
            (categoryMap.get(category) || 0) + 1
          );
        });

        setCategories(
          Array.from(categoryMap, ([name, count]) => ({
            name,
            count
          }))
        );
      } catch (err) {
        console.error("Category loading error:", err);
        setError("Unable to load categories.");
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, []);

  return (
    <>
      <Navbar />

      <main className="section products-section">
        <div className="section-heading">
          <div>
            <span className="eyebrow">FIND WHAT YOU NEED</span>
            <h2>Shop by Category</h2>
            <p>Explore our product collections.</p>
          </div>
        </div>

        {loading && <p>Loading categories...</p>}

        {!loading && error && <p>{error}</p>}

        {!loading && !error && categories.length > 0 && (
          <div className="category-grid">
            {categories.map((category) => (
              <button
                type="button"
                className="category-card"
                key={category.name}
                onClick={() =>
                  navigate(
                    `/products?category=${encodeURIComponent(category.name)}`
                  )
                }
              >
                <h3>{category.name}</h3>
                <p>{category.count} product(s)</p>
                <span>Explore →</span>
              </button>
            ))}
          </div>
        )}

        {!loading && !error && categories.length === 0 && (
          <p>No categories available yet.</p>
        )}
      </main>

      <Footer />
    </>
  );
}

export default Categories;
