import React, { useState, useRef, useEffect } from 'react';
import { Upload, Link as LinkIcon, Trash2, Image as ImageIcon, AlertCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function BookCoverUpload({ bookId, currentCoverUrl, onCoverUpdate }) {
  const { user, token } = useAuth();
  const [coverUrl, setCoverUrl] = useState(currentCoverUrl);
  const [isUploading, setIsUploading] = useState(false);
  const [urlInput, setUrlInput] = useState('');
  const [showUrlInput, setShowUrlInput] = useState(false);
  const [error, setError] = useState('');
  const fileInputRef = useRef(null);
  const [isDragOver, setIsDragOver] = useState(false);

  // Sync state từ bên ngoài nếu currentCoverUrl thay đổi
  useEffect(() => {
    setCoverUrl(currentCoverUrl);
  }, [currentCoverUrl]);

  // Phân quyền hiển thị TRÊN VIEW
  // Nếu là GUEST HOẶC USER (role == 'ROLE_USER') -> Chỉ trả về view tĩnh, không hiện upload UI.
  if (!user || user.role === 'ROLE_USER' || user.role === 'ROLE_GUEST') {
    return (
      <div className="w-[160px] h-[220px] bg-gray-100 rounded-md shadow-md border 2 overflow-hidden flex items-center justify-center">
        {coverUrl ? (
          <img src={coverUrl} alt="Book Cover" className="w-full h-full object-cover" />
        ) : (
          <ImageIcon className="w-12 h-12 text-gray-400" />
        )}
      </div>
    );
  }

  // --- Logic cho LIBRARIAN và ADMIN (Được Edit) ---

  const validateFile = (file) => {
    setError('');
    if (!file) return false;
    
    // Kiểm tra ext / type
    const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      setError('Chỉ hỗ trợ JPG, PNG, WEBP.');
      return false;
    }
    
    // Dung lượng tối đa 2MB = 2 * 1024 * 1024 bytes
    if (file.size > 2 * 1024 * 1024) {
      setError('Dung lượng tối đa 2MB.');
      return false;
    }
    
    return true;
  };

  const handleFileUpload = async (file) => {
    if (!validateFile(file)) return;

    setIsUploading(true);
    const formData = new FormData();
    formData.append('cover_image', file);

    try {
      const res = await fetch(`/api/v1/books/${bookId}/cover`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData
      });
      
      if (!res.ok) {
        let errorMsg = `Upload failed with status ${res.status}`;
        try {
          const errorData = await res.json();
          errorMsg = errorData.message || errorMsg;
        } catch (e) {
          // If not JSON, ignore
        }
        throw new Error(errorMsg);
      }

      const data = await res.json();
      if (data.success) {
        setCoverUrl(data.coverUrl);
        // Bắn event/callback cập nhật state cha mà không cần reload
        if (onCoverUpdate) onCoverUpdate(data.coverUrl);
        
        // Hoặc Global Event Bus:
        window.dispatchEvent(new CustomEvent('bookCoverUpdated', { 
            detail: { bookId, coverUrl: data.coverUrl } 
        }));
      }
    } catch (err) {
      setError(err.message || 'Lỗi khi upload.');
    } finally {
      setIsUploading(false);
    }
  };

  const handleUrlUpload = async () => {
    if (!urlInput.trim()) return;
    setError('');
    setIsUploading(true);
    
    try {
      const res = await fetch(`/api/v1/books/${bookId}/cover-from-url`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ url: urlInput })
      });
      
      if (!res.ok) {
        let errorMsg = `Download failed with status ${res.status}`;
        try {
          const errorData = await res.json();
          errorMsg = errorData.message || errorMsg;
        } catch (e) { }
        throw new Error(errorMsg);
      }

      const data = await res.json();
      if (data.success) {
        setCoverUrl(data.coverUrl);
        setShowUrlInput(false);
        setUrlInput('');
        if (onCoverUpdate) onCoverUpdate(data.coverUrl);
        window.dispatchEvent(new CustomEvent('bookCoverUpdated', { detail: { bookId, coverUrl: data.coverUrl } }));
      }
    } catch (err) {
      setError(err.message || 'Lỗi khi download ảnh từ URL.');
    } finally {
      setIsUploading(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa ảnh bìa này không?")) return;
    setIsUploading(true);
    try {
      const res = await fetch(`/api/v1/books/${bookId}/cover`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!res.ok) throw new Error('Delete failed');

      setCoverUrl(null);
      if (onCoverUpdate) onCoverUpdate(null);
      window.dispatchEvent(new CustomEvent('bookCoverUpdated', { detail: { bookId, coverUrl: null } }));
    } catch (err) {
      setError('Lỗi khi xóa ảnh bìa.');
    } finally {
      setIsUploading(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragOver(false);
    const file = e.dataTransfer.files[0];
    handleFileUpload(file);
  };
  
  return (
    <div className="flex gap-6 items-start">
      {/* Cột Preview (Chứa Ảnh/Placeholder) */}
      <div className="relative shrink-0 w-[160px] h-[220px] bg-gray-100 rounded-md shadow-md border 2 overflow-hidden flex flex-col items-center justify-center">
        {isUploading && (
          <div className="absolute inset-0 bg-white/70 z-10 flex items-center justify-center">
            <span className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></span>
          </div>
        )}
        
        {coverUrl ? (
          <img src={coverUrl} alt="Cover Preview" className="w-full h-full object-cover" />
        ) : (
          <div className="text-gray-400 flex flex-col items-center justify-center">
            <ImageIcon className="w-12 h-12 mb-2" />
            <span className="text-xs text-center font-medium">Chưa có ảnh bìa</span>
          </div>
        )}
      </div>

      {/* Cột Điều Khiển Upload */}
      <div className="flex flex-col flex-1 max-w-sm">
        <label className="block text-sm font-medium text-gray-700 mb-2">Quản Lý Ảnh Bìa</label>
        
        {error && <div className="mb-2 text-sm text-red-600 bg-red-50 p-2 rounded">{error}</div>}

        <div 
          onDragOver={(e) => { e.preventDefault(); setIsDragOver(true); }}
          onDragLeave={() => setIsDragOver(false)}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
          className={`border-2 border-dashed rounded-lg p-4 flex flex-col items-center justify-center cursor-pointer transition-colors ${isDragOver ? 'border-indigo-500 bg-indigo-50' : 'border-gray-300 hover:bg-gray-50'}`}
        >
          <Upload className="w-6 h-6 text-indigo-600 mb-2" />
          <p className="text-sm font-medium text-gray-700">Kéo thả hoặc Nhấn chọn file</p>
          <p className="text-xs text-gray-500 mt-1">JPG, PNG, WEBP lên tới 2MB</p>
          <input 
            type="file" 
            ref={fileInputRef}
            className="hidden" 
            accept="image/png, image/jpeg, image/webp"
            onChange={(e) => handleFileUpload(e.target.files[0])}
          />
        </div>

        <div className="mt-3 flex items-center justify-between gap-2">
          {!showUrlInput ? (
            <button 
              type="button"
              onClick={() => setShowUrlInput(true)}
              className="text-sm text-indigo-600 hover:text-indigo-700 flex items-center font-medium transition-colors"
            >
              <LinkIcon className="w-4 h-4 mr-1" /> Nhập URL tải xuống
            </button>
          ) : (
             <div className="flex w-full gap-2 relative">
               <input 
                 autoFocus
                 type="text" 
                 placeholder="https://..." 
                 className="flex-1 text-sm border-gray-300 rounded-md shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                 value={urlInput}
                 onChange={e => setUrlInput(e.target.value)}
                 onKeyDown={(e) => e.key === 'Enter' && handleUrlUpload()}
               />
               <button 
                type="button" 
                onClick={handleUrlUpload}
                disabled={isUploading || !urlInput.trim()}
                className="bg-indigo-600 text-white text-sm px-3 py-1.5 rounded-md hover:bg-indigo-700 disabled:opacity-50"
               >
                 Tải
               </button>
               <button 
                type="button" 
                onClick={() => setShowUrlInput(false)}
                className="text-gray-400 hover:text-gray-600 px-1 absolute -top-5 right-0 text-xs"
               >
                 Hủy
               </button>
             </div>
          )}

          {coverUrl && (
            <button 
              type="button"
              onClick={handleDelete}
              className="text-sm text-red-600 hover:text-red-700 flex items-center font-medium transition-colors ml-auto"
            >
              <Trash2 className="w-4 h-4 mr-1" /> Xóa ảnh
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
