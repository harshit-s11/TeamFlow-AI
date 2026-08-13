import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar">
      <Link to="/" className="brand">
        TeamFlow <span>AI</span>
      </Link>

      <nav className="nav-links">
        <Link to="/health" className="nav-link">
          System Health
        </Link>
        {isAuthenticated ? (
          <>
            <Link to="/" className="nav-link">
              Dashboard
            </Link>
            <Link to="/teams" className="nav-link">
              Teams
            </Link>
            <Link to="/projects" className="nav-link">
              Projects
            </Link>

            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
              {user?.name} ({user?.role})
            </span>

            <button onClick={handleLogout} className="btn btn-secondary" style={{ padding: '0.4rem 0.85rem', fontSize: '0.85rem' }}>
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="nav-link">
              Sign In
            </Link>
            <Link to="/register" className="btn btn-primary" style={{ padding: '0.4rem 0.85rem', fontSize: '0.85rem' }}>
              Get Started
            </Link>
          </>
        )}
      </nav>
    </header>
  );
};
