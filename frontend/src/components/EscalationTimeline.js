import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const EscalationTimeline = () => {
    const { id } = useParams();
    const { token } = useAuth();
    const [timeline, setTimeline] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [message, setMessage] = useState('');

    useEffect(() => {
        fetchTimeline();
    }, [id]);

    const fetchTimeline = async () => {
        try {
            setLoading(true);
            const response = await axios.get(`http://localhost:8080/api/complaints/${id}/escalation-timeline`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            
            if (response.data.success) {
                setTimeline(response.data.timeline);
                setMessage(response.data.message);
            }
        } catch (err) {
            setError('Failed to fetch escalation timeline');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="text-center mt-5"><div className="spinner-border"></div></div>;
    if (error) return <div className="alert alert-danger mt-5">{error}</div>;

    return (
        <div className="container mt-4">
            <h2 className="mb-4">Complaint #{id} - Escalation Timeline</h2>
            
            {message && (
                <div className={`alert ${message.includes('SUPER ADMIN') ? 'alert-danger' : 
                                message.includes('escalated') ? 'alert-warning' : 'alert-info'}`}>
                    {message}
                </div>
            )}
            
            <div className="timeline">
                {timeline.map((item, index) => (
                    <div className={`timeline-item ${item.status}`} key={index}>
                        <div className="timeline-marker">
                            <span style={{ fontSize: '1.5rem' }}>{item.icon}</span>
                        </div>
                        <div className="timeline-content">
                            <h5>{item.title}</h5>
                            <p>{item.description}</p>
                            {item.timeLimitHours && (
                                <p><small>Time Limit: {item.timeLimitHours} hours</small></p>
                            )}
                            {item.reachedAt && (
                                <p><small>Reached: {new Date(item.reachedAt).toLocaleString()}</small></p>
                            )}
                            <div className={`badge bg-${item.status === 'completed' ? 'success' : 
                                            item.status === 'current' ? 'warning' : 'secondary'}`}>
                                {item.status.toUpperCase()}
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            <style jsx>{`
                .timeline {
                    position: relative;
                    padding-left: 30px;
                    margin: 20px 0;
                }
                .timeline-item {
                    position: relative;
                    margin-bottom: 30px;
                }
                .timeline-marker {
                    position: absolute;
                    left: -40px;
                    top: 0;
                    width: 30px;
                    height: 30px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                .timeline-content {
                    background: #f8f9fa;
                    padding: 15px;
                    border-radius: 8px;
                    border-left: 4px solid #ddd;
                }
                .timeline-item.completed .timeline-content {
                    border-left-color: #28a745;
                    background: #d4edda;
                }
                .timeline-item.current .timeline-content {
                    border-left-color: #ffc107;
                    background: #fff3cd;
                }
                .timeline-item.pending .timeline-content {
                    border-left-color: #6c757d;
                    background: #e9ecef;
                }
            `}</style>
            
            <div className="mt-4">
                <button 
                    className="btn btn-primary"
                    onClick={() =>← Back to Complaint</button>
            </div>
        </div>
    );
};

export default EscalationTimeline;

