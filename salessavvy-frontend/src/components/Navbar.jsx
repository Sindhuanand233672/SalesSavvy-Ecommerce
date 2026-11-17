
import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Search,
  ShoppingCart,
  Heart,
  UserRound,
  LogOut,
  X,
  ChevronDown,
  Menu,
  LayoutDashboard,
  Package,
  Tags,
  ClipboardList,
  TicketPercent,
  Users,
  Sun,
  Moon
} from "lucide-react";

import { useAuth } from "../context/AuthContext";
import "../styles/navbar.css";

function Navbar() {
  const { user, isLoggedIn, isAdmin, logout } = useAuth();

  const [searchOpen, setSearchOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [managementOpen, setManagementOpen] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [theme, setTheme] = useState(
    () => localStorage.getItem("salessavvy-theme") || "light"
  );

  const navigate = useNavigate();

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("salessavvy-theme", theme);
  }, [theme]);

  const handleSearch = (e) => {
    e.preventDefault();

    const query = searchTerm.trim();

    if (!query) return;

    navigate(`/products?search=${encodeURIComponent(query)}`);
    setSearchOpen(false);
    setSearchTerm("");
    setMobileOpen(false);
  };

  const closeMenus = () => {
    setManagementOpen(false);
    setMobileOpen(false);
  };

  const handleLogout = () => {
    logout();
    closeMenus();
    navigate("/");
  };

  return (
    <header className="ss-navbar">
      <div className="ss-nav-container">
        <Link to="/" className="ss-logo" onClick={closeMenus}>
          Sales<span>Savvy</span>.
        </Link>

        <nav className={`ss-nav-links ${mobileOpen ? "ss-nav-open" : ""}`}>
          <Link to="/" onClick={closeMenus}>Home</Link>
          <Link to="/products" onClick={closeMenus}>Shop</Link>
          <Link to="/categories" onClick={closeMenus}>Categories</Link>
          <Link to="/deals" onClick={closeMenus}>Deals</Link>

          {isLoggedIn && !isAdmin && (
            <Link to="/orders" onClick={closeMenus}>
              My Orders
            </Link>
          )}

          {isAdmin && (
            <div className="ss-management">
              <button
                type="button"
                className={`ss-management-trigger ${managementOpen ? "active" : ""}`}
                onClick={() => setManagementOpen((v) => !v)}
                aria-expanded={managementOpen}
              >
                Management <ChevronDown size={16} />
              </button>

              {managementOpen && (
                <div className="ss-management-menu">
                  <Link to="/admin/dashboard" onClick={closeMenus}>
                    <LayoutDashboard size={17} /> Dashboard
                  </Link>

                  <Link to="/admin/orders" onClick={closeMenus}>
                    <ClipboardList size={17} /> Order Management
                  </Link>

                  <Link to="/admin/products" onClick={closeMenus}>
                    <Package size={17} /> Product Management
                  </Link>

                  <Link to="/admin/categories" onClick={closeMenus}>
                    <Tags size={17} /> Category Management
                  </Link>

                  <Link to="/admin/coupons" onClick={closeMenus}>
                    <TicketPercent size={17} /> Coupon Management
                  </Link>

                  <Link to="/admin/customers" onClick={closeMenus}>
                    <Users size={17} /> Customer Management
                  </Link>

                  <Link to="/admin/reviews" onClick={closeMenus}>
                    <ClipboardList size={17} /> Review Management
                  </Link>
                </div>
              )}
            </div>
          )}
        </nav>

        <div className="ss-nav-actions">
          <button
            type="button"
            className="ss-theme-toggle"
            aria-label={`Switch to ${theme === "light" ? "dark" : "light"} mode`}
            title={`Switch to ${theme === "light" ? "dark" : "light"} mode`}
            onClick={() =>
              setTheme((current) =>
                current === "light" ? "dark" : "light"
              )
            }
          >
            {theme === "light" ? <Moon /> : <Sun />}
          </button>

          <button
            type="button"
            aria-label="Search"
            onClick={() => setSearchOpen((v) => !v)}
          >
            {searchOpen ? <X /> : <Search />}
          </button>

          {isLoggedIn ? (
            <>
              {!isAdmin && (
                <>
                  <Link to="/wishlist" aria-label="Wishlist">
                    <Heart />
                  </Link>

                  <Link to="/cart" aria-label="Cart">
                    <ShoppingCart />
                  </Link>
                </>
              )}

              <span className="ss-user-label">
                {user?.fullName || (isAdmin ? "Admin" : "Account")}
              </span>

              <button
                type="button"
                className="ss-auth-action"
                onClick={handleLogout}
                aria-label="Sign out"
              >
                <LogOut size={17} />
                <span>Sign Out</span>
              </button>
            </>
          ) : (
            <Link
              to="/login"
              className="ss-auth-action"
              aria-label="Sign in"
            >
              <UserRound size={17} />
              <span>Sign In</span>
            </Link>
          )}

          <button
            type="button"
            className="ss-mobile-toggle"
            aria-label="Toggle navigation"
            onClick={() => setMobileOpen((v) => !v)}
          >
            <Menu />
          </button>
        </div>
      </div>

      {searchOpen && (
        <form className="ss-search-form" onSubmit={handleSearch}>
          <input
            type="search"
            placeholder="Search products by name, SKU or description..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            autoFocus
          />

          <button type="submit">
            <Search size={18} /> Search
          </button>
        </form>
      )}
    </header>
  );
}

export default Navbar;
