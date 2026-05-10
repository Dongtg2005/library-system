// Import các icon từ Heroicons library (để hiển thị trên UI)
import { PencilSquareIcon, TrashIcon, BookOpenIcon, MagnifyingGlassIcon, PlusIcon, ChevronUpDownIcon, CheckIcon, XMarkIcon as CloseIcon } from '@heroicons/react/24/outline';
// Import Listbox component từ Headless UI (dùng cho dropdown select multiple categories)
import { Listbox, Transition } from '@headlessui/react';
// Import React hooks và Fragment để quản lý state và render elements
import React, { useEffect, useMemo, useState, Fragment } from 'react';
// Import các custom components
import Button from './Button';
import Input from './Input';
import Modal from './Modal';
import BookCoverUpload from './BookCoverUpload';
// Import mock data và API functions
import { bookRows, PagedData } from '../data/mockData';
import { createBook, createCategory, deleteBook, fetchBooks, searchBooks, updateBook, fetchCategoriesTree } from '../lib/api';
import { formatCategoryList, formatCategoryName } from '../lib/categoryLabels';
// Import custom hooks
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';

// Số lượng sách hiển thị trên mỗi trang
const pageSize = 6;

const BookTable = () => {
  // Lấy token JWT từ AuthContext để sử dụng trong API calls
  const { token } = useAuth();
  // Lấy function translate để hỗ trợ đa ngôn ngữ
  const { t, language } = useTranslation();
  
  // ===== STATE MANAGEMENT =====
  // Keyword tìm kiếm sách
  const [query, setQuery] = useState('');
  // Số trang hiện tại (bắt đầu từ 1)
  const [page, setPage] = useState(1);
  // Trường để sắp xếp (title, author, category, isbn, totalQuantity, status)
  const [sortKey, setSortKey] = useState('title');
  // Danh sách sách từ API
  const [books, setBooks] = useState([]);
  // Tổng số sách trong database
  const [totalItems, setTotalItems] = useState(0);
  // Trạng thái đang tải dữ liệu
  const [loading, setLoading] = useState(false);
  // Thông báo lỗi khi tải dữ liệu
  const [error, setError] = useState('');
  // Sách được chọn để edit (null khi create mới)
  const [selected, setSelected] = useState(null);
  // Cờ để kiểm soát modal (tạo mới hay edit)
  const [creating, setCreating] = useState(false);
  // Dữ liệu form: title, author, categoryIds (mảng ID), isbn, totalQuantity
  const [form, setForm] = useState({ title: '', author: '', categoryIds: [], isbn: '', totalQuantity: 1 });
  // Lỗi validation của form
  const [formError, setFormError] = useState('');
  // Danh sách danh mục (categories) từ API
  const [categoryList, setCategoryList] = useState([]);
  // Tên danh mục mới đang nhập
  const [newCategoryName, setNewCategoryName] = useState('');
  // Trạng thái đang tạo danh mục mới
  const [creatingCategory, setCreatingCategory] = useState(false);

  // Chuẩn hóa mock data: chuyển đổi định dạng của bookRows để match với API response
  const normalizedMockBooks = useMemo(
    () =>
      bookRows.map((row) => ({
        id: row.id,
        title: row.title,
        author: row.author,
        category: row.category,
        isbn: row.isbn,
        totalQuantity: Number(row.stock || 0),
        availableQty: Number(row.available || 0),
        status: String(row.status || 'AVAILABLE').toUpperCase().replace(/\s+/g, '_'),
      })),
    []
  );

  // Format trạng thái sách: chuyển 'OUT_OF_STOCK' thành 'Out Of Stock'
  const formatStatus = (status) => String(status || 'UNKNOWN').replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (s) => s.toUpperCase());

  // ===== HÀM TẢI DỮ LIỆU =====
  // Tải danh sách sách từ API theo page, sort key, và keyword tìm kiếm
  const loadBooks = async () => {
    setLoading(true);
    setError('');

    try {
      // Tạo params cho API: page (0-based), size = 6 items/page
      const params = { page: Math.max(page - 1, 0), size: pageSize };
      // Nếu có query, gọi searchBooks API, ngược lại gọi fetchBooks (get all)
      const payload = query.trim() ? await searchBooks({ ...params, title: query.trim() }) : await fetchBooks(params);

      // Lấy content từ response, nếu không có thì dùng mảng rỗng
      const content = Array.isArray(payload?.content) ? payload.content : [];
      // Cập nhật state với dữ liệu từ API
      setBooks(content);
      // Lưu tổng số item để tính số trang
      setTotalItems(Number(payload?.totalElements || content.length));
    } catch (apiError) {
      // Nếu API fail, hiển thị thông báo lỗi
      setError(apiError.message || t('bookTable.loadFailedFallback'));

      // Fallback: sử dụng mock data và filter theo query
      const filteredMock = normalizedMockBooks.filter((row) =>
        [row.title, row.author, row.category, row.isbn].some((value) => String(value || '').toLowerCase().includes(query.toLowerCase()))
      );

      setTotalItems(filteredMock.length);
      setBooks(filteredMock.slice((page - 1) * pageSize, page * pageSize));
    } finally {
      setLoading(false);
    }
  };

  // ===== EFFECTS & MEMOIZATION =====
  // Hook chạy khi page, query, hoặc token thay đổi
  useEffect(() => {
    // Tải lại danh sách sách
    loadBooks();
    // Tải danh sách categories từ API để hiển thị trong dropdown
    fetchCategoriesTree(token).then(data => {
      if (Array.isArray(data)) setCategoryList(data);
    }).catch(err => console.error('Failed to load categories', err));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, query, token]);

  // Sắp xếp danh sách sách dựa vào sortKey được chọn
  const sortedRows = useMemo(() => {
    return [...books].sort((a, b) => String(a[sortKey] ?? '').localeCompare(String(b[sortKey] ?? '')));
  }, [books, sortKey]);

  // Tính toán tổng số trang dựa vào totalItems và pageSize
  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));

  // ===== MODAL HANDLERS =====
  // Mở modal tạo sách mới
  const openCreate = () => {
    setCreating(true);
    setSelected(null);
    // Reset form với giá trị mặc định
    setForm({ title: '', author: '', categoryIds: [], isbn: '', totalQuantity: 1 });
    setFormError('');
  };

  // Mở modal edit sách: điền dữ liệu sách vào form
  const openEdit = (row) => {
    setSelected(row); // Lưu sách được edit
    setCreating(false);
    // Điền dữ liệu sách vào form, categoryIds là mảng ID từ mảng categories
    setForm({
      title: row.title || '',
      author: row.author || '',
      categoryIds: row.categories && row.categories.length > 0 ? row.categories.map(c => c.id) : [],
      isbn: row.isbn || '',
      totalQuantity: Number(row.totalQuantity || 1),
    });
    setFormError('');
  };

  // Đóng modal và reset form
  const closeModal = () => {
    setSelected(null);
    setCreating(false);
    setFormError('');
  };

  // ===== SAVE & DELETE HANDLERS =====
  // Xử lý lưu sách (tạo mới hoặc update)
  const handleSave = async () => {
    // Validate: title, isbn, totalQuantity không được bỏ trống
    if (!form.title.trim() || !form.isbn.trim() || !Number(form.totalQuantity)) {
      setFormError(t('bookTable.requiredFields'));
      return;
    }

    // Kiểm tra authentication token
    if (!token) {
      setFormError(t('auth.loginRequired'));
      return;
    }

    // Chuẩn bị dữ liệu payload: trim các string, giữ nguyên categoryIds array
    const payload = {
      title: form.title.trim(),
      author: form.author.trim(),
      categoryIds: form.categoryIds, // Mảng ID categories
      isbn: form.isbn.trim(),
      totalQuantity: Number(form.totalQuantity),
    };

    try {
      // Nếu selected?.id tồn tại = đang edit, ngược lại = create mới
      if (selected?.id) {
        await updateBook(token, selected.id, {
          title: payload.title,
          author: payload.author,
          categoryIds: payload.categoryIds,
          totalQuantity: payload.totalQuantity,
        });
      } else {
        await createBook(token, payload);
      }

      // Sau khi lưu thành công, đóng modal và tải lại danh sách
      closeModal();
      loadBooks();
    } catch (saveError) {
      setFormError(saveError.message || t('bookTable.saveFailed'));
    }
  };

  // Tạo danh mục mới và tự động chọn vào form
  const handleCreateCategory = async () => {
    const name = newCategoryName.trim();
    if (!name || !token) return;
    setCreatingCategory(true);
    try {
      const created = await createCategory(token, { name, description: '' });
      setCategoryList((prev) => [...prev, created]);
      setForm((prev) => ({ ...prev, categoryIds: [...prev.categoryIds, created.id] }));
      setNewCategoryName('');
    } catch (err) {
      setFormError(err.message || t('bookTable.saveFailed'));
    } finally {
      setCreatingCategory(false);
    }
  };

  // Xử lý xóa sách
  const handleDelete = async (id) => {
    // Kiểm tra token và id hợp lệ
    if (!token || !id) return;

    try {
      // Gọi API xóa sách
      await deleteBook(token, id);
      // Nếu sách bị xóa là sách đang edit, đóng modal
      if (selected?.id === id) closeModal();
      // Tải lại danh sách
      loadBooks();
    } catch (deleteError) {
      setError(deleteError.message || t('bookTable.deleteFailed'));
    }
  };

  return (
    <div className="space-y-5">
      {/* ===== SEARCH & ACTION BAR ===== */}
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        {/* Search input: người dùng nhập từ khóa tìm kiếm */}
        <Input
          label={t('bookTable.searchBooks')}
          value={query}
          onChange={(e) => {
            setQuery(e.target.value);
            setPage(1); // Reset về trang 1 khi tìm kiếm
          }}
          placeholder={t('bookTable.searchTitleAuthorCategory')}
        />
        {/* Nút "Thêm sách mới" - mở modal tạo sách */}
        <Button className="lg:mt-7" onClick={openCreate}>
          <PlusIcon className="h-5 w-5" />{t('bookTable.addBook')}
        </Button>
      </div>

      {/* Hiển thị thông báo lỗi nếu có (ví dụ: lỗi tải dữ liệu) */}
      {error && <p className="text-sm text-amber-600">{error}</p>}

      {/* ===== BOOKS TABLE ===== */}
      <div className="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-xl dark:border-slate-800 dark:bg-slate-950">
        <div className="overflow-x-auto">
        <table className="min-w-[840px] w-full divide-y divide-slate-200 dark:divide-slate-800">
          {/* HEADER ROW - Tiêu đề các cột */}
          <thead className="bg-slate-50 dark:bg-slate-900">
            <tr>
              {[
                { label: t('bookTable.bookTitle'), sort: 'title' },
                { label: t('bookTable.author'), sort: 'author' },
                { label: t('bookTable.category'), sort: 'category' },
                { label: t('bookTable.isbn'), sort: 'isbn' },
                { label: t('bookTable.stock'), sort: 'totalQuantity' },
                { label: t('bookTable.status'), sort: 'status' },
                { label: t('common.actions') },
              ].map((head) => (
                <th key={head.label} className="px-5 py-4 text-left text-xs font-bold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">
                  {/* Button để click và thay đổi sortKey - sắp xếp theo cột này */}
                  <button
                    className="inline-flex items-center gap-2"
                    onClick={() => head.sort && setSortKey(head.sort)}
                  >
                    {head.label}
                    {head.sort && <MagnifyingGlassIcon className="h-3.5 w-3.5 opacity-30" />}
                  </button>
                </th>
              ))}
            </tr>
          </thead>
          {/* BODY - Danh sách sách */}
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
            {/* Hiển thị loading message khi đang tải */}
            {loading ? (
              <tr>
                <td colSpan={7} className="px-5 py-6 text-sm text-slate-500">
                  {t('bookTable.loading')}
                </td>
              </tr>
            ) : (
              // Render từng sách từ sortedRows (đã được sắp xếp)
              sortedRows.map((row) => (
                <tr key={row.id} className="transition hover:bg-slate-50/70 dark:hover:bg-slate-900/50">
                  {/* Cột: Tên sách */}
                  <td className="px-5 py-4 font-semibold text-slate-900 dark:text-white">{row.title}</td>
                  {/* Cột: Tác giả */}
                  <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.author || '-'}</td>
                  {/* Cột: Danh mục (hiển thị category name hoặc '-' nếu không có) */}
                  <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.category || '-'}</td>
                  {/* Cột: ISBN */}
                  <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{row.isbn}</td>
                  {/* Cột: Kho (availableQty/totalQuantity) */}
                  <td className="px-5 py-4 text-slate-600 dark:text-slate-300">{Number(row.availableQty || 0)}/{Number(row.totalQuantity || 0)}</td>
                  {/* Cột: Trạng thái (AVAILABLE, OUT_OF_STOCK, ARCHIVED) - hiển thị dưới dạng badge */}
                  <td className="px-5 py-4">
                    <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-bold text-primary">{formatStatus(row.status)}</span>
                  </td>
                  {/* Cột: Actions (Edit, Delete) */}
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-2">
                      {/* Nút Edit - mở modal edit */}
                      <Button variant="ghost" size="sm" onClick={() => openEdit(row)}><PencilSquareIcon className="h-4 w-4" />{t('bookTable.edit')}</Button>
                      {/* Nút Delete - xóa sách */}
                      <Button variant="ghost" size="sm" className="text-rose-500 hover:bg-rose-500/10" onClick={() => handleDelete(row.id)}><TrashIcon className="h-4 w-4" />{t('bookTable.delete')}</Button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
        </div>
      </div>

      {/* ===== PAGINATION ===== */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        {/* Text hiển thị: "Showing 6 of 15" */}
        <p className="text-sm text-slate-500 dark:text-slate-400">{t('bookTable.showing', { count: sortedRows.length, total: totalItems })}</p>
        <div className="flex flex-wrap gap-2">
          {/* Nút "Previous" - quay lại trang trước */}
          <Button variant="secondary" size="sm" disabled={page === 1 || loading} onClick={() => setPage((p) => Math.max(1, p - 1))}>{t('bookTable.prev')}</Button>
          {/* Nút "Next" - đến trang sau */}
          <Button variant="secondary" size="sm" disabled={page === totalPages || loading} onClick={() => setPage((p) => Math.min(totalPages, p + 1))}>{t('bookTable.next')}</Button>
        </div>
      </div>

      {/* ===== BOOK FORM MODAL ===== */}
      <Modal open={creating || !!selected} onClose={closeModal} title={selected ? t('bookTable.editBook') : t('bookTable.addBookTitle')}>
        <div className="space-y-4">
          {/* INPUT: Tên sách */}
          <Input label={t('book.label.title')} value={form.title} onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))} />
          
          {/* INPUT: Tác giả */}
          <Input label={t('book.label.author')} value={form.author} onChange={(e) => setForm((prev) => ({ ...prev, author: e.target.value }))} />
          
          {/* ===== CATEGORY MULTI-SELECT ===== */}
          <div className="space-y-1 relative">
            <label className="mb-2 block text-sm font-bold text-slate-700 dark:text-slate-300">{t('book.label.category')}</label>
            {/* Listbox component từ Headless UI - cho phép chọn nhiều danh mục */}
            <Listbox
              value={form.categoryIds} // Mảng ID danh mục được chọn
              onChange={(values) => setForm(prev => ({ ...prev, categoryIds: values }))} // Update form.categoryIds
              multiple // Cho phép chọn nhiều
            >
              <div className="relative mt-1">
                {/* Button để mở/đóng dropdown */}
                <Listbox.Button className="relative w-full cursor-default rounded-2xl border border-slate-200 bg-white py-2.5 pl-4 pr-10 text-left outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 dark:border-slate-800 dark:bg-slate-950 sm:text-sm transition-all shadow-sm">
                  <span className="block truncate">
                    {/* Hiển thị danh mục được chọn, hoặc placeholder nếu chưa chọn */}
                    {form.categoryIds.length === 0 
                      ? <span className="text-slate-400">{t('dropdown.selectOption')}</span>
                      : formatCategoryList(categoryList.filter(c => form.categoryIds.includes(c.id)), language)
                    }
                  </span>
                  {/* Chevron icon */}
                  <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                    <ChevronUpDownIcon className="h-5 w-5 text-slate-400" aria-hidden="true" />
                  </span>
                </Listbox.Button>

                {/* Dropdown options với animation */}
                <Transition
                  as={Fragment}
                  leave="transition ease-in duration-100"
                  leaveFrom="opacity-100"
                  leaveTo="opacity-0"
                >
                  <Listbox.Options className="absolute z-50 mt-1 max-h-60 w-full overflow-auto rounded-xl bg-white py-1 text-base shadow-2xl ring-1 ring-black/5 focus:outline-none dark:bg-slate-900 sm:text-sm">
                    {/* Render từng category option */}
                    {categoryList.map((cat) => (
                      <Listbox.Option
                        key={cat.id}
                        className={({ active }) =>
                          `relative cursor-default select-none py-2.5 pl-10 pr-4 transition-colors ${
                            active ? 'bg-primary/10 text-primary' : 'text-slate-900 dark:text-slate-100'
                          }`
                        }
                        value={cat.id} // Giá trị là ID category
                      >
                        {({ selected }) => (
                          <>
                            {/* Category name text */}
                            <span className={`block truncate ${selected ? 'font-bold text-primary' : 'font-normal'}`}>
                              {formatCategoryName(cat.name, language)}
                            </span>
                            {/* Checkmark icon nếu category được chọn */}
                            {selected ? (
                              <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-primary">
                                <CheckIcon className="h-5 w-5" aria-hidden="true" />
                              </span>
                            ) : null}
                          </>
                        )}
                      </Listbox.Option>
                    ))}
                  </Listbox.Options>
                </Transition>
              </div>
            </Listbox>
            
            {/* ===== CREATE NEW CATEGORY INLINE ===== */}
            <div className="mt-3 flex gap-2">
              <input
                type="text"
                value={newCategoryName}
                onChange={(e) => setNewCategoryName(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleCreateCategory()}
                placeholder={t('bookTable.newCategoryPlaceholder') || 'Tên danh mục mới...'}
                className="flex-1 rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-sm outline-none focus:border-primary dark:border-slate-700 dark:bg-slate-950"
              />
              <button
                type="button"
                onClick={handleCreateCategory}
                disabled={!newCategoryName.trim() || creatingCategory}
                className="rounded-xl bg-primary px-3 py-1.5 text-xs font-bold text-white disabled:opacity-40 hover:opacity-90 transition-opacity"
              >
                {creatingCategory ? '...' : (t('bookTable.addCategory') || '+ Thêm')}
              </button>
            </div>

            {/* ===== SELECTED TAGS DISPLAY ===== */}
            {/* Hiển thị các category được chọn dưới dạng tag */}
            {form.categoryIds.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-2">
                {categoryList
                  .filter(c => form.categoryIds.includes(c.id)) // Lấy những category trong form.categoryIds
                  .map(cat => (
                    <span 
                      key={cat.id} 
                      className="inline-flex items-center gap-1.5 rounded-full bg-primary/10 px-3 py-1 text-xs font-bold text-primary animate-in fade-in zoom-in duration-200"
                    >
                      {/* Category name */}
                      {formatCategoryName(cat.name, language)}
                      {/* Nút X để xóa category khỏi form */}
                      <button
                        type="button"
                        onClick={() => setForm(prev => ({ 
                          ...prev, 
                          categoryIds: prev.categoryIds.filter(id => id !== cat.id) // Loại bỏ ID này khỏi mảng
                        }))}
                        className="hover:text-primary-focus p-0.5 rounded-full hover:bg-primary/20 transition-colors"
                      >
                        <CloseIcon className="h-3 w-3" />
                      </button>
                    </span>
                  ))
                }
              </div>
            )}
          </div>

          {/* INPUT: ISBN */}
          <Input label={t('book.label.isbn')} value={form.isbn} onChange={(e) => setForm((prev) => ({ ...prev, isbn: e.target.value }))} />
          
          {/* INPUT: Tổng số lượng sách */}
          <Input label={t('bookTable.totalQuantity')} type="number" min="1" value={form.totalQuantity} onChange={(e) => setForm((prev) => ({ ...prev, totalQuantity: e.target.value }))} />
          
          {/* BOOK COVER UPLOAD - chỉ hiển thị khi edit sách (có selected?.id) */}
          {selected && (
            <div className="pt-2 border-t border-slate-100 dark:border-slate-800">
              <BookCoverUpload 
                bookId={selected.id} 
                currentCoverUrl={selected.coverImageUrl} // Backend cung cấp full URL hoặc null
                onCoverUpdate={(url) => {
                   // Cập nhật lại books list với cover URL mới để reload UI
                   setBooks(books.map(b => b.id === selected.id ? { ...b, coverImageUrl: url } : b));
                }}
              />
            </div>
          )}

          {/* Hiển thị lỗi validation của form nếu có */}
          {formError && <p className="text-sm text-rose-500">{formError}</p>}
          
          {/* ACTION BUTTONS */}
          <div className="flex justify-end gap-3">
            {/* Nút Cancel - đóng modal */}
            <Button variant="secondary" onClick={closeModal}>{t('common.cancel')}</Button>
            {/* Nút Save - lưu sách (tạo mới hoặc update) */}
            <Button onClick={handleSave}><BookOpenIcon className="h-4 w-4" />{t('bookTable.saveBook')}</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default BookTable;
