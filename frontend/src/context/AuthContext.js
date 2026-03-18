// frontend/src/context/AuthContext.js
import React, { createContext, useState, useContext, useEffect } from 'react';
import axios from 'axios';

const AuthContext = createContext();

export const useAuth = () => {
    return useContext(AuthContext);
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Check token validity on app start
    useEffect(() => {
        const token = localStorage.getItem('token');
        const username = localStorage.getItem('username');
        const role = localStorage.getItem('role');
        const email = localStorage.getItem('email');
        const userId = localStorage.getItem('userId');

        if (token && username) {
            // Verify token is still valid
            if (isTokenValid(token)) {
                setUser({
                    token,
                    username,
                    role,
                    email,
                    userId,
                    isAuthenticated: true,
                    isAdmin: role === 'ADMIN'
                });
                
                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            } else {
                // Token expired, clear storage
                clearStorage();
            }
        }
        setLoading(false);
    }, []);

    // Check if JWT token is valid (not expired)
    const isTokenValid = (token) => {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const expiryTime = payload.exp * 1000; // Convert to milliseconds
            return Date.now() < expiryTime;
        } catch (error) {
            return false;
        }
    };

    // Clear all auth data
    const clearStorage = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
        localStorage.removeItem('email');
        localStorage.removeItem('role');
        delete axios.defaults.headers.common['Authorization'];
        setUser(null);
    };

    // Login function
    const login = (userData) => {
        const { token, userId, username, email, role } = userData;
        
        // Store in localStorage
        localStorage.setItem('token', token);
        localStorage.setItem('userId', userId);
        localStorage.setItem('username', username);
        localStorage.setItem('email', email);
        localStorage.setItem('role', role);
        
        // Update state
        const userObj = {
            token,
            userId,
            username,
            email,
            role,
            isAuthenticated: true,
            isAdmin: role === 'ADMIN'
        };
        
        setUser(userObj);
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        
        return userObj;
    };

    // Logout function
    const logout = () => {
        clearStorage();
        window.location.href = '/login'; // Force refresh
    };

    // Check authentication
    const isAuthenticated = () => {
        if (!user) return false;
        
        // Verify token hasn't expired
        const token = localStorage.getItem('token');
        if (token && !isTokenValid(token)) {
            logout();
            return false;
        }
        
        return user.isAuthenticated === true;
    };

    const isAdmin = () => {
        return user?.isAdmin === true;
    };

    const getCurrentUser = () => {
        return user;
    };

    const value = {
        user,
        loading,
        login,
        logout,
        isAuthenticated,
        isAdmin,
        getCurrentUser
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};