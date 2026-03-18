import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';

const UserEscalatedComplaints = () => {
    const { token } = useAuth();
    const [escalatedComplaints, setEscalatedComplaints] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchEscalatedComplaints();
    }, []);

    const fetchEscalatedComplaints = async () => {
        try {
            setLoading(true);
            const response = await axios.get('http://localhost:8080/api/complaints/my/escalated', {
                headers: { Authorization: `Bearer ${token}` }
            });
            
            if (response.data.success) {
                setEscalatedComplaints(response.data.data);
            }
        } catch (err) {
            setError('Failed to fetch escalated complaints');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const getEscalationBadge = (level) => {
        if (level >= 5) {
            return <span className="badge bg-danger">SUPER ADMIN</span>;
        } else if (level >= 3) {
            return <span className="badge bg-warning">HIGH</span>;
        } else if (level >= 1) {
            return <span className="badge bg-info">MEDIUM</span>;
        }
        return null;
    };

    const getEscalationMessage = (level) => {
        if (level >= 5) {
            return "Your complaint has reached the highest authority (SUPER ADMIN). You should receive an email confirmation.";
        } else if (level >= 3) {
            return "Your complaint has been escalated to management for immediate attention.";
        } else if (level >= 1) {
            return "Your complaint has been escalated for faster resolution.";
        }
        return "";
    };

    if (loading) return <div className="text-center"><div className="spinner-border"></div></div>;
    if (error) return <div className="alert alert-danger">{error}</div>;

    return (
        <div className="container mt-4">
            <h2 className="mb-4">My Escalated Complaints</h2>
            
            {escalatedComplaints.length === 0 ? (
                <div className="alert alert-info">
                    No complaints have been escalated yet. Your complaints will automatically escalate if they take too long to resolve.
                </div>
            ) : (
                <div className="row">
                    {escalatedComplaints.map(complaint => (
                        <div className="col-md-6 mb-4" key={complaint.id}>
                            <div className={`card ${complaint.escalationLevel >= 5 ? 'border-danger' : 
                                            complaint.escalationLevel >= 3 ? 'border-warning' : 'border-info'}`}>
                                <div className="card-header d-flex justify-content-between align-items-center">
                                    <h5 className="mb-0">#{complaint.id} - {complaint.title}</h5>
                                    {getEscalationBadge(complaint.escalationLevel)}
                                </div>
                                <div className="card-body">
                                    <p><strong>Description:</strong> {complaint.description}</p>
                                    <p><strong>Category:</strong> {complaint.category}</p>
                                    <p><strong>Priority:</strong> {complaint.priority}</p>
                                    <p><strong>Status:</strong> {complaint.status}</p>
                                    
                                    <div className="mt-3 p-3 bg-light rounded">
                                        <h6>Escalation Details:</h6>
                                        <p><strong>Level:</strong> {complaint.escalationLevel}/5</p>
                                        <p><strong>Escalated At:</strong> {
                                            complaint.escalatedAt ? new Date(complaint.escalatedAt).toLocaleString() : 'Not escalated yet'
                                        }</p>
                                        <p><strong>Next Escalation:</strong> {
                                            complaint.nextEscalationTime ? 
                                            new Date(complaint.nextEscalationTime).toLocaleString() : 
                                            'No further escalation'
                                        }</p>
                                        <p className="text-success">
                                            <strong>Message:</strong> {getEscalationMessage(complaint.escalationLevel)}
                                        </p>
                                    </div>
                                    
                                    <div className="mt-3">
                                        <button 
                                            className="btn btn-sm btn-outline-primary me-2"
                                            onClick={() => window.location.href = `/complaint/${complaint.id}/timeline`}
                                        >
                                            View Escalation Timeline
                                        </button>
                                        <button 
                                            className="btn btn-sm btn-outline-secondary"
                                            onClick={() => window.location.href = `/complaint/${complaint.id}`}
                                        >
                                            View Details
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default UserEscalatedComplaints;
