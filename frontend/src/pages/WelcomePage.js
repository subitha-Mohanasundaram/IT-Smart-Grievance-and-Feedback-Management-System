import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

function WelcomePage() {
  const navigate = useNavigate();
  const [time, setTime] = useState('');

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setTime(now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }));
    };
    updateTime();
    const timer = setInterval(updateTime, 60_000);
    return () => clearInterval(timer);
  }, []);

  return (
    <div className="page-hero">
      <div className="hero-panel fade-in" role="region" aria-label="Welcome">
        <div className="welcome-container">
          <h1 className="welcome-title">Smart grievance & feedback, done right.</h1>
          <p className="welcome-subtitle">
            ResolveIT helps teams log issues, collaborate with context, and track progress from submission to resolution —
            with a clean workflow for users and admins.
          </p>

          <div className="button-group">
            <button type="button" className="btn btn-primary btn-large" onClick={() => navigate('/login')}>
              Sign in
            </button>
            <button type="button" className="btn btn-secondary btn-large" onClick={() => navigate('/register')}>
              Create account
            </button>
          </div>

          <div className="welcome-meta" aria-label="Highlights">
            <span className="pill">Status tracking</span>
            <span className="pill">Escalation-ready</span>
            <span className="pill">Time: {time}</span>
          </div>
        </div>

        <div className="feature-grid">
          <div className="feature-card">
            <h3 className="feature-title">Faster filing</h3>
            <p className="feature-text">Clear fields, sensible defaults, and optional attachments for full context.</p>
          </div>
          <div className="feature-card">
            <h3 className="feature-title">Transparent workflow</h3>
            <p className="feature-text">Consistent statuses with a clean admin view for assignment and updates.</p>
          </div>
          <div className="feature-card">
            <h3 className="feature-title">Clean, modern UI</h3>
            <p className="feature-text">Cohesive typography, spacing, and components that feel professional.</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default WelcomePage;
