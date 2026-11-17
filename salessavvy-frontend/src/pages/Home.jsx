import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import ProductCard from "../components/ProductCard";
import api from "../services/api";
import { ArrowRight, Truck, ShieldCheck, Headphones, RefreshCw, Sparkles } from "lucide-react";
import "../styles/home.css";

const categories = [
  { name: "Electronics", icon: "💻", note: "Phones, laptops & more" },
  { name: "Fashion", icon: "👕", note: "Everyday styles" },
  { name: "Accessories", icon: "⌚", note: "The finishing touch" },
  { name: "Home Appliances", icon: "🏠", note: "Made for your home" },
  { name: "Audio", icon: "🎧", note: "Listen your way" },
];

function Home() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;
    api.get("/products")
      .then(({ data }) => {
        const list = Array.isArray(data) ? data : data?.content || [];
        if (active) setProducts(list.map((product) => ({
          ...product,
          image: product.imageUrl || product.image || "",
          category: typeof product.category === "string" ? product.category : product.category?.name || "General",
        })));
      })
      .catch((err) => {
        console.error("Product loading error:", err);
        if (active) setError("We couldn't load products right now. Please try again.");
      })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  const featuredProducts = products.slice(0, 8);

  return <>
    <Navbar />
    <main className="ss-home">
      <section className="ss-home-hero">
        <div className="ss-hero-copy">
          <span className="ss-kicker"><Sparkles size={15} /> THE SMARTER WAY TO SHOP</span>
          <h1>Good finds.<br /><span>Better everyday.</span></h1>
          <p>Explore thoughtfully selected products across technology, fashion and home essentials — all in one place.</p>
          <div className="ss-hero-actions">
            <Link to="/products" className="ss-primary-cta">Shop all products <ArrowRight size={17} /></Link>
            <Link to="/deals" className="ss-secondary-cta">Explore deals</Link>
          </div>
          <div className="ss-trust-line"><span><ShieldCheck size={16} /> Secure checkout</span><span><Truck size={16} /> Convenient delivery</span></div>
        </div>
        <div className="ss-hero-visual">
          <img src="
https://images.unsplash.com/photo-1498049794561-7780e7231661?w=1200&auto=format&fit=crop&q=85
" alt="A curated selection of shopping finds" />
          <div className="ss-hero-note"><span className="ss-note-icon">✦</span><span><strong>Find something you love</strong><small>Browse the latest collection</small></span></div>
        </div>
      </section>

      <section className="ss-service-strip" aria-label="Shopping benefits">
        <div><Truck /><span><strong>Convenient delivery</strong><small>Delivery details at checkout</small></span></div>
        <div><ShieldCheck /><span><strong>Secure shopping</strong><small>Protected account access</small></span></div>
        <div><Headphones /><span><strong>Customer support</strong><small>Help when you need it</small></span></div>
        <div><RefreshCw /><span><strong>Order updates</strong><small>Track your purchases</small></span></div>
      </section>

      <section className="ss-home-section ss-category-section">
        <div className="ss-section-title"><div><span className="ss-section-kicker">SHOP YOUR WAY</span><h2>Browse by category</h2><p>Start with what you're looking for.</p></div><Link to="/categories">All categories <ArrowRight size={16} /></Link></div>
        <div className="ss-category-row">{categories.map((category) => <button type="button" className="ss-category-tile" key={category.name} onClick={() => navigate(`/products?search=${encodeURIComponent(category.name)}`)}><span className="ss-category-icon">{category.icon}</span><span className="ss-category-text"><strong>{category.name}</strong><small>{category.note}</small></span><ArrowRight size={17} /></button>)}</div>
      </section>

      <section className="ss-home-section ss-featured-section" id="featured-products">
        <div className="ss-section-title"><div><span className="ss-section-kicker">A PLACE TO START</span><h2>Featured products</h2><p>A small selection from our catalogue.</p></div><Link to="/products">View all products <ArrowRight size={16} /></Link></div>
        {loading && <p className="ss-state-message">Loading featured products…</p>}
        {!loading && error && <p className="ss-state-message ss-error-message">{error}</p>}
        {!loading && !error && featuredProducts.length === 0 && <p className="ss-state-message">No products are available at the moment.</p>}
        {!loading && !error && featuredProducts.length > 0 && <div className="product-grid ss-featured-grid">{featuredProducts.map((product) => <ProductCard product={product} key={product.id} />)}</div>}
        {!loading && products.length > featuredProducts.length && <div className="ss-browse-all"><span>Looking for something specific? Explore the complete catalogue.</span><Link to="/products">Browse all {products.length} products <ArrowRight size={16} /></Link></div>}
      </section>
      <section className="ss-bottom-promo"><div><span className="ss-section-kicker">YOUR NEXT GREAT FIND</span><h2>More choice. Less searching.</h2><p>Explore the full SalesSavvy collection and find what fits your everyday.</p></div><Link to="/products" className="ss-primary-cta">Explore the shop <ArrowRight size={17} /></Link></section>
    </main>
    <Footer />
  </>;
}

export default Home;
