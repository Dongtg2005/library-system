import React, { useMemo } from 'react';
import { useAuth } from '../context/AuthContext';
import './Notifications.css';

const Notifications = () => {
  const { user } = useAuth();

  const notifications = useMemo(() => {
    const now = new Date().toLocaleString('en-GB');
    return [
      {
        id: 1,
        type: 'overdue',
        title: 'Overdue Reminder',
        message: '7 books are overdue and require reminders.',
        time: now,
      },
      {
        id: 2,
        type: 'system',
        title: 'System Message',
        message: 'Borrow service synchronization completed successfully.',
        time: now,
      },
      {
        id: 3,
        type: 'activity',
        title: 'New Borrowing Peak',
        message: 'Borrow volume increased by 18% this week.',
        time: now,
      },
      {
        id: 4,
        type: 'account',
        title: 'Role Updated',
        message: `${user?.fullName || 'User'} profile permissions were verified.`,
        time: now,
      },
    ];
  }, [user]);

  return (
    <div className="notifications-page">
      <section className="panel">
        <h3>Alerts and Messages</h3>
        <p className="subtitle">Overdue reminders and system updates from all services.</p>

        <div className="notification-list">
          {notifications.map((item) => (
            <article key={item.id} className={`notification-card ${item.type}`}>
              <div>
                <h4>{item.title}</h4>
                <p>{item.message}</p>
              </div>
              <span>{item.time}</span>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
};

export default Notifications;
