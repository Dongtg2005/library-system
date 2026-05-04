const buildHeaders = (token, extraHeaders = {}) => ({
  'Content-Type': 'application/json',
  ...(token ? { Authorization: `Bearer ${token}` } : {}),
  ...extraHeaders,
});

const withQuery = (path, params = {}) => {
  const search = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value);
    }
  });

  const query = search.toString();
  return query ? `${path}?${query}` : path;
};

export const apiRequest = async (path, { method = 'GET', token, body, headers } = {}) => {
  const response = await fetch(path, {
    method,
    headers: buildHeaders(token, headers),
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  });

  if (response.status === 204) {
    return null;
  }

  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    const message = payload?.message || payload?.error || `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return payload;
};

export const fetchBooks = (params = {}) => apiRequest(withQuery('/api/v1/books', params));

export const searchBooks = (params = {}) => apiRequest(withQuery('/api/v1/books/search', params));

export const autocompleteBooks = (q) => apiRequest(withQuery('/api/v1/books/autocomplete', { q }));

export const fetchBookById = (id) => apiRequest(`/api/v1/books/${id}`);

export const fetchCategories = (token) => apiRequest('/api/v1/categories', { token });

export const fetchCategoriesTree = (token) => apiRequest('/api/v1/categories/tree', { token });
export const createBook = (token, payload) =>
  apiRequest('/api/v1/books', {
    method: 'POST',
    token,
    body: payload,
  });

export const updateBook = (token, id, payload) =>
  apiRequest(`/api/v1/books/${id}`, {
    method: 'PUT',
    token,
    body: payload,
  });

export const deleteBook = (token, id) =>
  apiRequest(`/api/v1/books/${id}`, {
    method: 'DELETE',
    token,
  });

export const fetchCurrentUser = (token) => apiRequest('/api/v1/auth/me', { token });

export const fetchUsers = (token, params = {}) =>
  apiRequest(withQuery('/api/v1/users', params), { token });

export const createUser = (token, payload) =>
  apiRequest('/api/v1/users', {
    method: 'POST',
    token,
    body: payload,
  });

export const updateUser = (token, id, payload) =>
  apiRequest(`/api/v1/users/${id}`, {
    method: 'PUT',
    token,
    body: payload,
  });

export const deleteUser = (token, id) =>
  apiRequest(`/api/v1/users/${id}`, {
    method: 'DELETE',
    token,
  });

export const fetchBorrowHistory = (token) => apiRequest('/api/v1/borrows/history', { token });

export const checkBorrowStatus = (token, bookId) =>
  apiRequest(`/api/v1/borrows/check?bookId=${bookId}`, { token });

export const createBorrow = (token, bookId, notes = '') =>
  apiRequest('/api/v1/borrows', {
    method: 'POST',
    token,
    body: {
      bookId,
      conditionOnBorrow: 'GOOD',
      notes,
    },
  });

export const extendBorrow = (token, borrowRecordId) =>
  apiRequest(`/api/v1/borrows/${borrowRecordId}/extend`, {
    method: 'POST',
    token,
  });

export const returnBorrow = (token, borrowRecordId, returnNotes = '') =>
  apiRequest('/api/v1/borrows/return', {
    method: 'POST',
    token,
    body: {
      borrowRecordId,
      conditionOnReturn: 'GOOD',
      returnNotes,
    },
  });

export const fetchDashboardStats = (token) =>
  apiRequest('/api/v1/analytics/dashboard', { token });

export const fetchReviews = (bookId, params = {}) =>
  apiRequest(withQuery(`/api/v1/books/${bookId}/reviews`, params));

export const addReview = (token, bookId, payload) =>
  apiRequest(`/api/v1/books/${bookId}/reviews`, {
    method: 'POST',
    token,
    body: payload,
  });

export const createReservation = (token, payload) =>
  apiRequest('/api/v1/reservations', {
    method: 'POST',
    token,
    body: payload,
  });

export const cancelReservation = (token, reservationId) =>
  apiRequest(`/api/v1/reservations/${reservationId}`, {
    method: 'DELETE',
    token,
  });

export const getMyReservations = (token) =>
  apiRequest('/api/v1/reservations/my-reservations', { token });

export const getBookReservations = (token, bookId) =>
  apiRequest(`/api/v1/reservations/book/${bookId}`, { token });

export const getAllReservations = (token, params = {}) =>
  apiRequest(withQuery('/api/v1/reservations', params), { token });

export const fulfillReservation = (token, reservationId) =>
  apiRequest(`/api/v1/reservations/${reservationId}/fulfill`, {
    method: 'POST',
    token,
  });

export const getReservationCount = (token, bookId) =>
  apiRequest(`/api/v1/reservations/book/${bookId}/count`, { token });

// Notification API
export const getNotifications = (token) =>
  apiRequest('/api/v1/notifications', { token });

export const getNotificationCount = (token) =>
  apiRequest('/api/v1/notifications/count', { token });

export const getUnreadNotifications = (token) =>
  apiRequest('/api/v1/notifications/unread', { token });

export const markAsRead = (token, notificationId) =>
  apiRequest(`/api/v1/notifications/${notificationId}/read`, {
    method: 'PUT',
    token
  });

export const markAllAsRead = (token) =>
  apiRequest('/api/v1/notifications/read-all', {
    method: 'PUT',
    token
  });

export const deleteNotification = (token, notificationId) =>
  apiRequest(`/api/v1/notifications/${notificationId}`, {
    method: 'DELETE',
    token
  });

export const deleteAllNotifications = async (token) => {
  const response = await fetch('/api/v1/notifications', {
    method: 'DELETE',
    headers: buildHeaders(token),
  });

  if (!response.ok) {
    throw new Error('Failed to delete all notifications');
  }

  return response;
};

// Reviews API
export const fetchBookReviews = (bookId, params = {}) =>
  apiRequest(withQuery(`/api/v1/books/${bookId}/reviews`, params));

export const fetchBookReviewsPopular = (bookId) =>
  apiRequest(`/api/v1/books/${bookId}/reviews/popular`);

export const fetchBookRatingSummary = (bookId) =>
  apiRequest(`/api/v1/books/${bookId}/reviews/summary`);

export const updateReview = (token, reviewId, payload) =>
  apiRequest(`/api/v1/reviews/${reviewId}`, {
    method: 'PUT',
    token,
    body: payload,
  });

export const deleteReview = (token, reviewId) =>
  apiRequest(`/api/v1/reviews/${reviewId}`, {
    method: 'DELETE',
    token,
  });

export const voteReview = (token, payload) =>
  apiRequest('/api/v1/reviews/vote', {
    method: 'POST',
    token,
    body: payload,
  });

export const fetchReviewComments = (reviewId) =>
  apiRequest(`/api/v1/reviews/${reviewId}/comments`);

export const addComment = (token, payload) =>
  apiRequest('/api/v1/reviews/comments', {
    method: 'POST',
    token,
    body: payload,
  });

export const likeComment = (token, commentId) =>
  apiRequest(`/api/v1/reviews/comments/${commentId}/like`, {
    method: 'POST',
    token,
  });

export const deleteComment = (token, commentId) =>
  apiRequest(`/api/v1/reviews/comments/${commentId}`, {
    method: 'DELETE',
    token,
  });
