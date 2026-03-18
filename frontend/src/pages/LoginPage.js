import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';

function LoginPage() {
  const [formData, setFormData] = useState({ username: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  useEffect(() => {
    document.title = 'Login | ResolveIT';
  }, []);

  const handleChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.username.trim() || !formData.password.trim()) {
      toast.error('Please enter both username and password');
      return;
    }

    setLoading(true);
    try {
      const response = await axios.post('http://localhost:8080/api/auth/login', {
        username: formData.username,
        password: formData.password
      });

      if (response.data.success) {
        login(response.data);
        toast.success('Welcome back!');
        navigate(response.data.role === 'ADMIN' ? '/admin' : '/my-complaints');
      } else {
        toast.error(response.data.message || 'Login failed');
      }
    } catch (error) {
      if (error.response?.status === 401) toast.error('Invalid username or password');
      else if (error.response?.status === 500) toast.error('Server error. Please try again later.');
      else if (error.request) toast.error('Network error. Please check your connection.');
      else toast.error(error.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card fade-in">
        <div className="auth-brand" aria-hidden="true">
          <h1>ResolveIT</h1>
          <p>Smart Grievance & Feedback Management</p>
          <ul>
            <li>Role-based dashboards</li>
            <li>Status updates & escalation</li>
            <li>Attachments & comments</li>
          </ul>
        </div>

        <div className="auth-form">
          <h2>Sign in</h2>
          <p className="muted">Use your account to submit and track complaints.</p>

          <form onSubmit={handleSubmit}>
            <div className="field">
              <label htmlFor="username">Username</label>
              <input
                id="username"
                className="form-control"
                name="username"
                value={formData.username}
                onChange={handleChange}
                autoComplete="username"
                placeholder="Enter your username"
                disabled={loading}
              />
            </div>

            <div className="field">
              <label htmlFor="password">Password</label>
              <div className="input-row">
                <input
                  id="password"
                  className="form-control"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  value={formData.password}
                  onChange={handleChange}
                  autoComplete="current-password"
                  placeholder="Enter your password"
                  disabled={loading}
                />
                <button
                  type="button"
                  className="input-action"
                  onClick={() => setShowPassword((v) => !v)}
                  disabled={loading}
                >
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>
            </div>

            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? (
                  <>
                    <span className="spinner-border" style={{ width: 18, height: 18, borderWidth: 3 }} />
                    Signing in…
                  </>
                ) : (
                  'Sign in'
                )}
              </button>
              <button type="button" className="btn btn-outline" onClick={() => navigate('/register')} disabled={loading}>
                Create account
              </button>
            </div>

            <div className="form-footer">
              <span>
                New here? <Link className="link" to="/register">Create an account</Link>
              </span>
              <Link className="link" to="/">Back home</Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
