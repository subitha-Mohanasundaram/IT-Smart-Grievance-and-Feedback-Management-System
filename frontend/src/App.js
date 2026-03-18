import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './App.css';

// Auth Context
import { AuthProvider, useAuth } from './context/AuthContext';

// Components
import Navbar from './components/Navbar';

// Pages
import WelcomePage from './pages/WelcomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AllComplaintsPage from './pages/AllComplaintsPage';
import NewComplaintPage from './pages/NewComplaintPage';
import MyComplaintsPage from './pages/MyComplaintsPage';
import AdminDashboard from './pages/AdminDashboard';
import AdminComplaintDetails from './pages/AdminComplaintDetails';
import ComplaintDetails from './pages/ComplaintDetails'; // ADD THIS IMPORT

// Private Route Component
const PrivateRoute = ({ children, adminOnly = false, userOnly = false }) => {
  const { isAuthenticated, isAdmin, loading } = useAuth();
  
  if (loading) {
    return (
      <div className="container mt-5 text-center">
        <div className="spinner-border" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }
  
  if (!isAuthenticated()) {
    return <Navigate to="/login" />;
  }
  
  if (adminOnly && !isAdmin()) {
    return <Navigate to="/my-complaints" />;
  }
  
  if (userOnly && isAdmin()) {
    return <Navigate to="/admin" />;
  }
  
  return children;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Navbar />
          <div className="main-content">
            <Routes>
              <Route path="/" element={<WelcomePage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              
              {/* Protected User Routes */}
              <Route path="/new-complaint" element={
                <PrivateRoute>
                  <NewComplaintPage />
                </PrivateRoute>
              } />
              
              <Route path="/my-complaints" element={
                <PrivateRoute>
                  <MyComplaintsPage />
                </PrivateRoute>
              } />
              
              {/* User Complaint Details Route - ADD THIS */}
              <Route path="/complaints/:id" element={
                <PrivateRoute>
                  <ComplaintDetails />
                </PrivateRoute>
              } />
              
              {/* Protected Admin Routes */}
              <Route path="/admin" element={
                <PrivateRoute adminOnly>
                  <AdminDashboard />
                </PrivateRoute>
              } />
              
              <Route path="/admin/complaints/:id" element={
                <PrivateRoute adminOnly>
                  <AdminComplaintDetails />
                </PrivateRoute>
              } />
              
              <Route path="/all-complaints" element={
                <PrivateRoute adminOnly>
                  <AllComplaintsPage />
                </PrivateRoute>
              } />
              
              {/* Redirect unknown routes */}
              <Route path="*" element={<Navigate to="/" />} />
            </Routes>
          </div>
          <ToastContainer position="top-right" autoClose={3000} />
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;