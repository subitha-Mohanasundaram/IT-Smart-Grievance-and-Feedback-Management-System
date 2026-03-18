import React from 'react';
import { Navigate } from 'react-router-dom';

function PrivateRoute({ children, requiredRole }) {
  const token = localStorage.getItem('token');
  const userRole = localStorage.getItem('role');
  
  if (!token) {
    return <Navigate to="/login" />;
  }
  
  // Check role if required
  if (requiredRole && userRole !== requiredRole) {
    if (userRole === 'ADMIN') {
      return <Navigate to="/admin" />;
    } else {
      return <Navigate to="/my-complaints" />;
    }
  }
  
  return children;
}

export default PrivateRoute;