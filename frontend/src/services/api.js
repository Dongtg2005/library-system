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
  // Get user profile
  getProfile: () => apiClient.get('/users/me'),
  
  // Update user profile
  updateProfile: (data) => apiClient.put('/users/me', data),
  
  // Change password
  changePassword: (data) => apiClient.post('/users/change-password', data),
};

/**
 * Book API calls (Book Service)
 */
export const bookAPI = {
  // Get all books
  getBooks: (params) => apiClient.get('/books', { params }),
  
  // Search books
  searchBooks: (keyword) => apiClient.get('/books/search', { params: { q: keyword } }),
  
  // Get book details
  getBookDetails: (bookId) => apiClient.get(`/books/${bookId}`),
};

/**
 * Borrow API calls (Borrow Service)
 */
export const borrowAPI = {
  // Get user's borrowed books
  getBorrowedBooks: () => apiClient.get('/borrows/my-books'),
  
  // Borrow a book
  borrowBook: (bookId) => apiClient.post('/borrows', { bookId }),
  
  // Return a book
  returnBook: (borrowId) => apiClient.post(`/borrows/${borrowId}/return`),
  
  // Get borrow history
  getBorrowHistory: () => apiClient.get('/borrows/history'),
};

export default apiClient;
