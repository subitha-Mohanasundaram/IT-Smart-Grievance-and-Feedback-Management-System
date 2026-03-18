import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './AdminComplaintDetails.css';

const AdminComplaintDetails = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [complaint, setComplaint] = useState(null);
    const [comments, setComments] = useState([]);
    const [newComment, setNewComment] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showCommentForm, setShowCommentForm] = useState(false);
    const [commentType, setCommentType] = useState('PUBLIC');
    const [submitting, setSubmitting] = useState(false);

    const fetchComplaintDetails = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            
            const token = localStorage.getItem('token');
            console.log(`📋 Fetching complaint details for ID: ${id}`);
            
            // Try the /view endpoint first
            try {
                const response = await axios.get(`http://localhost:8080/api/complaints/${id}/view`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                
                console.log('✅ Response from /view:', response.data);
                
                if (response.data.success) {
                    setComplaint(response.data.complaint);
                    setComments(response.data.comments || []);
                    console.log(`📝 Loaded ${response.data.comments?.length || 0} comments`);
                    setLoading(false);
                    return;
                } else {
                    console.error('❌ /view returned success: false', response.data);
                }
            } catch (viewError) {
                console.log('⚠️ /view endpoint failed, trying /details endpoint:', viewError.message);
                console.log('Error details:', viewError.response?.data);
                
                // Fallback to /details endpoint
                const detailsResponse = await axios.get(`http://localhost:8080/api/complaints/${id}/details`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                
                console.log('✅ Response from /details:', detailsResponse.data);
                
                if (detailsResponse.data.success) {
                    setComplaint(detailsResponse.data.data);
                    
                    // Fetch comments separately
                    try {
                        const commentsResponse = await axios.get(`http://localhost:8080/api/complaints/${id}/comments`, {
                            headers: {
                                'Authorization': `Bearer ${token}`
                            },
                            params: {
                                adminView: true
                            }
                        });
                        
                        console.log('✅ Comments response:', commentsResponse.data);
                        
                        if (commentsResponse.data.success) {
                            setComments(commentsResponse.data.data || []);
                            console.log(`📝 Loaded ${commentsResponse.data.data?.length || 0} comments from separate endpoint`);
                        }
                    } catch (commentsError) {
                        console.log('❌ Error fetching comments:', commentsError.message);
                        console.log('Comments error details:', commentsError.response?.data);
                        setComments([]);
                    }
                    
                    setLoading(false);
                    return;
                }
            }
            
            throw new Error('Failed to fetch complaint details from both endpoints');
            
        } catch (error) {
            console.error('❌ Error fetching complaint details:', error);
            console.error('Error response:', error.response?.data);
            setError(error.message || 'Unknown error occurred');
            setLoading(false);
        }
    }, [id]);

    useEffect(() => {
        if (id) {
            fetchComplaintDetails();
        }
    }, [id, fetchComplaintDetails]);

    const handleAddComment = async () => {
        if (!newComment.trim()) {
            alert('Please enter a comment');
            return;
        }

        if (submitting) {
            return;
        }

        try {
            setSubmitting(true);
            const token = localStorage.getItem('token');
            
            // Create FormData
            const formData = new FormData();
            formData.append('content', newComment.trim());
            formData.append('complaintId', id);
            formData.append('type', commentType);
            formData.append('isAdminOnly', commentType === 'INTERNAL' ? 'true' : 'false'); // Send as string
            
            console.log('📤 Submitting comment with data:');
            console.log('- Content:', newComment);
            console.log('- Complaint ID:', id);
            console.log('- Type:', commentType);
            console.log('- Is Admin Only:', commentType === 'INTERNAL');

            // Log FormData contents
            for (let pair of formData.entries()) {
                console.log(`  ${pair[0]}: ${pair[1]}`);
            }

            const response = await axios.post('http://localhost:8080/api/comments', formData, {
                headers: {
                    'Authorization': `Bearer ${token}`
                    // DO NOT set Content-Type header - let axios set it automatically for FormData
                }
            });

            console.log('✅ Comment response:', response.data);

            if (response.data.success) {
                alert('Comment added successfully!');
                setNewComment('');
                setShowCommentForm(false);
                // Refresh comments
                await fetchComplaintDetails();
            } else {
                alert('Failed to add comment: ' + response.data.message);
            }
        } catch (error) {
            console.error('❌ Error adding comment:', error);
            console.error('Error response data:', error.response?.data);
            console.error('Error status:', error.response?.status);
            console.error('Error headers:', error.response?.headers);
            
            let errorMessage = 'Error adding comment: ';
            if (error.response?.data?.message) {
                errorMessage += error.response.data.message;
            } else if (error.response?.data) {
                errorMessage += JSON.stringify(error.response.data);
            } else {
                errorMessage += error.message;
            }
            
            alert(errorMessage);
        } finally {
            setSubmitting(false);
        }
    };

    const handleUpdateStatus = async (newStatus) => {
        if (!window.confirm(`Change status to ${newStatus}?`)) {
            return;
        }

        try {
            const token = localStorage.getItem('token');
            const response = await axios.put(
                `http://localhost:8080/api/complaints/${id}/status`,
                { status: newStatus },
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (response.data.success) {
                alert('Status updated successfully!');
                fetchComplaintDetails(); // Refresh complaint details
            } else {
                alert('Failed to update status: ' + response.data.message);
            }
        } catch (error) {
            console.error('Error updating status:', error);
            alert('Error updating status: ' + error.message);
        }
    };

    const handleDownloadFile = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`http://localhost:8080/api/complaints/${id}/download`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                responseType: 'blob'
            });

            // Get filename from response headers or use default
            const filename = complaint?.fileName || `complaint_${id}_attachment`;
            
            // Create download link
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', filename);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            console.error('Error downloading file:', error);
            alert('Error downloading file: ' + error.message);
        }
    };

    if (loading) {
        return (
            <div className="admin-complaint-details">
                <div className="loading-container">
                    <h2>Loading complaint details...</h2>
                    <div className="loading-spinner"></div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="admin-complaint-details">
                <div className="error-container">
                    <h2>Error Loading Complaint</h2>
                    <p>{error}</p>
                    <button onClick={() => navigate('/admin/complaints')} className="back-button">
                        Back to Complaints
                    </button>
                </div>
            </div>
        );
    }

    if (!complaint) {
        return (
            <div className="admin-complaint-details">
                <div className="not-found">
                    <h2>Complaint not found</h2>
                    <button onClick={() => navigate('/admin/complaints')} className="back-button">
                        Back to Complaints
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="admin-complaint-details">
            <div className="header">
                <h1>Complaint Details</h1>
                <button onClick={() => navigate('/admin/complaints')} className="back-button">
                    Back to Complaints
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
                    <p><strong>Created By:</strong> {complaint.userName || complaint.userId || 'Unknown'}</p>
                    <p><strong>Description:</strong></p>
                    <div className="description-box">{complaint.description || 'No description provided'}</div>
                </div>

                {complaint.fileName && (
                    <div className="file-section">
                        <p><strong>Attachment:</strong> {complaint.fileName}</p>
                        <button onClick={handleDownloadFile} className="download-button">
                            Download File
                        </button>
                    </div>
                )}

                <div className="actions-section">
                    <h3>Update Status</h3>
                    <div className="status-buttons">
                        {['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'].map(status => (
                            <button
                                key={status}
                                onClick={() => handleUpdateStatus(status)}
                                className={`status-button ${complaint.status === status ? 'active' : ''}`}
                                disabled={submitting}
                            >
                                {status}
                            </button>
                        ))}
                    </div>
                </div>
            </div>

            {/* Comments Section */}
            <div className="comments-section">
                <div className="comments-header">
                    <h3>Comments ({comments.length})</h3>
                    <button 
                        onClick={() => setShowCommentForm(!showCommentForm)} 
                        className="add-comment-btn"
                        disabled={submitting}
                    >
                        {showCommentForm ? 'Cancel' : 'Add Comment'}
                    </button>
                </div>

                {showCommentForm && (
                    <div className="comment-form">
                        <textarea
                            value={newComment}
                            onChange={(e) => setNewComment(e.target.value)}
                            placeholder="Enter your comment..."
                            rows="4"
                            disabled={submitting}
                        />
                        <div className="comment-type-selector">
                            <label>
                                <input
                                    type="radio"
                                    value="PUBLIC"
                                    checked={commentType === 'PUBLIC'}
                                    onChange={(e) => setCommentType(e.target.value)}
                                    disabled={submitting}
                                />
                                Public
                            </label>
                            <label>
                                <input
                                    type="radio"
                                    value="INTERNAL"
                                    checked={commentType === 'INTERNAL'}
                                    onChange={(e) => setCommentType(e.target.value)}
                                    disabled={submitting}
                                />
                                Internal (Admin Only)
                            </label>
                        </div>
                        <button 
                            onClick={handleAddComment} 
                            className="submit-comment-btn"
                            disabled={submitting || !newComment.trim()}
                        >
                            {submitting ? 'Submitting...' : 'Submit Comment'}
                        </button>
                    </div>
                )}

                {comments.length === 0 ? (
                    <p className="no-comments">No comments yet. Be the first to add a comment!</p>
                ) : (
                    <div className="comments-list">
                        {comments.map((comment, index) => (
                            <div key={comment.id || index} className={`comment-item ${comment.isAdminOnly ? 'admin-only' : ''}`}>
                                <div className="comment-header">
                                    <span className="comment-author">
                                        {comment.authorName || comment.userName || 'Admin'}
                                    </span>
                                    <span className="comment-time">
                                        {comment.formattedCreatedAt || 
                                         (comment.createdAt ? new Date(comment.createdAt).toLocaleString() : 'Unknown time')}
                                    </span>
                                    <span className={`comment-type ${comment.type?.toLowerCase()}`}>
                                        {comment.type || 'PUBLIC'}
                                    </span>
                                    {comment.isAdminOnly && (
                                        <span className="admin-only-badge">Admin Only</span>
                                    )}
                                </div>
                                <div className="comment-content">{comment.content}</div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default AdminComplaintDetails;