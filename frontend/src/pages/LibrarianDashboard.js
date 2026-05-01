import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import Button from '../components/Button';
import { CheckIcon, XMarkIcon, BookOpenIcon, ClockIcon, ExclamationTriangleIcon } from '@heroicons/react/24/outline';

const LibrarianDashboard = () => {
  const navigate = useNavigate();
  const { user, token } = useAuth();
  const toast = useToast();
  
  const [pendingRequests, setPendingRequests] = useState([]);
  const [overdueBooks, setOverdueBooks] = useState([]);
  const [todayReturns, setTodayReturns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    pendingCount: 0,
    overdueCount: 0,
    todayReturnCount: 0,
    totalBorrowed: 0,
  });

  useEffect(() => {
    if (token) {
      loadDashboardData();
    }
  }, [token]);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      // Load pending borrow requests
      const pendingRes = await fetch('/api/v1/borrows?status=PENDING_APPROVAL', {
        headers: { 'Authorization': `Bearer ${token}` },
      });
      if (pendingRes.ok) {
        const pendingData = await pendingRes.json();
        setPendingRequests(pendingData.content || []);
        setStats(prev => ({ ...prev, pendingCount: pendingData.totalElements || 0 }));
      }

      // Load overdue books
      const overdueRes = await fetch('/api/v1/borrows?status=OVERDUE', {
        headers: { 'Authorization': `Bearer ${token}` },
      });
      if (overdueRes.ok) {
        const overdueData = await overdueRes.json();
        setOverdueBooks(overdueData.content || []);
        setStats(prev => ({ ...prev, overdueCount: overdueData.totalElements || 0 }));
      }

      // Load today's returns (ACTIVE with due date today)
      const today = new Date().toISOString().split('T')[0];
      const returnsRes = await fetch(`/api/v1/borrows?status=ACTIVE&dueDate=${today}`, {
        headers: { 'Authorization': `Bearer ${token}` },
      });
      if (returnsRes.ok) {
        const returnsData = await returnsRes.json();
        setTodayReturns(returnsData.content || []);
        setStats(prev => ({ ...prev, todayReturnCount: returnsData.totalElements || 0 }));
      }

      // Load total borrowed
      const activeRes = await fetch('/api/v1/borrows?status=ACTIVE', {
        headers: { 'Authorization': `Bearer ${token}` },
      });
      if (activeRes.ok) {
        const activeData = await activeRes.json();
        setStats(prev => ({ ...prev, totalBorrowed: activeData.totalElements || 0 }));
      }
    } catch (err) {
      console.error('Dashboard load error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (borrowId) => {
    try {
      const response = await fetch(`/api/v1/borrows/${borrowId}/approve`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}` },
      });
      if (response.ok) {
        toast?.addToast({ type: 'success', title: 'Approved', message: 'Borrow request approved' });
        loadDashboardData();
      } else {
        throw new Error('Failed to approve');
      }
    } catch (err) {
      toast?.addToast({ type: 'error', title: 'Error', message: err.message });
    }
  };

  const handleReject = async (borrowId) => {
    const reason = prompt('Enter rejection reason (optional):') || '';
    try {
      const response = await fetch(`/api/v1/borrows/${borrowId}/reject?reason=${encodeURIComponent(reason)}`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}` },
      });
      if (response.ok) {
        toast?.addToast({ type: 'success', title: 'Rejected', message: 'Borrow request rejected' });
        loadDashboardData();
      } else {
        throw new Error('Failed to reject');
      }
    } catch (err) {
      toast?.addToast({ type: 'error', title: 'Error', message: err.message });
    }
  };

  const handleConfirmReturn = async (borrowId) => {
    try {
      const response = await fetch(`/api/v1/borrows/${borrowId}/return`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}` },
      });
      if (response.ok) {
        toast?.addToast({ type: 'success', title: 'Returned', message: 'Book returned successfully' });
        loadDashboardData();
      } else {
        throw new Error('Failed to confirm return');
      }
    } catch (err) {
      toast?.addToast({ type: 'error', title: 'Error', message: err.message });
    }
  };

  // Task cards
  const taskCards = [
    { 
      title: 'Pending Approvals', 
      count: stats.pendingCount, 
      icon: BookOpenIcon,
      color: 'bg-amber-500',
      urgent: stats.pendingCount > 5 
    },
    { 
      title: 'Overdue Books', 
      count: stats.overdueCount, 
      icon: ExclamationTriangleIcon,
      color: 'bg-rose-500',
      urgent: stats.overdueCount > 0 
    },
    { 
      title: 'Due Today', 
      count: stats.todayReturnCount, 
      icon: ClockIcon,
      color: 'bg-teal-500',
      urgent: false 
    },
    { 
      title: 'Active Borrows', 
      count: stats.totalBorrowed, 
      icon: BookOpenIcon,
      color: 'bg-emerald-500',
      urgent: false 
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">
          Librarian Workstation
        </p>
        <h2 className="mt-2 text-3xl font-black text-slate-900 dark:text-white">
          Welcome, {user?.fullName || user?.email}!
        </h2>
        <p className="mt-2 text-slate-600 dark:text-slate-400">
          Role: <span className="font-semibold text-amber-600">{user?.role}</span> | Process borrow requests and manage returns
        </p>
      </div>

      {/* Task Summary Cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {taskCards.map((card) => (
          <div 
            key={card.title}
            className={`rounded-2xl border bg-white p-5 shadow-sm transition hover:shadow-md dark:border-slate-800 dark:bg-slate-900 ${card.urgent ? 'ring-2 ring-rose-500/30' : ''}`}
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-slate-500 dark:text-slate-400">{card.title}</p>
                <p className={`mt-1 text-3xl font-black ${card.urgent ? 'text-rose-600' : 'text-slate-900 dark:text-white'}`}>
                  {card.count}
                </p>
              </div>
              <div className={`${card.color} rounded-xl p-3 text-white`}>
                <card.icon className="h-6 w-6" />
              </div>
            </div>
            {card.urgent && (
              <p className="mt-2 text-xs font-semibold text-rose-600">⚠️ Requires attention</p>
            )}
          </div>
        ))}
      </div>

      {/* Pending Approvals Table */}
      <div className="rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="border-b border-slate-200 p-5 dark:border-slate-800">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">Pending Borrow Requests</h3>
              <p className="text-sm text-slate-500 dark:text-slate-400">Review and approve borrow requests</p>
            </div>
            <Button variant="secondary" onClick={loadDashboardData} disabled={loading}>
              {loading ? 'Loading...' : 'Refresh'}
            </Button>
          </div>
        </div>
        
        {pendingRequests.length === 0 ? (
          <div className="p-8 text-center text-slate-500 dark:text-slate-400">
            <BookOpenIcon className="mx-auto h-12 w-12 opacity-30" />
            <p className="mt-2">No pending requests</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-slate-50 dark:bg-slate-800/50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-slate-500 dark:text-slate-400">User</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-slate-500 dark:text-slate-400">Book</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-slate-500 dark:text-slate-400">Request Date</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-slate-500 dark:text-slate-400">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                {pendingRequests.map((req) => (
                  <tr key={req.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30">
                    <td className="px-4 py-3">
                      <p className="font-medium text-slate-900 dark:text-white">User #{req.memberId}</p>
                    </td>
                    <td className="px-4 py-3">
                      <p className="text-slate-600 dark:text-slate-300">Book #{req.bookId?.slice(0, 8)}</p>
                    </td>
                    <td className="px-4 py-3 text-sm text-slate-500 dark:text-slate-400">
                      {new Date(req.borrowDate).toLocaleDateString()}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2">
                        <Button 
                          size="sm" 
                          onClick={() => handleApprove(req.id)}
                          className="bg-emerald-600 hover:bg-emerald-700"
                        >
                          <CheckIcon className="h-4 w-4" /> Approve
                        </Button>
                        <Button 
                          size="sm" 
                          variant="secondary"
                          onClick={() => handleReject(req.id)}
                          className="text-rose-600 hover:bg-rose-50"
                        >
                          <XMarkIcon className="h-4 w-4" /> Reject
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Two Column Layout: Overdue & Today's Returns */}
      <div className="grid gap-6 lg:grid-cols-2">
        {/* Overdue Books */}
        <div className="rounded-2xl border border-rose-200 bg-rose-50/50 shadow-sm dark:border-rose-900/30 dark:bg-rose-900/10">
          <div className="border-b border-rose-200 p-5 dark:border-rose-900/30">
            <h3 className="text-lg font-bold text-rose-900 dark:text-rose-400">⚠️ Overdue Books</h3>
            <p className="text-sm text-rose-700 dark:text-rose-500">Books past due date requiring action</p>
          </div>
          {overdueBooks.length === 0 ? (
            <div className="p-6 text-center text-rose-600/60 dark:text-rose-500/60">
              <p>No overdue books</p>
            </div>
          ) : (
            <div className="max-h-64 overflow-y-auto">
              {overdueBooks.map((book) => (
                <div key={book.id} className="flex items-center justify-between border-b border-rose-100 p-4 last:border-0 dark:border-rose-900/20">
                  <div>
                    <p className="font-medium text-rose-900 dark:text-rose-400">Book #{book.bookId?.slice(0, 8)}</p>
                    <p className="text-sm text-rose-700 dark:text-rose-500">Due: {new Date(book.dueDate).toLocaleDateString()}</p>
                  </div>
                  <Button size="sm" onClick={() => handleConfirmReturn(book.id)}>
                    Confirm Return
                  </Button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Today's Returns */}
        <div className="rounded-2xl border border-teal-200 bg-teal-50/50 shadow-sm dark:border-teal-900/30 dark:bg-teal-900/10">
          <div className="border-b border-teal-200 p-5 dark:border-teal-900/30">
            <h3 className="text-lg font-bold text-teal-900 dark:text-teal-400">📅 Due Today</h3>
            <p className="text-sm text-teal-700 dark:text-teal-500">Books expected to be returned today</p>
          </div>
          {todayReturns.length === 0 ? (
            <div className="p-6 text-center text-teal-600/60 dark:text-teal-500/60">
              <p>No books due today</p>
            </div>
          ) : (
            <div className="max-h-64 overflow-y-auto">
              {todayReturns.map((book) => (
                <div key={book.id} className="flex items-center justify-between border-b border-teal-100 p-4 last:border-0 dark:border-teal-900/20">
                  <div>
                    <p className="font-medium text-teal-900 dark:text-teal-400">Book #{book.bookId?.slice(0, 8)}</p>
                    <p className="text-sm text-teal-700 dark:text-teal-500">User #{book.memberId}</p>
                  </div>
                  <Button size="sm" variant="secondary" onClick={() => handleConfirmReturn(book.id)}>
                    Received
                  </Button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Quick Links */}
      <div className="flex gap-4">
        <Button variant="secondary" onClick={() => navigate('/admin/borrow')}>
          View All Borrows
        </Button>
        <Button variant="secondary" onClick={() => navigate('/admin/books')}>
          Manage Books
        </Button>
      </div>
    </div>
  );
};

export default LibrarianDashboard;
