import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

function AdminDashboard() {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedStatus, setSelectedStatus] = useState({});
  const [assignedTo, setAssignedTo] = useState({});
  const navigate = useNavigate();

  const userRole = localStorage.getItem('role');
  const username = localStorage.getItem('username');

  useEffect(() => {
    if (userRole !== 'ADMIN') {
      toast.error('Access denied. Admin only.');
      navigate('/complaints');
      return;
    }
    
    fetchComplaints();
  }, []);

  const fetchComplaints = async () => {
    try {
      setLoading(true);
      const response = await fetch('http://localhost:8080/api/complaints', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      
      const data = await response.json();
      
      if (data.success) {
        setComplaints(data.data || []);
        const statusMap = {};
        const assignMap = {};
        (data.data || []).forEach(comp => {
          statusMap[comp.id] = comp.status || 'OPEN';
          assignMap[comp.id] = comp.assignedTo || '';
        });
        setSelectedStatus(statusMap);
        setAssignedTo(assignMap);
      } else {
        toast.error(data.message || 'Failed to fetch complaints');
      }
    } catch (error) {
      console.error('Error:', error);
      toast.error('Cannot connect to server');
    } finally {
      setLoading(false);
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

  const updateStatus = async (complaintId) => {
    try {
      const response = await fetch(`http://localhost:8080/api/complaints/${complaintId}/status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          status: selectedStatus[complaintId],
          assignedTo: assignedTo[complaintId] || username
        })
      });

      const data = await response.json();
      
      if (data.success) {
        toast.success(`Status updated to ${selectedStatus[complaintId]}`);
        fetchComplaints();
      } else {
        toast.error(data.message || 'Failed to update status');
      }
    } catch (error) {
      console.error('Error:', error);
      toast.error('Failed to update status');
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

  const formatFileSize = (bytes) => {
    if (!bytes) return '0 bytes';
    if (bytes < 1024) return bytes + ' bytes';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
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

  const isVideoFile = (fileName) => {
    if (!fileName) return false;
    const videoExtensions = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.mkv', '.webm'];
    return videoExtensions.some(ext => fileName.toLowerCase().endsWith(ext));
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
      padding: '20px'
    }}>
      {/* Header */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '30px',
        padding: '20px',
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
          }}>Admin Dashboard</h1>
          <p style={{
            color: '#666',
            fontSize: '16px',
            margin: '5px 0'
          }}>Manage all complaints ({complaints.length})</p>
          <p style={{
            color: '#3498db',
            fontSize: '14px',
            margin: '5px 0'
          }}>Welcome, {username}</p>
        </div>
        <div style={{ display: 'flex', gap: '15px' }}>
          <button 
            style={{
              padding: '12px 24px',
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: '600',
              transition: 'background-color 0.3s'
            }}
            onMouseOver={(e) => e.target.style.backgroundColor = '#5a6268'}
            onMouseOut={(e) => e.target.style.backgroundColor = '#6c757d'}
            onClick={fetchComplaints}
          >
            Refresh
          </button>
          <button 
            style={{
              padding: '12px 24px',
              backgroundColor: '#3498db',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: '600',
              transition: 'background-color 0.3s'
            }}
            onMouseOver={(e) => e.target.style.backgroundColor = '#2980b9'}
            onMouseOut={(e) => e.target.style.backgroundColor = '#3498db'}
            onClick={() => navigate('/complaints')}
          >
            View User Panel
          </button>
        </div>
      </div>

      {/* Stats Cards */}
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
            textAlign: 'center'
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

      {/* Complaints Table */}
      <div style={{
        backgroundColor: 'white',
        borderRadius: '12px',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
        overflow: 'hidden'
      }}>
        {complaints.length === 0 ? (
          <div style={{ padding: '60px', textAlign: 'center', color: '#7f8c8d' }}>
            <h4 style={{ marginBottom: '15px', fontSize: '24px' }}>No complaints found</h4>
            <p style={{ fontSize: '16px' }}>All complaints will appear here</p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8f9fa', borderBottom: '2px solid #e1e5e9' }}>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>ID</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Title</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Description</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Status</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Assigned To</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Created By</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Created At</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Attachment</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Actions</th>
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
                    <td style={{ padding: '20px', color: '#666', fontFamily: 'monospace', fontSize: '14px' }}>#{complaint.id}</td>
                    <td style={{ padding: '20px', fontWeight: '600', fontSize: '14px' }}>{complaint.title}</td>
                    <td style={{ padding: '20px', color: '#555', maxWidth: '400px', fontSize: '14px' }}>
                      <div style={{
                        maxHeight: '60px',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        display: '-webkit-box',
                        WebkitLineClamp: 3,
                        WebkitBoxOrient: 'vertical'
                      }}>
                        {complaint.description}
                      </div>
                    </td>
                    
                    <td style={{ padding: '20px' }}>
                      <select
                        value={selectedStatus[complaint.id] || 'OPEN'}
                        onChange={(e) => setSelectedStatus({
                          ...selectedStatus,
                          [complaint.id]: e.target.value
                        })}
                        style={{
                          padding: '10px 15px',
                          borderRadius: '6px',
                          border: `2px solid ${getStatusColor(selectedStatus[complaint.id])}`,
                          backgroundColor: 'white',
                          color: '#2c3e50',
                          fontSize: '14px',
                          fontWeight: '500',
                          width: '100%',
                          cursor: 'pointer'
                        }}
                      >
                        <option value="OPEN">OPEN</option>
                        <option value="IN_PROGRESS">IN PROGRESS</option>
                        <option value="RESOLVED">RESOLVED</option>
                        <option value="CLOSED">CLOSED</option>
                        <option value="REJECTED">REJECTED</option>
                      </select>
                    </td>
                    
                    <td style={{ padding: '20px' }}>
                      <input
                        type="text"
                        value={assignedTo[complaint.id] || ''}
                        onChange={(e) => setAssignedTo({
                          ...assignedTo,
                          [complaint.id]: e.target.value
                        })}
                        placeholder="Assign to..."
                        style={{
                          padding: '10px 15px',
                          borderRadius: '6px',
                          border: '2px solid #ddd',
                          width: '100%',
                          fontSize: '14px',
                          boxSizing: 'border-box'
                        }}
                      />
                    </td>
                    
                    <td style={{ padding: '20px', color: '#666', fontSize: '14px' }}>
                      <span style={{
                        backgroundColor: '#e8f4fc',
                        color: '#3498db',
                        padding: '5px 12px',
                        borderRadius: '20px',
                        fontSize: '13px',
                        fontWeight: '600'
                      }}>
                        {complaint.userName || 'Unknown'}
                      </span>
                    </td>
                    
                    <td style={{ padding: '20px', color: '#666', fontSize: '14px' }}>{formatDate(complaint.createdAt)}</td>
                    
                    {/* Attachment Column */}
                    <td style={{ padding: '20px' }}>
                      {complaint.fileName ? (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
                          <button 
                            onClick={() => handleDownload(complaint.id, complaint.fileName)}
                            style={{
                              backgroundColor: '#17a2b8',
                              color: 'white',
                              border: 'none',
                              padding: '8px 15px',
                              borderRadius: '6px',
                              cursor: 'pointer',
                              fontSize: '13px',
                              fontWeight: '600',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              gap: '8px',
                              transition: 'background-color 0.3s',
                              width: '100%'
                            }}
                            onMouseOver={(e) => e.target.style.backgroundColor = '#138496'}
                            onMouseOut={(e) => e.target.style.backgroundColor = '#17a2b8'}
                          >
                            <span>📥</span>
                            Download
                          </button>
                          {isVideoFile(complaint.fileName) && (
                            <button 
                              onClick={() => navigate(`/admin/complaints/${complaint.id}`)}
                              style={{
                                backgroundColor: '#28a745',
                                color: 'white',
                                border: 'none',
                                padding: '8px 15px',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontSize: '13px',
                                fontWeight: '600',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                gap: '8px',
                                transition: 'background-color 0.3s',
                                width: '100%'
                              }}
                              onMouseOver={(e) => e.target.style.backgroundColor = '#218838'}
                              onMouseOut={(e) => e.target.style.backgroundColor = '#28a745'}
                            >
                              <span>🎬</span>
                              Play Video
                            </button>
                          )}
                          <div style={{ fontSize: '12px', color: '#666' }}>
                            <div style={{ fontWeight: '500', wordBreak: 'break-word' }}>{complaint.fileName}</div>
                            <div style={{ fontSize: '11px', color: '#888', marginTop: '3px' }}>
                              {formatFileSize(complaint.fileSize)}
                            </div>
                          </div>
                        </div>
                      ) : (
                        <span style={{
                          color: '#999',
                          fontSize: '13px',
                          fontStyle: 'italic'
                        }}>No attachment</span>
                      )}
                    </td>
                    
                    <td style={{ padding: '20px' }}>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                        <button
                          onClick={() => updateStatus(complaint.id)}
                          style={{
                            backgroundColor: '#28a745',
                            color: 'white',
                            border: 'none',
                            padding: '10px 20px',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            fontSize: '14px',
                            fontWeight: '600',
                            transition: 'background-color 0.3s',
                            width: '100%'
                          }}
                          onMouseOver={(e) => e.target.style.backgroundColor = '#218838'}
                          onMouseOut={(e) => e.target.style.backgroundColor = '#28a745'}
                        >
                          Update Status
                        </button>
                        <button
                          onClick={() => navigate(`/admin/complaints/${complaint.id}`)}
                          style={{
                            backgroundColor: '#3498db',
                            color: 'white',
                            border: 'none',
                            padding: '10px 20px',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            fontSize: '14px',
                            fontWeight: '600',
                            transition: 'background-color 0.3s',
                            width: '100%'
                          }}
                          onMouseOver={(e) => e.target.style.backgroundColor = '#2980b9'}
                          onMouseOut={(e) => e.target.style.backgroundColor = '#3498db'}
                        >
                          View Details
                        </button>
                      </div>
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
        <p>Total complaints: {complaints.length} | Admin Panel • IT Grievance System</p>
      </div>

      <style>
        {`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
          
          body {
            margin: 0;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
          }
          
          ::-webkit-scrollbar {
            width: 8px;
            height: 8px;
          }
          
          ::-webkit-scrollbar-track {
            background: #f1f1f1;
            border-radius: 4px;
          }
          
          ::-webkit-scrollbar-thumb {
            background: #888;
            border-radius: 4px;
          }
          
          ::-webkit-scrollbar-thumb:hover {
            background: #555;
          }
        `}
      </style>
    </div>
  );
}

export default AdminDashboard;