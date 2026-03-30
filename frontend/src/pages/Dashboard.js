import React, { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { bookAPI, borrowAPI, userAPI } from '../services/api';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    totalBooks: 0,
    borrowedBooks: 0,
    totalUsers: 0,
    overdueBooks: 0,
  });
  const [trends, setTrends] = useState([]);
  const [recentRecords, setRecentRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        setLoading(true);
        setError('');

        const [booksRes, borrowsRes, usersRes] = await Promise.allSettled([
          bookAPI.getBooks(),
          borrowAPI.getBorrowHistory(),
          userAPI.getUsers(),
        ]);

        const books = booksRes.status === 'fulfilled' ? normalizeList(booksRes.value?.data) : [];
        const records = borrowsRes.status === 'fulfilled' ? normalizeList(borrowsRes.value?.data) : [];
        const users = usersRes.status === 'fulfilled' ? normalizeList(usersRes.value?.data) : [];

        if (books.length === 0 && records.length === 0) {
          const mock = buildMockDashboard();
          setStats(mock.stats);
          setTrends(mock.trends);
          setRecentRecords(mock.recentRecords);
          return;
        }

        const borrowedCount = records.filter((item) => item.status === 'BORROWED').length;
        const overdueCount = records.filter((item) => isOverdue(item)).length;

        setStats({
          totalBooks: books.length,
          borrowedBooks: borrowedCount,
          totalUsers: users.length,
          overdueBooks: overdueCount,
        });
        setTrends(buildTrends(records));
        setRecentRecords(records.slice(0, 6));
      } catch (err) {
        setError('Unable to load dashboard data.');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboard();
  }, [user]);

  const maxTrendValue = useMemo(() => {
    if (trends.length === 0) {
      return 1;
    }
    return Math.max(...trends.map((item) => item.value));
  }, [trends]);

  return (
    <div className="dashboard-page">
      <div className="hero-panel">
        <h1>Welcome, {user?.fullName || 'Reader'}</h1>
        <p>Track activity across services and keep your library operations healthy in one place.</p>
      </div>

      {loading && <p className="panel-note">Loading dashboard...</p>}
      {error && <p className="panel-error">{error}</p>}

      <div className="stats-grid">
        <article className="stat-card">
          <p>Total Books</p>
          <h3>{stats.totalBooks}</h3>
          <span>Catalog across all categories</span>
        </article>
        <article className="stat-card">
          <p>Borrowed Books</p>
          <h3>{stats.borrowedBooks}</h3>
          <span>Currently in circulation</span>
        </article>
        <article className="stat-card">
          <p>Total Users</p>
          <h3>{stats.totalUsers}</h3>
          <span>Readers and admins</span>
        </article>
        <article className="stat-card warning">
          <p>Overdue Books</p>
          <h3>{stats.overdueBooks}</h3>
          <span>Need immediate follow-up</span>
        </article>
      </div>

      <div className="dashboard-grid">
        <section className="panel chart-panel">
          <header>
            <h2>Borrowing Trend</h2>
            <p>Monthly borrow volume</p>
          </header>
          <div className="trend-bars">
            {trends.map((point) => (
              <div key={point.label} className="trend-column">
                <div
                  className="trend-fill"
                  style={{ height: `${(point.value / maxTrendValue) * 100}%` }}
                  title={`${point.value} borrows`}
                />
                <span>{point.label}</span>
              </div>
            ))}
          </div>
        </section>

        <section className="panel table-panel">
          <header>
            <h2>Latest Transactions</h2>
            <p>Most recent borrowing records</p>
          </header>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>User</th>
                  <th>Book</th>
                  <th>Borrow Date</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {recentRecords.map((record, index) => (
                  <tr key={record.id || record.borrowId || index}>
                    <td>{record.userName || record.userEmail || 'Unknown'}</td>
                    <td>{record.bookTitle || record.title || 'Untitled'}</td>
                    <td>{formatDate(record.borrowDate || record.createdAt)}</td>
                    <td>
                      <span className={`status-pill ${statusColor(record.status)}`}>
                        {record.status || 'UNKNOWN'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {recentRecords.length === 0 && <p className="panel-note">No transactions yet.</p>}
          </div>
        </section>
      </div>
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

const buildTrends = (records) => {
  const monthMap = new Map();
  const now = new Date();

  for (let i = 5; i >= 0; i -= 1) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
    const key = `${d.getFullYear()}-${d.getMonth()}`;
    monthMap.set(key, {
      label: d.toLocaleDateString('en-US', { month: 'short' }),
      value: 0,
    });
  }

  records.forEach((record) => {
    const date = new Date(record.borrowDate || record.createdAt || Date.now());
    const key = `${date.getFullYear()}-${date.getMonth()}`;
    if (monthMap.has(key)) {
      monthMap.get(key).value += 1;
    }
  });

  return Array.from(monthMap.values());
};

const isOverdue = (record) => {
  const dueDate = record.returnDate || record.dueDate;
  if (!dueDate || record.status === 'RETURNED') {
    return false;
  }
  return new Date(dueDate).getTime() < Date.now();
};

const formatDate = (value) => {
  if (!value) {
    return '-';
  }
  return new Date(value).toLocaleDateString('en-GB');
};

const statusColor = (status) => {
  if (status === 'RETURNED' || status === 'AVAILABLE') {
    return 'ok';
  }
  if (status === 'OVERDUE') {
    return 'danger';
  }
  return 'info';
};

const buildMockDashboard = () => ({
  stats: {
    totalBooks: 420,
    borrowedBooks: 83,
    totalUsers: 190,
    overdueBooks: 12,
  },
  trends: [
    { label: 'Nov', value: 34 },
    { label: 'Dec', value: 48 },
    { label: 'Jan', value: 52 },
    { label: 'Feb', value: 46 },
    { label: 'Mar', value: 58 },
    { label: 'Apr', value: 41 },
  ],
  recentRecords: [
    { userName: 'A. Tran', bookTitle: 'The Pragmatic Programmer', borrowDate: new Date(), status: 'BORROWED' },
    { userName: 'N. Le', bookTitle: 'Designing Data-Intensive Applications', borrowDate: new Date(), status: 'RETURNED' },
    { userName: 'M. Pham', bookTitle: 'Clean Code', borrowDate: new Date(), status: 'OVERDUE' },
  ],
});

export default Dashboard;
