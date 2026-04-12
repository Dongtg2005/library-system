import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchBookById } from '../lib/api';

const FavoritesPage = () => {
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      const ids = JSON.parse(localStorage.getItem('favoriteBookIds') || '[]');
      const entries = await Promise.all(
        ids.map(async (id) => {
          try {
            return await fetchBookById(id);
          } catch {
            return null;
          }
        }),
      );

      if (mounted) {
        setFavorites(entries.filter(Boolean));
        setLoading(false);
      }
    };

    load();

    return () => {
      mounted = false;
    };
  }, []);

  return (
    <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 page-fade dark:border-slate-800 dark:bg-slate-900/75">
      <h1 className="text-3xl font-black text-slate-950 dark:text-white">Favorites</h1>
      <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">Uses local favorite IDs and fetches each book from API.</p>

      {loading ? (
        <div className="mt-6 rounded-2xl bg-slate-100 px-4 py-8 text-sm dark:bg-slate-800">Loading favorites...</div>
      ) : (
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {favorites.map((book) => (
            <Link key={book.id} to={`/books/${book.id}`} className="rounded-3xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-950/90">
              <h3 className="font-bold text-slate-950 dark:text-white">{book.title}</h3>
              <p className="mt-1 text-sm text-slate-600 dark:text-slate-300">{book.author}</p>
            </Link>
          ))}
          {!favorites.length && <div className="col-span-full rounded-2xl bg-slate-100 px-4 py-8 text-sm text-slate-600 dark:bg-slate-800 dark:text-slate-300">No favorites yet.</div>}
        </div>
      )}
    </div>
  );
};

export default FavoritesPage;
