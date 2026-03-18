import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

function AllComplaintsPage() {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();

  const token = localStorage.getItem('token');
  const username = localStorage.getItem('username');
  const role = localStorage.getItem('role');

  useEffect(() => {
    if (!token) {
      navigate('/login');
      return;
    }
    fetchComplaints();
  }, []);

  const fetchComplaints = async () => {
    try {
      setLoading(true);
      
      const endpoint = role === 'ADMIN' 
        ? 'http://localhost:8080/api/complaints' 
        : 'http://localhost:8080/api/complaints/my-complaints';
      
      const response = await fetch(endpoint, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      const data = await response.json();
      
      if (data.success) {
        setComplaints(data.data || []);
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

  const filteredComplaints = complaints.filter(complaint => 
    complaint.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    complaint.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    complaint.status?.toLowerCase().includes(searchTerm.toLowerCase())
  );

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
          }}>
            {role === 'ADMIN' ? 'All Complaints' : 'My Complaints'}
          </h1>
          <p style={{
            color: '#666',
            fontSize: '16px',
            margin: '5px 0'
          }}>
            {role === 'ADMIN' 
              ? `Total: ${complaints.length} complaints` 
              : `Hello ${username}, you have ${complaints.length} complaints`}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '15px' }}>
          <button 
            onClick={fetchComplaints}
            style={{
              padding: '12px 24px',
              backgroundColor: '#6c757d',
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
            onMouseOver={(e) => e.target.style.backgroundColor = '#5a6268'}
            onMouseOut={(e) => e.target.style.backgroundColor = '#6c757d'}
          >
            <span>🔄</span>
            Refresh
          </button>
          {role === 'ADMIN' && (
            <button 
              onClick={() => navigate('/admin')}
              style={{
                padding: '12px 24px',
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
              <span>âš™ï¸</span>
              Admin Panel
            </button>
          )}
        </div>
      </div>

      {/* Search Bar */}
      <div style={{
        marginBottom: '30px',
        padding: '20px',
        backgroundColor: 'white',
        borderRadius: '12px',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
      }}>
        <div style={{
          position: 'relative',
          display: 'flex',
          alignItems: 'center'
        }}>
          <input
            type="text"
            placeholder="Search complaints by title, description, or status..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{
              width: '100%',
              padding: '15px 15px 15px 50px',
              fontSize: '16px',
              border: '2px solid #e1e5e9',
              borderRadius: '8px',
              boxSizing: 'border-box',
              transition: 'border-color 0.3s'
            }}
            onFocus={(e) => e.target.style.borderColor = '#3498db'}
            onBlur={(e) => e.target.style.borderColor = '#e1e5e9'}
          />
          <div style={{
            position: 'absolute',
            left: '15px',
            fontSize: '20px',
            color: '#666'
          }}>
            ðŸ”
          </div>
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
        {filteredComplaints.length === 0 ? (
          <div style={{ padding: '60px', textAlign: 'center', color: '#7f8c8d' }}>
            <h4 style={{ marginBottom: '15px', fontSize: '24px' }}>
              {role === 'ADMIN' ? 'No complaints found' : 'You have no complaints yet'}
            </h4>
            <p style={{ fontSize: '16px' }}>
              {role === 'ADMIN' 
                ? 'All complaints will appear here' 
                : 'Create your first complaint to get started'}
            </p>
            {role !== 'ADMIN' && (
              <button 
                onClick={() => navigate('/new-complaint')}
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
            )}
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
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Created By</th>
                  <th style={{ padding: '20px', textAlign: 'left', fontWeight: '600', color: '#2c3e50', fontSize: '14px' }}>Created At</th>
                </tr>
              </thead>
              <tbody>
                {filteredComplaints.map((complaint) => (
                  <tr key={complaint.id} style={{ 
                    borderBottom: '1px solid #f0f0f0',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f8f9fa'}
                  onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                  >
                    <td style={{ padding: '20px', color: '#666', fontFamily: 'monospace', fontSize: '14px', fontWeight: '600' }}>
                      #{complaint.id}
                    </td>
                    
                    <td style={{ padding: '20px', fontWeight: '600', fontSize: '14px' }}>
                      <div style={{ maxWidth: '250px' }}>
                        {complaint.title}
                      </div>
                    </td>
                    
                    <td style={{ padding: '20px', color: '#555', fontSize: '14px' }}>
                      <div style={{
                        maxHeight: '60px',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        display: '-webkit-box',
                        WebkitLineClamp: 3,
                        WebkitBoxOrient: 'vertical',
                        maxWidth: '400px'
                      }}>
                        {complaint.description}
                      </div>
                    </td>
                    
                    <td style={{ padding: '20px' }}>
                      <span style={{
                        backgroundColor: getStatusColor(complaint.status) + '20',
                        color: getStatusColor(complaint.status),
                        padding: '8px 18px',
                        borderRadius: '20px',
                        fontSize: '13px',
                        fontWeight: '600',
                        border: `1px solid ${getStatusColor(complaint.status)}`,
                        display: 'inline-block'
                      }}>
                        {complaint.status || 'OPEN'}
                      </span>
                    </td>
                    
                    <td style={{ padding: '20px', color: '#666', fontSize: '14px' }}>
                      <span style={{
                        backgroundColor: '#e8f4fc',
                        color: '#3498db',
                        padding: '6px 15px',
                        borderRadius: '20px',
                        fontSize: '13px',
                        fontWeight: '600'
                      }}>
                        {complaint.createdBy || 'Unknown'}
                      </span>
                    </td>
                    
                    <td style={{ padding: '20px', color: '#666', fontSize: '14px' }}>
                      {formatDate(complaint.createdAt)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Footer Info */}
      <div style={{
        marginTop: '30px',
        padding: '20px',
        backgroundColor: 'white',
        borderRadius: '12px',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
        textAlign: 'center',
        color: '#666',
        fontSize: '14px'
      }}>
        <p style={{ margin: '0 0 10px 0' }}>
          Showing {filteredComplaints.length} of {complaints.length} complaints
        </p>
        {searchTerm && (
          <p style={{ margin: '5px 0', color: '#3498db' }}>
            Filtered by: "{searchTerm}"
          </p>
        )}
        {role !== 'ADMIN' && (
          <p style={{ margin: '5px 0', color: '#28a745' }}>
            You can only see your own complaints
          </p>
        )}
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

export default AllComplaintsPage;
