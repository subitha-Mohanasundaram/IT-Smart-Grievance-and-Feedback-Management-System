import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom'; // ADD THIS IMPORT

function MyComplaintsPage() {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate(); // ADD THIS

  useEffect(() => {
    fetchMyComplaints();
  }, []);

  const fetchMyComplaints = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get('http://localhost:8080/api/complaints/my-complaints', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.data.success) {
        setComplaints(response.data.data);
      } else {
        toast.error(response.data.message);
      }
    } catch (error) {
      console.error('Error fetching complaints:', error);
      toast.error('Failed to load your complaints');
    } finally {
      setLoading(false);
    }
  };

  const handleViewFile = async (complaintId, fileName) => {
    try {
      const token = localStorage.getItem('token');
      const viewUrl = `http://localhost:8080/api/complaints/${complaintId}/view`;
      
      // Use fetch to get the file
      const response = await fetch(viewUrl, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error('Cannot view file');
      }
      
      // Get the file as blob
      const blob = await response.blob();
      
      // Create object URL
      const url = window.URL.createObjectURL(blob);
      
      // Get file type to decide how to open
      const contentType = response.headers.get('content-type');
      
      // For images, PDFs, text files - open in new tab
      if (contentType.includes('image/') || contentType.includes('pdf') || contentType.includes('text/')) {
        const newWindow = window.open('', '_blank');
        newWindow.document.write(`
          <html>
            <head><title>${fileName}</title></head>
            <body style="margin:0;padding:0;">
              ${contentType.includes('image/') ? 
                `<img src="${url}" style="max-width:100%;max-height:100vh;display:block;margin:auto;" />` : 
                contentType.includes('pdf') ?
                `<iframe src="${url}" width="100%" height="100vh" style="border:none;"></iframe>` :
                `<pre style="padding:20px;font-family:monospace;white-space:pre-wrap;">${await blob.text()}</pre>`
              }
            </body>
          </html>
        `);
        newWindow.document.close();
      } else {
        // For other files (doc, docx), download instead
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        toast.info('This file type opens better when downloaded');
      }
      
      // Clean up URL after use
      setTimeout(() => window.URL.revokeObjectURL(url), 1000);
      
    } catch (error) {
      console.error('View error:', error);
      toast.error('Cannot open file');
    }
  };

  const handleDownload = async (complaintId, fileName) => {
    try {
      const token = localStorage.getItem('token');
      const downloadUrl = `http://localhost:8080/api/complaints/${complaintId}/download`;
      
      const response = await fetch(downloadUrl, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error('Download failed');
      }
      
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName || 'file';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      
    } catch (error) {
      console.error('Download error:', error);
      toast.error('Failed to download file');
    }
  };

  // ADD THIS FUNCTION - Navigate to user complaint details
  const handleViewDetails = (complaintId) => {
    navigate(`/complaints/${complaintId}`);
  };

  const getStatusColor = (status) => {
    switch(status?.toUpperCase()) {
      case 'OPEN': return '#28a745';
      case 'IN_PROGRESS': return '#ffc107';
      case 'RESOLVED': return '#17a2b8';
      case 'CLOSED': return '#6c757d';
      case 'REJECTED': return '#dc3545';
      default: return '#6c757d';
    }
  };

  const getPriorityColor = (priority) => {
    switch(priority?.toUpperCase()) {
      case 'LOW': return '#28a745';
      case 'MEDIUM': return '#ffc107';
      case 'HIGH': return '#dc3545';
      default: return '#6c757d';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <div style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#f8f9fa'
      }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{
            width: '50px',
            height: '50px',
            border: '5px solid #f3f3f3',
            borderTop: '5px solid #3498db',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite',
            margin: '0 auto 20px'
          }}></div>
          <p style={{ fontSize: '18px', color: '#2c3e50' }}>Loading complaints...</p>
        </div>
      </div>
    );
  }

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#f5f7fa',
      padding: '30px'
    }}>
      {/* Header */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '30px',
        padding: '25px',
        backgroundColor: 'white',
        borderRadius: '12px',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
      }}>
        <div>
          <h1 style={{
            fontSize: '32px',
            fontWeight: 'bold',
            marginBottom: '8px',
            color: '#2c3e50',
            margin: 0
          }}>My Complaints</h1>
          <p style={{
            color: '#666',
            fontSize: '16px',
            margin: '5px 0'
          }}>Total: {complaints.length} complaints</p>
        </div>
        <button
          onClick={() => navigate('/new-complaint')} // UPDATED
          style={{
            padding: '12px 28px',
            backgroundColor: '#3498db',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
            fontSize: '16px',
            fontWeight: '600',
            transition: 'background-color 0.3s',
            display: 'flex',
            alignItems: 'center',
            gap: '10px'
          }}
          onMouseOver={(e) => e.target.style.backgroundColor = '#2980b9'}
          onMouseOut={(e) => e.target.style.backgroundColor = '#3498db'}
        >
          <span style={{ fontSize: '20px' }}>+</span>
          New Complaint
        </button>
      </div>

      {/* Stats */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
        gap: '20px',
        marginBottom: '30px'
      }}>
        {['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'].map((status) => (
          <div key={status} style={{
            backgroundColor: 'white',
            padding: '25px',
            borderRadius: '12px',
            boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
            textAlign: 'center',
            borderTop: `5px solid ${getStatusColor(status)}`
          }}>
            <div style={{
              fontSize: '36px',
              fontWeight: 'bold',
              color: getStatusColor(status),
              marginBottom: '10px'
            }}>
              {complaints.filter(c => c.status === status).length}
            </div>
            <div style={{
              fontSize: '16px',
              color: '#666',
              textTransform: 'uppercase',
              fontWeight: '600'
            }}>
              {status.replace('_', ' ')}
            </div>
          </div>
        ))}
      </div>

      {/* Table */}
      <div style={{
        backgroundColor: 'white',
        borderRadius: '12px',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
        overflow: 'hidden'
      }}>
        {complaints.length === 0 ? (
          <div style={{ padding: '60px', textAlign: 'center', color: '#7f8c8d' }}>
            <h4 style={{ marginBottom: '15px', fontSize: '24px' }}>No complaints found</h4>
            <p style={{ fontSize: '16px' }}>Create your first complaint to get started</p>
            <button
              onClick={() => navigate('/new-complaint')} // UPDATED
              style={{
                marginTop: '20px',
                padding: '12px 28px',
                backgroundColor: '#3498db',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '16px',
                fontWeight: '600'
              }}
            >
              Create First Complaint
            </button>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8f9fa', borderBottom: '2px solid #e1e5e9' }}>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>ID</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Title</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Category</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Priority</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Status</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Created Date</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Attachment</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Actions</th> {/* ADDED THIS COLUMN */}
                </tr>
              </thead>
              <tbody>
                {complaints.map((complaint) => (
                  <tr key={complaint.id} style={{ 
                    borderBottom: '1px solid #f0f0f0',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f8f9fa'}
                  onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                  >
                    <td style={{ padding: '20px', color: '#666', fontFamily: 'monospace', fontSize: '14px', fontWeight: '600' }}>#{complaint.id}</td>
                    
                    <td style={{ padding: '20px', fontWeight: '600', fontSize: '14px' }}>
                      <div style={{ maxWidth: '250px' }}>
                        {complaint.title}
                      </div>
                    </td>
                    
                    <td style={{ padding: '20px', fontSize: '14px' }}>
                      <span style={{
                        backgroundColor: '#e8f4fc',
                        color: '#3498db',
                        padding: '6px 15px',
                        borderRadius: '20px',
                        fontSize: '13px',
                        fontWeight: '600'
                      }}>
                        {complaint.category || 'General'}
                      </span>
                    </td>
                    
                    <td style={{ padding: '20px' }}>
                      <span style={{
                        backgroundColor: getPriorityColor(complaint.priority) + '20',
                        color: getPriorityColor(complaint.priority),
                        padding: '6px 15px',
                        borderRadius: '20px',
                        fontSize: '13px',
                        fontWeight: '600',
                        border: `1px solid ${getPriorityColor(complaint.priority)}`
                      }}>
                        {complaint.priority}
                      </span>
                    </td>
                    
                    <td style={{ padding: '20px' }}>
                      <span style={{
                        backgroundColor: getStatusColor(complaint.status) + '20',
                        color: getStatusColor(complaint.status),
                        padding: '6px 15px',
                        borderRadius: '20px',
                        fontSize: '13px',
                        fontWeight: '600',
                        border: `1px solid ${getStatusColor(complaint.status)}`
                      }}>
                        {complaint.status}
                      </span>
                    </td>
                    
                    <td style={{ padding: '20px', color: '#666', fontSize: '14px' }}>
                      {formatDate(complaint.createdAt)}
                    </td>
                    
                    <td style={{ padding: '20px' }}>
                      {complaint.fileName ? (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', minWidth: '200px' }}>
                          <div style={{ display: 'flex', gap: '10px' }}>
                            <button 
                              onClick={() => handleViewFile(complaint.id, complaint.fileName)}
                              style={{
                                backgroundColor: '#28a745',
                                color: 'white',
                                border: 'none',
                                padding: '8px 16px',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontSize: '13px',
                                fontWeight: '600',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '8px',
                                transition: 'background-color 0.3s',
                                flex: 1
                              }}
                              onMouseOver={(e) => e.target.style.backgroundColor = '#218838'}
                              onMouseOut={(e) => e.target.style.backgroundColor = '#28a745'}
                            >
                              <span>👁️</span>
                              View
                            </button>
                            <button 
                              onClick={() => handleDownload(complaint.id, complaint.fileName)}
                              style={{
                                backgroundColor: '#17a2b8',
                                color: 'white',
                                border: 'none',
                                padding: '8px 16px',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontSize: '13px',
                                fontWeight: '600',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '8px',
                                transition: 'background-color 0.3s',
                                flex: 1
                              }}
                              onMouseOver={(e) => e.target.style.backgroundColor = '#138496'}
                              onMouseOut={(e) => e.target.style.backgroundColor = '#17a2b8'}
                            >
                              <span>📥</span>
                              Download
                            </button>
                          </div>
                          <div style={{ fontSize: '12px', color: '#666' }}>
                            <div style={{ fontWeight: '500', wordBreak: 'break-word' }}>
                              {complaint.fileName}
                            </div>
                            {complaint.fileSize && (
                              <div style={{ fontSize: '11px', color: '#888', marginTop: '3px' }}>
                                {(complaint.fileSize / 1024).toFixed(2)} KB
                              </div>
                            )}
                          </div>
                        </div>
                      ) : (
                        <span style={{
                          color: '#999',
                          fontSize: '13px',
                          fontStyle: 'italic',
                          padding: '10px'
                        }}>
                          No attachment
                        </span>
                      )}
                    </td>

                    {/* ADDED: View Details Button Column */}
                    <td style={{ padding: '20px' }}>
                      <button 
                        onClick={() => handleViewDetails(complaint.id)}
                        style={{
                          backgroundColor: '#6f42c1',
                          color: 'white',
                          border: 'none',
                          padding: '10px 20px',
                          borderRadius: '6px',
                          cursor: 'pointer',
                          fontSize: '14px',
                          fontWeight: '600',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '8px',
                          transition: 'background-color 0.3s',
                          width: '100%',
                          justifyContent: 'center'
                        }}
                        onMouseOver={(e) => e.target.style.backgroundColor = '#59359b'}
                        onMouseOut={(e) => e.target.style.backgroundColor = '#6f42c1'}
                        title="View complaint details and comments"
                      >
                        <span>📋</span>
                        View Details
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Footer */}
      <div style={{
        marginTop: '30px',
        textAlign: 'center',
        color: '#999',
        fontSize: '14px',
        padding: '20px'
      }}>
        <p>Total: {complaints.length} complaints • My Complaints Panel</p>
      </div>

      <style>
        {`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}
      </style>
    </div>
  );
}

export default MyComplaintsPage;