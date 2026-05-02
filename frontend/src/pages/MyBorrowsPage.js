import React, { useCallback, useEffect, useState } from 'react';
import Button from '../components/Button';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';
import { extendBorrow, fetchBookById, fetchBorrowHistory, returnBorrow } from '../lib/api';
import { useToast } from '../context/ToastContext';

const MyBorrowsPage = () => {
  const { token } = useAuth();
  const { t } = useTranslation();
  const { addToast } = useToast() || {};
  const [activeTab, setActiveTab] = useState('borrowed');
  const [records, setRecords] = useState([]);
  const [bookMap, setBookMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState('');

  const tabs = [
    { key: 'borrowed', label: t('myBorrowsPage.borrowed'), statuses: ['ACTIVE', 'PENDING_APPROVAL'] },
    { key: 'returned', label: t('myBorrowsPage.returned'), statuses: ['RETURNED'] },
    { key: 'overdue', label: t('myBorrowsPage.overdue'), statuses: ['OVERDUE'] },
  ];

  const load = useCallback(async () => {
    if (!token) return;
    setLoading(true);

    try {
      const history = await fetchBorrowHistory(token);
      setRecords(history || []);

      const ids = [...new Set((history || []).map((item) => item.bookId).filter(Boolean))];
      const entries = await Promise.all(
        ids.map(async (id) => {
          try {
            const book = await fetchBookById(id);
            return [id, book];
          } catch {
            return [id, null];
          }
        }),
      );

      setBookMap(Object.fromEntries(entries));
    } catch (error) {
      addToast?.({ type: 'error', title: t('myBorrowsPage.loadFailed'), message: error.message || t('myBorrowsPage.loadFailedMessage') });
      setRecords([]);
      setBookMap({});
    } finally {
      setLoading(false);
    }
  }, [addToast, token, t]);

  useEffect(() => {
    load();
  }, [load]);

  const list = records.filter((item) => tabs.find((tab) => tab.key === activeTab)?.statuses.includes(item.borrowStatus));

  const onExtend = async (borrowId) => {
    if (!token) return;
    setSubmitting(borrowId);
    try {
      await extendBorrow(token, borrowId);
      addToast?.({ type: 'success', title: t('myBorrowsPage.extended'), message: t('myBorrowsPage.extendedMessage') });
      await load();
    } catch (error) {
      addToast?.({ type: 'error', title: t('myBorrowsPage.extendFailed'), message: error.message || t('myBorrowsPage.extendFailedMessage') });
    } finally {
      setSubmitting('');
    }
  };

  const onReturn = async (borrowId) => {
    if (!token) return;
    setSubmitting(borrowId);
    try {
      await returnBorrow(token, borrowId, t('myBorrowsPage.returnedFromUserPortal'));
      addToast?.({ type: 'success', title: t('myBorrowsPage.returned'), message: t('myBorrowsPage.returnedMessage') });
      await load();
    } catch (error) {
      addToast?.({ type: 'error', title: t('myBorrowsPage.returnFailed'), message: error.message || t('myBorrowsPage.returnFailedMessage') });
    } finally {
      setSubmitting('');
    }
  };

  return (
    <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
      <h1 className="text-3xl font-black text-slate-950 dark:text-white">{t('nav.myBooks')}</h1>
      <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{t('myBorrowsPage.dataFrom')}</p>

      <div className="mt-6 flex flex-wrap gap-2">
        {tabs.map((tab) => {
          const count = records.filter((item) => tab.statuses.includes(item.borrowStatus)).length;
          return (
            <button
              key={tab.key}
              type="button"
              onClick={() => setActiveTab(tab.key)}
              className={`rounded-full px-4 py-2 text-sm font-semibold transition ${
                tab.key === activeTab
                  ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
                  : 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-200'
              }`}
            >
              {tab.label} ({count})
            </button>
          );
        })}
      </div>

      {loading ? (
        <div className="mt-6 rounded-2xl bg-slate-100 px-4 py-8 text-sm dark:bg-slate-800">{t('myBorrowsPage.loading')}</div>
      ) : (
        <div className="mt-6 overflow-hidden rounded-3xl border border-slate-200 dark:border-slate-800">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-100 dark:bg-slate-800">
              <tr>
                <th className="px-4 py-3">{t('myBorrowsPage.book')}</th>
                <th className="px-4 py-3">{t('myBorrowsPage.borrowDate')}</th>
                <th className="px-4 py-3">{t('myBorrowsPage.dueDate')}</th>
                <th className="px-4 py-3">{t('common.status')}</th>
                <th className="px-4 py-3">{t('myBorrowsPage.action')}</th>
              </tr>
            </thead>
            <tbody>
              {list.map((item) => (
                <tr key={item.id} className="border-t border-slate-200 dark:border-slate-800">
                  <td className="px-4 py-3">{bookMap[item.bookId]?.title || item.bookId}</td>
                  <td className="px-4 py-3">{item.borrowDate || '-'}</td>
                  <td className="px-4 py-3">{item.dueDate || '-'}</td>
                  <td className="px-4 py-3">{item.borrowStatus}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      {['ACTIVE', 'PENDING_APPROVAL'].includes(item.borrowStatus) && (
                        <Button size="sm" variant="secondary" disabled={submitting === item.id} onClick={() => onExtend(item.id)}>
                          {t('myBorrowsPage.extend')}
                        </Button>
                      )}
                      {['ACTIVE', 'OVERDUE', 'PENDING_APPROVAL'].includes(item.borrowStatus) && (
                        <Button size="sm" disabled={submitting === item.id} onClick={() => onReturn(item.id)}>
                          {t('myBorrowsPage.return')}
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {!list.length && <div className="px-4 py-8 text-sm text-slate-600 dark:text-slate-300">{t('myBorrowsPage.noRecords')}</div>}
        </div>
      )}
    </div>
  );
};

export default MyBorrowsPage;
