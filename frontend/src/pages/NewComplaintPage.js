import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';

function NewComplaintPage() {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    priority: 'MEDIUM',
    category: 'Software'
  });
  
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated()) {
      toast.error('Please login to submit a complaint');
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      if (selectedFile.size > 10 * 1024 * 1024) {
        toast.error('File size should be less than 10MB');
        return;
      }
      setFile(selectedFile);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!isAuthenticated()) {
      toast.error('Please login to submit a complaint');
      navigate('/login');
      return;
    }
    
    if (!formData.title.trim() || !formData.description.trim()) {
      toast.error('Please fill in all required fields');
      return;
    }
    
    setLoading(true);
    
    try {
      const formDataToSend = new FormData();
      formDataToSend.append('title', formData.title);
      formDataToSend.append('description', formData.description);
      formDataToSend.append('category', formData.category);
      formDataToSend.append('priority', formData.priority);
      
      if (file) {
        formDataToSend.append('file', file);
      }
      
      const response = await axios.post('http://localhost:8080/api/complaints', formDataToSend, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'multipart/form-data'
        }
      });
      
      if (response.data.success) {
        toast.success('Complaint submitted successfully!');
        setFormData({
          title: '',
          description: '',
          priority: 'MEDIUM',
          category: 'Software'
        });
        setFile(null);
        navigate('/my-complaints');
      } else {
        toast.error(response.data.message || 'Failed to submit complaint');
      }
    } catch (error) {
      console.error('Error submitting complaint:', error);
      
      if (error.response) {
        if (error.response.status === 401 || error.response.status === 403) {
          toast.error('Session expired. Please login again.');
          logout();
          navigate('/login');
        } else if (error.response.status === 400) {
          toast.error(error.response.data.message || 'Invalid data. Please check your input.');
        } else if (error.response.status === 413) {
          toast.error('File size too large. Maximum 10MB allowed.');
        } else {
          toast.error('Server error: ' + (error.response.data.message || 'Please try again.'));
        }
      } else if (error.request) {
        toast.error('Network error. Please check your internet connection.');
      } else {
        toast.error('Error: ' + error.message);
      }
    } finally {
      setLoading(false);
    }
  };

  if (!isAuthenticated()) {
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
          <p style={{ fontSize: '18px', color: '#2c3e50' }}>Redirecting to login...</p>
        </div>
      </div>
    );
  }

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#f5f7fa',
      padding: '30px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }}>
      <div style={{
        width: '100%',
        maxWidth: '900px',
        backgroundColor: 'white',
        borderRadius: '16px',
        boxShadow: '0 10px 30px rgba(0,0,0,0.1)',
        overflow: 'hidden'
      }}>
        {/* Header */}
        <div style={{
          padding: '30px',
          backgroundColor: '#3498db',
          color: 'white'
        }}>
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: '20px'
          }}>
            <div>
              <h1 style={{
                fontSize: '32px',
                fontWeight: 'bold',
                marginBottom: '10px',
                margin: 0
              }}>Create New Complaint</h1>
              <p style={{
                fontSize: '16px',
                opacity: 0.9,
                margin: 0
              }}>Submit your IT grievance with details</p>
            </div>
            <div style={{
              backgroundColor: 'rgba(255,255,255,0.2)',
              padding: '10px 20px',
              borderRadius: '30px',
              fontSize: '14px',
              fontWeight: '600'
            }}>
              {user?.username} ({user?.role})
            </div>
          </div>
        </div>

        {/* Form */}
        <div style={{ padding: '40px' }}>
          <form onSubmit={handleSubmit}>
            {/* Title */}
            <div style={{ marginBottom: '30px' }}>
              <label style={{
                display: 'block',
                fontSize: '16px',
                fontWeight: '600',
                color: '#2c3e50',
                marginBottom: '12px'
              }}>
                Title *
              </label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleInputChange}
                required
                maxLength="200"
                placeholder="Enter complaint title"
                style={{
                  width: '100%',
                  padding: '15px',
                  fontSize: '16px',
                  border: '2px solid #e1e5e9',
                  borderRadius: '8px',
                  boxSizing: 'border-box',
                  transition: 'border-color 0.3s'
                }}
                onFocus={(e) => e.target.style.borderColor = '#3498db'}
                onBlur={(e) => e.target.style.borderColor = '#e1e5e9'}
              />
            </div>

            {/* Description */}
            <div style={{ marginBottom: '30px' }}>
              <label style={{
                display: 'block',
                fontSize: '16px',
                fontWeight: '600',
                color: '#2c3e50',
                marginBottom: '12px'
              }}>
                Description *
              </label>
              <textarea
                rows="6"
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                required
                placeholder="Describe your complaint in detail"
                style={{
                  width: '100%',
                  padding: '15px',
                  fontSize: '16px',
                  border: '2px solid #e1e5e9',
                  borderRadius: '8px',
                  boxSizing: 'border-box',
                  resize: 'vertical',
                  transition: 'border-color 0.3s',
                  fontFamily: 'inherit'
                }}
                onFocus={(e) => e.target.style.borderColor = '#3498db'}
                onBlur={(e) => e.target.style.borderColor = '#e1e5e9'}
              />
            </div>

            {/* Category and Priority */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: '1fr 1fr',
              gap: '30px',
              marginBottom: '30px'
            }}>
              {/* Category */}
              <div>
                <label style={{
                  display: 'block',
                  fontSize: '16px',
                  fontWeight: '600',
                  color: '#2c3e50',
                  marginBottom: '12px'
                }}>
                  Category
                </label>
                <select
                  name="category"
                  value={formData.category}
                  onChange={handleInputChange}
                  style={{
                    width: '100%',
                    padding: '15px',
                    fontSize: '16px',
                    border: '2px solid #e1e5e9',
                    borderRadius: '8px',
                    boxSizing: 'border-box',
                    backgroundColor: 'white',
                    cursor: 'pointer',
                    transition: 'border-color 0.3s'
                  }}
                  onFocus={(e) => e.target.style.borderColor = '#3498db'}
                  onBlur={(e) => e.target.style.borderColor = '#e1e5e9'}
                >
                  <option value="Software">Software</option>
                  <option value="Hardware">Hardware</option>
                  <option value="Network">Network</option>
                  <option value="Email">Email</option>
                  <option value="Access_Rights">Access Rights</option>
                  <option value="Security">Security</option>
                  <option value="Data_Loss">Data Loss</option>
                  <option value="Other">Other</option>
                </select>
              </div>

              {/* Priority */}
              <div>
                <label style={{
                  display: 'block',
                  fontSize: '16px',
                  fontWeight: '600',
                  color: '#2c3e50',
                  marginBottom: '12px'
                }}>
                  Priority
                </label>
                <select
                  name="priority"
                  value={formData.priority}
                  onChange={handleInputChange}
                  style={{
                    width: '100%',
                    padding: '15px',
                    fontSize: '16px',
                    border: '2px solid #e1e5e9',
                    borderRadius: '8px',
                    boxSizing: 'border-box',
                    backgroundColor: 'white',
                    cursor: 'pointer',
                    transition: 'border-color 0.3s'
                  }}
                  onFocus={(e) => e.target.style.borderColor = '#3498db'}
                  onBlur={(e) => e.target.style.borderColor = '#e1e5e9'}
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                </select>
              </div>
            </div>

            {/* File Attachment */}
            <div style={{ marginBottom: '40px' }}>
              <label style={{
                display: 'block',
                fontSize: '16px',
                fontWeight: '600',
                color: '#2c3e50',
                marginBottom: '12px'
              }}>
                Attachment (Optional)
              </label>
              <div style={{
                border: '2px dashed #e1e5e9',
                borderRadius: '8px',
                padding: '30px',
                textAlign: 'center',
                cursor: 'pointer',
                transition: 'border-color 0.3s',
                position: 'relative'
              }}
              onDragOver={(e) => {
                e.preventDefault();
                e.currentTarget.style.borderColor = '#3498db';
              }}
              onDragLeave={(e) => {
                e.currentTarget.style.borderColor = '#e1e5e9';
              }}
              >
                <input
                  type="file"
                  onChange={handleFileChange}
                  accept=".pdf,.doc,.docx,.jpg,.jpeg,.png,.txt"
                  style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    width: '100%',
                    height: '100%',
                    opacity: 0,
                    cursor: 'pointer'
                  }}
                />
                <div style={{ fontSize: '50px', color: '#3498db', marginBottom: '15px' }}>📎</div>
                <div style={{ fontSize: '18px', color: '#2c3e50', marginBottom: '10px' }}>
                  {file ? file.name : 'Click or drag to upload file'}
                </div>
                <div style={{ fontSize: '14px', color: '#666' }}>
                  Max 10MB • PDF, DOC, DOCX, JPG, PNG, TXT
                </div>
              </div>
              
              {file && (
                <div style={{
                  marginTop: '15px',
                  padding: '15px',
                  backgroundColor: '#e8f4fc',
                  borderRadius: '8px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '15px'
                }}>
                  <div style={{ fontSize: '24px', color: '#3498db' }}>📄</div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: '16px', fontWeight: '600', color: '#2c3e50' }}>
                      {file.name}
                    </div>
                    <div style={{ fontSize: '14px', color: '#666', marginTop: '5px' }}>
                      {(file.size / 1024).toFixed(2)} KB
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => setFile(null)}
                    style={{
                      backgroundColor: '#dc3545',
                      color: 'white',
                      border: 'none',
                      padding: '8px 16px',
                      borderRadius: '6px',
                      cursor: 'pointer',
                      fontSize: '14px',
                      fontWeight: '600',
                      transition: 'background-color 0.3s'
                    }}
                    onMouseOver={(e) => e.target.style.backgroundColor = '#c82333'}
                    onMouseOut={(e) => e.target.style.backgroundColor = '#dc3545'}
                  >
                    Remove
                  </button>
                </div>
              )}
            </div>

            {/* Buttons */}
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              gap: '20px',
              paddingTop: '20px',
              borderTop: '1px solid #e1e5e9'
            }}>
              <button 
                type="button"
                onClick={() => navigate('/my-complaints')}
                style={{
                  padding: '15px 30px',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  fontSize: '16px',
                  fontWeight: '600',
                  transition: 'background-color 0.3s',
                  flex: 1
                }}
                onMouseOver={(e) => e.target.style.backgroundColor = '#5a6268'}
                onMouseOut={(e) => e.target.style.backgroundColor = '#6c757d'}
              >
                Cancel
              </button>
              <button 
                type="submit"
                disabled={loading}
                style={{
                  padding: '15px 30px',
                  backgroundColor: loading ? '#95a5a6' : '#28a745',
                  color: 'white',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  fontSize: '16px',
                  fontWeight: '600',
                  transition: 'background-color 0.3s',
                  flex: 2,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '10px'
                }}
                onMouseOver={(e) => !loading && (e.target.style.backgroundColor = '#218838')}
                onMouseOut={(e) => !loading && (e.target.style.backgroundColor = '#28a745')}
              >
                {loading ? (
                  <>
                    <div style={{
                      width: '20px',
                      height: '20px',
                      border: '3px solid rgba(255,255,255,0.3)',
                      borderTop: '3px solid white',
                      borderRadius: '50%',
                      animation: 'spin 1s linear infinite'
                    }}></div>
                    Submitting...
                  </>
                ) : 'Submit Complaint'}
              </button>
            </div>
          </form>
        </div>
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

export default NewComplaintPage;