import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

function RegisterPage() {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    document.title = 'Register | ResolveIT';
  }, []);

  const passwordStrength = useMemo(() => {
    const password = formData.password || '';
    let strength = 0;
    if (password.length >= 8) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[a-z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[^A-Za-z0-9]/.test(password)) strength++;
    return Math.min(strength, 4);
  }, [formData.password]);

  const strengthPct = (passwordStrength / 4) * 100;
  const strengthMeta = useMemo(() => {
    if (passwordStrength <= 1) return { label: 'Weak', color: 'rgba(239, 68, 68, 0.85)' };
    if (passwordStrength === 2) return { label: 'Fair', color: 'rgba(245, 158, 11, 0.85)' };
    if (passwordStrength === 3) return { label: 'Good', color: 'rgba(37, 99, 235, 0.80)' };
    return { label: 'Strong', color: 'rgba(22, 163, 74, 0.85)' };
  }, [passwordStrength]);

  const handleChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const validateForm = () => {
    if (!formData.username.trim()) {
      toast.error('Username is required');
      return false;
    }
    if (formData.username.trim().length < 3) {
      toast.error('Username must be at least 3 characters');
      return false;
    }
    if (!formData.email.trim()) {
      toast.error('Email is required');
      return false;
    }
    if (!/\S+@\S+\.\S+/.test(formData.email)) {
      toast.error('Please enter a valid email address');
      return false;
    }
    if (!formData.password) {
      toast.error('Password is required');
      return false;
    }
    if (formData.password.length < 8) {
      toast.error('Password must be at least 8 characters');
      return false;
    }
    if (formData.password !== formData.confirmPassword) {
      toast.error('Passwords do not match');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setLoading(true);
    try {
      const response = await axios.post('http://localhost:8080/api/auth/register', {
        username: formData.username,
        email: formData.email,
        password: formData.password,
        fullName: formData.username
      });

      if (response.data.success) {
        toast.success('Registration successful! Please login.');
        navigate('/login');
      } else {
        toast.error(response.data.message || 'Registration failed');
      }
    } catch (error) {
      if (error.response?.status === 409) toast.error('Username or email already exists');
      else if (error.response?.status === 400) toast.error('Invalid registration data');
      else if (error.request) toast.error('Network error. Please check your connection.');
      else toast.error(error.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card fade-in">
        <div className="auth-brand" aria-hidden="true">
          <h1>ResolveIT</h1>
          <p>Create an account to start submitting and tracking complaints.</p>
          <ul>
            <li>Secure account access</li>
            <li>Track complaint status</li>
            <li>Transparent admin updates</li>
          </ul>
        </div>

        <div className="auth-form">
          <h2>Create account</h2>
          <p className="muted">A few details and you’re ready to go.</p>

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
                placeholder="Choose a username"
                disabled={loading}
              />
            </div>

            <div className="field">
              <label htmlFor="email">Email</label>
              <input
                id="email"
                className="form-control"
                name="email"
                value={formData.email}
                onChange={handleChange}
                autoComplete="email"
                placeholder="you@company.com"
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
                  autoComplete="new-password"
                  placeholder="Create a strong password"
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

              <div className="strength" aria-label="Password strength">
                <div className="strength-bar" aria-hidden="true">
                  <div style={{ width: `${strengthPct}%`, background: strengthMeta.color }} />
                </div>
                <span style={{ color: 'var(--muted)', fontSize: 12, fontWeight: 800 }}>
                  {formData.password ? `Strength: ${strengthMeta.label}` : 'Strength: —'}
                </span>
              </div>
            </div>

            <div className="field">
              <label htmlFor="confirmPassword">Confirm password</label>
              <div className="input-row">
                <input
                  id="confirmPassword"
                  className="form-control"
                  name="confirmPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  autoComplete="new-password"
                  placeholder="Re-enter password"
                  disabled={loading}
                />
                <button
                  type="button"
                  className="input-action"
                  onClick={() => setShowConfirmPassword((v) => !v)}
                  disabled={loading}
                >
                  {showConfirmPassword ? 'Hide' : 'Show'}
                </button>
              </div>
            </div>

            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? (
                  <>
                    <span className="spinner-border" style={{ width: 18, height: 18, borderWidth: 3 }} />
                    Creating…
                  </>
                ) : (
                  'Create account'
                )}
              </button>
              <button type="button" className="btn btn-outline" onClick={() => navigate('/login')} disabled={loading}>
                Sign in
              </button>
            </div>

            <div className="form-footer">
              <span>
                Already have an account? <Link className="link" to="/login">Sign in</Link>
              </span>
              <Link className="link" to="/">Back home</Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default RegisterPage;
