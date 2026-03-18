import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../../context/AuthContext';

const AdminEscalationDashboard = () => {
    const { token } = useAuth();
    const [stats, setStats] = useState({});
    const [escalatedComplaints, setEscalatedComplaints] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchEscalationData();
    }, []);

    const fetchEscalationData = async () => {
        try {
            setLoading(true);
            
            // Fetch escalation stats
            const statsResponse = await axios.get('http://localhost:8080/api/admin/escalation/stats', {
                headers: { Authorization: `Bearer ${token}` }
            });
            
            // Fetch escalated complaints
            const complaintsResponse = await axios.get('http://localhost:8080/api/admin/escalation/complaints', {
                headers: { Authorization: `Bearer ${token}` }
            });
            
            if (statsResponse.data.success) {
                setStats(statsResponse.data.stats);
            }
            
            if (complaintsResponse.data.success) {
                setEscalatedComplaints(complaintsResponse.data.data);
            }
        } catch (err) {
            console.error('Error fetching escalation data:', err);
        } finally {
            setLoading(false);
        }
    };

    const manuallyEscalate = async (complaintId, targetLevel) => {
        if (!window.confirm(`Manually escalate complaint #${complaintId} to Level ${targetLevel}?`)) return;
        
        try {
            await axios.post(`http://localhost:8080/api/admin/complaints/${complaintId}/escalate`, {
                targetLevel: targetLevel,
                reason: "Manual escalation by admin"
            }, {
                headers: { Authorization: `Bearer ${token}` }
            });
            
            alert('Complaint escalated successfully!');
            fetchEscalationData();
        } catch (err) {
            alert('Failed to escalate complaint');
            console.error(err);
        }
    };

    if (loading) return <div className="text-center"><div className="spinner-border"></div></div>;

    return (
        <div className="container mt-4">
            <h2 className="mb-4">Escalation Dashboard</h2>
            
            {/* Stats Cards */}
            <div className="row mb-4">
                <div className="col-md-3">
                    <div className="card text-white bg-primary">
                        <div className="card-body">
                            <h5 className="card-title">Total Escalated</h5>
                            <h2>{stats.totalEscalated || 0}</h2>
                            <p className="card-text">{stats.escalationRate || '0%'} of all complaints</p>
                        </div>
                    </div>
                </div>
                
                <div className="col-md-3">
                    <div className="card text-white bg-danger">
                        <div className="card-body">
                            <h5 className="card-title">SUPER ADMIN Level</h5>
                            <h2>{stats.levelCounts && stats.levelCounts[5] ? stats.levelCounts[5] : 0}</h2>
                            <p className="card-text">Highest priority complaints</p>
                        </div>
                    </div>
                </div>
                
                <div className="col-md-3">
                    <div className="card text-white bg-warning">
                        <div className="card-body">
                            <h5 className="card-title">High Priority</h5>
                            <h2>{stats.totalHighPriority || 0}</h2>
                            <p className="card-text">Level 3+ escalations</p>
                        </div>
                    </div>
                </div>
                
                <div className="col-md-3">
                    <div className="card text-white bg-info">
                        <div className="card-body">
                            <h5 className="card-title">Next Escalation</h5>
                            <button 
                                className="btn btn-light"
                                onClick={() => window.location.href = '/admin/escalation/pending'}
                            >
                                View Pending
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            
            {/* Escalated Complaints Table */}
            <div className="card">
                <div className="card-header d-flex justify-content-between align-items-center">
                    <h5 className="mb-0">Currently Escalated Complaints</h5>
                    <button className="btn btn-sm btn-primary" onClick={fetchEscalationData}>
                        Refresh
                    </button>
                </div>
                <div className="card-body">
                    {escalatedComplaints.length === 0 ? (
                        <div className="text-center p-4">
                            <p className="text-muted">No escalated complaints at the moment</p>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <table className="table table-hover">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Title</th>
                                        <th>User</th>
                                        <th>Escalation Level</th>
                                        <th>Assigned To</th>
                                        <th>Escalated At</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {escalatedComplaints.map(complaint => (
                                        <tr key={complaint.id} className={
                                            complaint.escalationLevel >= 5 ? 'table-danger' :
                                            complaint.escalationLevel >= 3 ? 'table-warning' : ''
                                        }>
                                            <td>#{complaint.id}</td>
                                            <td>{complaint.title}</td>
                                            <td>{complaint.createdBy}</td>
                                            <td>
                                                <span className={`badge ${
                                                    complaint.escalationLevel >= 5 ? 'bg-danger' :
                                                    complaint.escalationLevel >= 3 ? 'bg-warning' : 'bg-info'
                                                }`}>
                                                    Level {complaint.escalationLevel}
                                                </span>
                                            </td>
                                            <td>{complaint.assignedTo || 'Not assigned'}</td>
                                            <td>{complaint.escalatedAt ? 
                                                new Date(complaint.escalatedAt).toLocaleString() : 'N/A'}</td>
                                            <td>
                                                <button 
                                                    className="btn btn-sm btn-outline-primary me-2"
                                                    onClick={() => window.location.href = `/admin/complaint/${complaint.id}`}
                                                >
                                                    View
                                                </button>
                                                {complaint.escalationLevel < 5 && (
                                                    <button 
                                                        className="btn btn-sm btn-outline-danger"
                                                        onClick={() => manuallyEscalate(complaint.id, complaint.escalationLevel + 1)}
                                                    >
                                                        Escalate
                                                    </button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>
            
            {/* Quick Actions */}
            <div className="mt-4">
                <div className="card">
                    <div className="card-header">
                        <h5 className="mb-0">Quick Actions</h5>
                    </div>
                    <div className="card-body">
                        <div className="row">
                            <div className="col-md-4">
                                <button 
                                    className="btn btn-primary w-100 mb-2"
                                    onClick={() =>Configure Escalation</button>
                            </div>
                            <div className="col-md-4">
                                <button 
                                    className="btn btn-warning w-100 mb-2"
                                    onClick={() =>High Priority Complaints</button>
                            </div>
                            <div className="col-md-4">
                                <button 
                                    className="btn btn-success w-100 mb-2"
                                    onClick={() =>Run Escalation Now</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminEscalationDashboard;

