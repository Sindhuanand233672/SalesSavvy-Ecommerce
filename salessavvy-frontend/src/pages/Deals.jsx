
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import api from "../services/api";
import { ArrowRight, TicketPercent, Tag, Clock3 } from "lucide-react";
import "../styles/home.css";

function Deals() {
  const [deals, setDeals] = useState([]);
  const [coupons, setCoupons] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    Promise.allSettled([
      api.get("/products"),
      api.get("/coupons/active"),
    ]).then((results) => {
      if (!active) return;

      const productResult = results[0];
      const couponResult = results[1];

      if (productResult.status === "fulfilled") {
        const data = productResult.value.data;
        const list = Array.isArray(data)
          ? data
          : data?.content || data?.products || [];

        const validDeals = list
          .map((product) => {
            const salePrice = Number(
              product.discountPrice ??
                product.discountedPrice ??
                product.salePrice ??
                product.offerPrice
            );

            const regularPrice = Number(
              product.originalPrice ?? product.price
            );

            return { ...product, salePrice, regularPrice };
          })
          .filter(
            (product) =>
              Number.isFinite(product.salePrice) &&
              Number.isFinite(product.regularPrice) &&
              product.salePrice > 0 &&
              product.regularPrice > product.salePrice
          );

        setDeals(validDeals);
      } else {
        setError("Unable to load products. Please try again later.");
      }

      if (couponResult.status === "fulfilled") {
        const data = couponResult.value.data;

        const list = Array.isArray(data)
          ? data
          : data?.content || data?.coupons || [];

        const now = Date.now();

        const validCoupons = list.filter((coupon) => {
          const isActive =
            coupon.active === true || coupon.active === "true";

          const startDate = coupon.startDate
            ? new Date(coupon.startDate).getTime()
            : null;

          const expiryDate = coupon.expiryDate
            ? new Date(coupon.expiryDate).getTime()
            : null;

          const withinStartDate =
            startDate === null ||
            (!Number.isNaN(startDate) && startDate <= now);

          const withinExpiryDate =
            expiryDate === null ||
            (!Number.isNaN(expiryDate) && expiryDate >= now);

          const usageAvailable =
            coupon.usageLimit == null ||
            coupon.usageCount == null ||
            Number(coupon.usageCount) < Number(coupon.usageLimit);

          return (
            isActive &&
            withinStartDate &&
            withinExpiryDate &&
            usageAvailable
          );
        });

        setCoupons(validCoupons);
      }

      setLoading(false);
    });

    return () => {
      active = false;
    };
  }, []);

  const formatPrice = (price) =>
    new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 0,
    }).format(price);

  return (
    <>
      <Navbar />

      <main className="ss-deals-page">
        <section className="ss-deals-hero">
          <div>
            <span className="ss-section-kicker">SALESSAVVY OFFERS</span>
            <h1>
              Deals worth
              <br />
              <span>looking for.</span>
            </h1>
            <p>
              Explore current price reductions and available coupon offers.
              Offer details are shown only when they are returned by the store.
            </p>
          </div>

          <div className="ss-deals-hero-art">
            <TicketPercent size={76} strokeWidth={1.2} />
            <span>Offers &amp; savings</span>
          </div>
        </section>

        <section className="ss-deals-content">
          <div className="ss-section-title">
            <div>
              <span className="ss-section-kicker">AVAILABLE NOW</span>
              <h2>Coupon offers</h2>
              <p>Check the terms before applying a coupon at checkout.</p>
            </div>
          </div>

          {loading && (
            <p className="ss-state-message">
              Checking available offers…
            </p>
          )}

          {!loading && coupons.length > 0 && (
            <div className="ss-coupon-grid">
              {coupons.map((coupon) => (
                <article className="ss-coupon-card" key={coupon.id}>
                  <div className="ss-coupon-icon">
                    <TicketPercent />
                  </div>

                  <div className="ss-coupon-info">
                    <span className="ss-coupon-label">ACTIVE COUPON</span>
                    <h3>{coupon.code}</h3>
                    <p>
                      {coupon.description ||
                        "Apply this coupon at checkout."}
                    </p>

                    <small>
                      {coupon.discountType === "PERCENTAGE"
                        ? `${coupon.discountValue}% off`
                        : `${formatPrice(Number(coupon.discountValue))} off`}

                      {Number(coupon.minimumOrderAmount) > 0
                        ? ` · Min. order ${formatPrice(
                            Number(coupon.minimumOrderAmount)
                          )}`
                        : ""}
                    </small>
                  </div>

                  <span className="ss-coupon-status">Active</span>
                </article>
              ))}
            </div>
          )}

          {!loading && coupons.length === 0 && (
            <div className="ss-no-offers">
              <div className="ss-no-offers-icon">
                <Tag />
              </div>

              <div>
                <h3>No active coupon offers right now</h3>
                <p>
                  We don't display made-up discounts. Browse the catalogue
                  and check back for store-published offers.
                </p>
                <Link to="/products">
                  Explore products <ArrowRight size={16} />
                </Link>
              </div>
            </div>
          )}

          <div className="ss-section-title ss-price-deals-heading">
            <div>
              <span className="ss-section-kicker">PRICE REDUCTIONS</span>
              <h2>Products on offer</h2>
              <p>
                Products with a lower sale price than their listed price.
              </p>
            </div>
          </div>

          {!loading && !error && deals.length === 0 && (
            <div className="ss-no-offers ss-no-price-deals">
              <div className="ss-no-offers-icon">
                <Clock3 />
              </div>

              <div>
                <h3>No marked-down products at the moment</h3>
                <p>
                  Explore all products to discover the current collection.
                </p>
                <Link to="/products">
                  Shop all products <ArrowRight size={16} />
                </Link>
              </div>
            </div>
          )}

          {!loading && error && (
            <p className="ss-state-message ss-error-message">{error}</p>
          )}

          {!loading && deals.length > 0 && (
            <div className="ss-deal-product-grid">
              {deals.map((product) => {
                const discount = Math.round(
                  ((product.regularPrice - product.salePrice) /
                    product.regularPrice) *
                    100
                );

                return (
                  <article className="ss-deal-product" key={product.id}>
                    <Link
                      to={`/products/${product.id}`}
                      className="ss-deal-product-image"
                    >
                      <img
                        src={
                          product.imageUrl ||
                          "https://placehold.co/500x500?text=Product"
                        }
                        alt={product.name}
                        onError={(event) => {
                          event.currentTarget.src =
                            "https://placehold.co/500x500?text=Product";
                        }}
                      />

                      <span>{discount}% OFF</span>
                    </Link>

                    <div className="ss-deal-product-info">
                      <small>
                        {product.category?.name ||
                          product.category ||
                          "Store pick"}
                      </small>

                      <Link to={`/products/${product.id}`}>
                        <h3>{product.name}</h3>
                      </Link>

                      <div>
                        <strong>{formatPrice(product.salePrice)}</strong>
                        <del>{formatPrice(product.regularPrice)}</del>
                      </div>
                    </div>
                  </article>
                );
              })}
            </div>
          )}
        </section>
      </main>

      <Footer />
    </>
  );
}

export default Deals;
