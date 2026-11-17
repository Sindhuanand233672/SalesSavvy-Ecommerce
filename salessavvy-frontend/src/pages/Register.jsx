
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../services/api";
import { useAuth } from "../context/AuthContext";

function Register() {
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    phoneNumber: "",
    password: ""
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await api.post("/auth/register", form);
      login(response.data);
      navigate("/");
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>Create Account</h1>
        <p>Join SalesSavvy today.</p>

        {error && <p className="auth-error">{error}</p>}

        <label>Full Name</label>
        <input required value={form.fullName}
          onChange={(e) => setForm({ ...form, fullName: e.target.value })}
          placeholder="Your full name" />

        <label>Email</label>
        <input type="email" required value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          placeholder="Your email" />

        <label>Phone Number</label>
        <input required value={form.phoneNumber}
          onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
          placeholder="10-digit phone number" />

        <label>Password</label>
        <input type="password" required minLength={8} value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          placeholder="Minimum 8 characters" />

        <button type="submit" disabled={loading}>
          {loading ? "Creating account..." : "Register"}
        </button>

        <p>Already registered? <Link to="/login">Login</Link></p>
      </form>
    </div>
  );
}

export default Register;
