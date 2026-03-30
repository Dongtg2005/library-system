import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { userAPI } from '../services/api';
import './Users.css';

const defaultForm = {
  authUserId: '',
  fullName: '',
  email: '',
  role: 'USER',
};

const Users = () => {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState(defaultForm);
  const [message, setMessage] = useState('');

  const isAdmin = user?.role === 'ADMIN';

  useEffect(() => {
    if (!isAdmin) {
      return;
    }

    const fetchUsers = async () => {
      try {
        const response = await userAPI.getUsers();
        const list = normalizeList(response.data);
        setUsers(list.length ? list : mockUsers);
      } catch (error) {
        setUsers(mockUsers);
        setMessage('Loaded sample users. Connect User Service for live management.');
      }
    };

    fetchUsers();
  }, [isAdmin]);

  if (!isAdmin) {
    return (
      <div className="users-page">
        <section className="panel denied-panel">
          <h3>Access Restricted</h3>
          <p>The Users module is available for ADMIN role only.</p>
        </section>
      </div>
    );
  }

  const addUser = async (event) => {
    event.preventDefault();

    const payload = {
      authUserId: Number(form.authUserId || Date.now()),
      fullName: form.fullName,
      email: form.email,
      role: form.role,
    };

    try {
      const response = await userAPI.createUser(payload);
      const created = response?.data?.id ? response.data : { ...payload, id: Date.now(), memberStatus: 'ACTIVE' };
      setUsers((prev) => [created, ...prev]);
      setMessage('User created successfully.');
    } catch (error) {
      setUsers((prev) => [{ ...payload, id: Date.now(), memberStatus: 'ACTIVE' }, ...prev]);
      setMessage('User API unavailable. Preview mode created this user locally.');
    }

    setForm(defaultForm);
  };

  const updateRole = async (id, role) => {
    try {
      await userAPI.updateRole(id, { role });
    } catch (error) {
      setMessage('Role updated locally.');
    }

    setUsers((prev) => prev.map((item) => (item.id === id ? { ...item, role } : item)));
  };

  const removeUser = async (id) => {
    try {
      await userAPI.deleteUser(id);
    } catch (error) {
      setMessage('User removed locally.');
    }

    setUsers((prev) => prev.filter((item) => item.id !== id));
  };

  return (
    <div className="users-page">
      <section className="panel">
        <h3>Add New User</h3>
        <form className="user-form" onSubmit={addUser}>
          <input
            value={form.authUserId}
            onChange={(event) => setForm((prev) => ({ ...prev, authUserId: event.target.value }))}
            placeholder="Auth User Id"
            required
          />
          <input
            value={form.fullName}
            onChange={(event) => setForm((prev) => ({ ...prev, fullName: event.target.value }))}
            placeholder="Full Name"
            required
          />
          <input
            type="email"
            value={form.email}
            onChange={(event) => setForm((prev) => ({ ...prev, email: event.target.value }))}
            placeholder="Email"
            required
          />
          <select value={form.role} onChange={(event) => setForm((prev) => ({ ...prev, role: event.target.value }))}>
            <option value="USER">User</option>
            <option value="ADMIN">Admin</option>
          </select>
          <button type="submit" className="primary-btn">Add User</button>
        </form>
        {message && <p className="panel-note">{message}</p>}
      </section>

      <section className="panel table-panel">
        <header>
          <h3>User Directory</h3>
          <p>{users.length} users</p>
        </header>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {users.map((item) => (
                <tr key={item.id}>
                  <td>{item.fullName}</td>
                  <td>{item.email}</td>
                  <td>
                    <select value={item.role || 'USER'} onChange={(event) => updateRole(item.id, event.target.value)}>
                      <option value="USER">User</option>
                      <option value="ADMIN">Admin</option>
                    </select>
                  </td>
                  <td>
                    <span className={`status-pill ${(item.memberStatus || 'ACTIVE') === 'ACTIVE' ? 'ok' : 'danger'}`}>
                      {item.memberStatus || 'ACTIVE'}
                    </span>
                  </td>
                  <td>
                    <button type="button" className="action-btn danger" onClick={() => removeUser(item.id)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
};

const normalizeList = (data) => {
  if (Array.isArray(data)) {
    return data;
  }
  if (Array.isArray(data?.content)) {
    return data.content;
  }
  return [];
};

const mockUsers = [
  { id: 1, fullName: 'Admin Main', email: 'admin@lms.com', role: 'ADMIN', memberStatus: 'ACTIVE' },
  { id: 2, fullName: 'Nguyen Reader', email: 'reader@lms.com', role: 'USER', memberStatus: 'ACTIVE' },
  { id: 3, fullName: 'Tran User', email: 'tran@lms.com', role: 'USER', memberStatus: 'SUSPENDED' },
];

export default Users;
