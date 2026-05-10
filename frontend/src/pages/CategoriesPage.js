import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchCategories, searchBooks } from '../lib/api';
import { useTranslation } from '../context/LanguageContext';
import { formatCategoryName } from '../lib/categoryLabels';

const CategoriesPage = () => {
  const { t, language } = useTranslation();
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      try {
        const cats = await fetchCategories();
        if (!mounted) return;

        // Count books per category in parallel
        const withCounts = await Promise.all(
          cats.map(async (cat) => {
            try {
              const res = await searchBooks({ category: cat.name, size: 1 });
              return { ...cat, total: res?.totalElements ?? 0 };
            } catch {
              return { ...cat, total: 0 };
            }
          })
        );

        if (mounted) {
          setCategories(
            withCounts
              .filter((c) => c.total > 0)
              .sort((a, b) => b.total - a.total)
          );
        }
      } catch {
        if (mounted) setCategories([]);
      } finally {
        if (mounted) setLoading(false);
      }
    };

    load();

    return () => {
      mounted = false;
    };
  }, []);

  return (
    <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
      <h1 className="text-3xl font-black text-slate-950 dark:text-white">{t('categoriesPage.title')}</h1>

      {loading ? (
        <div className="mt-6 rounded-2xl bg-slate-100 px-4 py-8 text-sm dark:bg-slate-800">{t('categoriesPage.loading')}</div>
      ) : (
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {categories.map((category) => (
            <Link key={category.id} to={`/books?category=${encodeURIComponent(category.name)}`} className="rounded-3xl border border-slate-200 bg-white p-5 transition hover:-translate-y-1 hover:shadow-lg dark:border-slate-800 dark:bg-slate-950/90">
              <p className="text-xs uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">{t('nav.categories')}</p>
              <h3 className="mt-2 text-xl font-black text-slate-950 dark:text-white">{formatCategoryName(category.name, language)}</h3>
              <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{category.total} {t('categoriesPage.booksCount')}</p>
            </Link>
          ))}
          {!categories.length && (
            <div className="col-span-full rounded-2xl bg-slate-100 px-4 py-8 text-sm text-slate-600 dark:bg-slate-800 dark:text-slate-300">{t('categoriesPage.noCategories')}</div>
          )}
        </div>
      )}
    </div>
  );
};

export default CategoriesPage;
