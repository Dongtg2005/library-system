import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8000/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to request headers
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Handle response errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('access_token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/**
 * Authentication API calls
 */
export const authAPI = {
  // Register new user
  register: (data) => apiClient.post('/auth/register', data),
  
  // Login user
  login: (data) => apiClient.post('/auth/login', data),
  
  // Get current user info
  getCurrentUser: () => apiClient.get('/auth/me'),
  
  // Validate token
  validateToken: (token) => apiClient.get(`/auth/validate?token=${token}`),
  
  // Logout
  logout: () => apiClient.post('/auth/logout'),
};

/**
 * User API calls (User Service)
 */
export const userAPI = {
  getProfile: () => apiClient.get('/users/me'),
  updateProfile: (data) => apiClient.put('/users/me', data),
  changePassword: (data) => apiClient.post('/users/change-password', data),
  getUsers: () => apiClient.get('/users'),
  createUser: (data) => apiClient.post('/users', data),
  updateUser: (id, data) => apiClient.put(`/users/${id}`, data),
  updateRole: (id, data) => apiClient.patch(`/users/${id}/role`, data),
  updateStatus: (id, data) => apiClient.patch(`/users/${id}/status`, data),
  deleteUser: (id) => apiClient.delete(`/users/${id}`),
};

/**
 * Book API calls (Book Service)
 */
export const bookAPI = {
  getBooks: (params) => apiClient.get('/books', { params }),
  searchBooks: (keyword) => apiClient.get('/books/search', { params: { q: keyword } }),
  getBookDetails: (bookId) => apiClient.get(`/books/${bookId}`),
  createBook: (data) => apiClient.post('/books', data),
  updateBook: (bookId, data) => apiClient.put(`/books/${bookId}`, data),
  deleteBook: (bookId) => apiClient.delete(`/books/${bookId}`),
};

/**
 * Borrow API calls (Borrow Service)
 */
export const borrowAPI = {
  getBorrowedBooks: () => apiClient.get('/borrows/my-books'),
  borrowBook: (bookId) => apiClient.post('/borrows', { bookId }),
  returnBook: (borrowId) => apiClient.post(`/borrows/${borrowId}/return`),
  getBorrowHistory: () => apiClient.get('/borrows/history'),
  getBorrowRecords: () => apiClient.get('/borrows'),
};

export default apiClient;
