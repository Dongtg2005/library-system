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

