import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';

function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout, isAuthenticated, isAdmin } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  const handleLogout = () => {
    logout();
    navigate('/login');
    toast.success('Logged out successfully');
  };

  return (
    <nav className="navbar">
      <div className="nav-container">
        <Link to="/" className="nav-logo">
          ResolveIT <span>Grievance</span>
        </Link>

        <button
          type="button"
          className="nav-toggle"
          aria-label="Toggle navigation menu"
          aria-expanded={menuOpen ? 'true' : 'false'}
          onClick={() => setMenuOpen((v) => !v)}
        >
          <span className="nav-toggle-bar" />
          <span className="nav-toggle-bar" />
          <span className="nav-toggle-bar" />
        </button>

        <div className={`nav-links ${menuOpen ? 'open' : ''}`}>
          <Link 
            to="/" 
            className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}
          >
            Home
          </Link>
          
          {isAuthenticated() ? (
            <>
              {isAdmin() ? (
                <>
                  <Link 
                    to="/all-complaints" 
                    className={`nav-link ${location.pathname.includes('complaints') ? 'active' : ''}`}
                  >
                    All Complaints
                  </Link>
                </>
              ) : (
                <>
                  <Link 
                    to="/my-complaints" 
                    className={`nav-link ${location.pathname === '/my-complaints' ? 'active' : ''}`}
                  >
                    My Complaints
                  </Link>
                  <Link 
                    to="/new-complaint" 
                    className={`nav-link ${location.pathname === '/new-complaint' ? 'active' : ''}`}
                  >
                    New Complaint
                  </Link>
                </>
              )}
              
              <div className="user-info">
                <span className="username">{user?.username}</span>
                <span className="user-role">({user?.role})</span>
              </div>
              
              <button 
                onClick={handleLogout} 
                className="btn btn-outline logout-btn"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link 
                to="/login" 
                className={`nav-link ${location.pathname === '/login' ? 'active' : ''}`}
              >
                Login
              </Link>
              
              <Link 
                to="/register" 
                className={`nav-link ${location.pathname === '/register' ? 'active' : ''}`}
              >
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
