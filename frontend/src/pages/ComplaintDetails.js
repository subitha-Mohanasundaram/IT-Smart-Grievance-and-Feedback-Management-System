import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './ComplaintDetails.css';

const ComplaintDetails = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [complaint, setComplaint] = useState(null);
    const [comments, setComments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchComplaintDetails = async () => {
            try {
                setLoading(true);
                setError(null);
                
                const token = localStorage.getItem('token');
                
                if (!token) {
                    navigate('/login');
                    return;
                }

                console.log(`ðŸ‘¤ User fetching complaint ${id} details...`);

                // Try to get user's complaint (only their own complaints)
                const response = await axios.get(`http://localhost:8080/api/complaints/my-complaints`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });

                if (response.data.success) {
                    // Find the specific complaint from user's list
                    const userComplaints = response.data.data || [];
                    const foundComplaint = userComplaints.find((c) => String(c.id) === String(id));
                    
                    if (foundComplaint) {
                        setComplaint(foundComplaint);
                        
                        // Fetch comments for this complaint (USER VIEW - adminView=false)
                        try {
                            const commentsResponse = await axios.get(`http://localhost:8080/api/complaints/${id}/comments`, {
                                headers: { 'Authorization': `Bearer ${token}` },
                                params: { adminView: false } // IMPORTANT: User only sees PUBLIC comments
                            });

                            console.log('ðŸ“ User comments response:', commentsResponse.data);

                            if (commentsResponse.data.success) {
                                setComments(commentsResponse.data.data || []);
                                console.log(`âœ… User loaded ${commentsResponse.data.data?.length || 0} PUBLIC comments`);
                            }
                        } catch (commentsError) {
                            console.log('âš ï¸ Could not fetch comments:', commentsError.message);
                            setComments([]);
                        }
                    } else {
                        setError('Complaint not found or access denied');
                    }
                } else {
                    setError('Failed to load complaints');
                }
                
                setLoading(false);
            } catch (error) {
                console.error('âŒ Error in user complaint view:', error);
                console.error('Error response:', error.response?.data);
                
                if (error.response?.status === 401) {
                    navigate('/login');
                    return;
                }
                
                setError(error.response?.data?.message || error.message || 'Failed to load complaint');
                setLoading(false);
            }
        };

        if (id) {
            fetchComplaintDetails();
        }
    }, [id, navigate]);

    const handleGoBack = () => {
        navigate('/my-complaints'); // Or wherever user's complaint list is
    };

    if (loading) {
        return (
            <div className="complaint-details">
                <div className="loading-container">
                    <h2>Loading complaint details...</h2>
                    <div className="loading-spinner"></div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="complaint-details">
                <div className="error-container">
                    <h2>Error</h2>
                    <p>{error}</p>
                    <button onClick={handleGoBack} className="back-button">
                        Back to My Complaints
                    </button>
                </div>
            </div>
        );
    }

    if (!complaint) {
        return (
            <div className="complaint-details">
                <div className="not-found">
                    <h2>Complaint not found</h2>
                    <p>You may not have permission to view this complaint or it doesn't exist.</p>
                    <button onClick={handleGoBack} className="back-button">
                        Back to My Complaints
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="complaint-details">
            <div className="header">
                <h1>Complaint Details</h1>
                <button onClick={handleGoBack} className="back-button">
                    Back to My Complaints
                </button>
            </div>

            <div className="complaint-info">
                <div className="complaint-header">
                    <h2>#{complaint.id} - {complaint.title}</h2>
                    <span className={`status-badge ${complaint.status?.toLowerCase()}`}>
                        {complaint.status || 'Unknown'}
                    </span>
                </div>

                <div className="complaint-meta">
                    <p><strong>Category:</strong> {complaint.category || 'N/A'}</p>
                    <p><strong>Priority:</strong> <span className={`priority-${complaint.priority?.toLowerCase()}`}>{complaint.priority || 'N/A'}</span></p>
                    <p><strong>Created:</strong> {complaint.createdAt ? new Date(complaint.createdAt).toLocaleString() : 'N/A'}</p>
                    <p><strong>Last Updated:</strong> {complaint.updatedAt ? new Date(complaint.updatedAt).toLocaleString() : 'N/A'}</p>
                    <p><strong>Description:</strong></p>
                    <div className="description-box">{complaint.description || 'No description provided'}</div>
                </div>

                {complaint.fileName && (
                    <div className="file-section">
                        <p><strong>Attachment:</strong> {complaint.fileName}</p>
                        <button 
                            onClick={() => window.open(`http://localhost:8080/api/complaints/${id}/download`, '_blank')}
                            className="download-button"
                        >
                            Download File
                        </button>
                    </div>
                )}
            </div>

            {/* Comments Section - USER VIEW (Only PUBLIC comments) */}
            <div className="comments-section">
                <div className="comments-header">
                    <h3>Comments ({comments.length})</h3>
                    <p className="comments-note">
                        <small>Note: You can only see public comments. Internal comments are visible only to admins.</small>
                    </p>
                </div>

                {comments.length === 0 ? (
                    <div className="no-comments">
                        <p>No comments yet.</p>
                        <p className="info-text">When admins add public comments, they will appear here.</p>
                    </div>
                ) : (
                    <div className="comments-list">
                        {comments.map((comment) => (
                            <div key={comment.id} className="comment-item public-comment">
                                <div className="comment-header">
                                    <div className="comment-author-info">
                                        <span className="comment-author">
                                            {comment.authorName || comment.authorRole || 'Admin'}
                                        </span>
                                        <span className="comment-role">
                                            {comment.authorRole === 'ADMIN' ? '(Admin)' : '(User)'}
                                        </span>
                                    </div>
                                    <div className="comment-meta">
                                        <span className="comment-time">
                                            {comment.formattedCreatedAt || 
                                             (comment.createdAt ? new Date(comment.createdAt).toLocaleString() : 'Unknown time')}
                                        </span>
                                        <span className="comment-type-badge public">
                                            Public Comment
                                        </span>
                                    </div>
                                </div>
                                <div className="comment-content">
                                    {comment.content}
                                </div>
                                {comment.attachmentPath && (
                                    <div className="comment-attachment">
                                        <small>
                                            <a 
                                                href={`http://localhost:8080${comment.attachmentPath}`} 
                                                target="_blank" 
                                                rel="noopener noreferrer"
                                            >View Attachment</a>
                                        </small>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default ComplaintDetails;

