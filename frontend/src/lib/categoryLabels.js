const CATEGORY_TRANSLATIONS = {
  vi: {
    Programming: 'Lập trình',
    'Self-help': 'Phát triển bản thân',
    Systems: 'Hệ thống',
    Productivity: 'Năng suất',
    Business: 'Kinh doanh',
    Science: 'Khoa học',
    Technology: 'Công nghệ',
    History: 'Lịch sử',
    Romance: 'Lãng mạn',
    Fantasy: 'Giả tưởng',
    Fiction: 'Tiểu thuyết',
    'Non-fiction': 'Phi hư cấu',
    Education: 'Giáo dục',
    Finance: 'Tài chính',
    Health: 'Sức khỏe',
    Art: 'Nghệ thuật',
    Children: 'Thiếu nhi',
    Literature: 'Văn học',
    Management: 'Quản lý',
    Uncategorized: 'Chưa phân loại',
    General: 'Chung',
    'Chưa phân loại': 'Chưa phân loại',
    'Lập trình': 'Lập trình',
    'Phát triển bản thân': 'Phát triển bản thân',
    'Hệ thống': 'Hệ thống',
    'Năng suất': 'Năng suất',
    'Kinh doanh': 'Kinh doanh',
    'Khoa học': 'Khoa học',
    'Công nghệ': 'Công nghệ',
    'Lịch sử': 'Lịch sử',
    'Lãng mạn': 'Lãng mạn',
    'Giả tưởng': 'Giả tưởng',
    'Tiểu thuyết': 'Tiểu thuyết',
    'Phi hư cấu': 'Phi hư cấu',
    'Giáo dục': 'Giáo dục',
    'Tài chính': 'Tài chính',
    'Sức khỏe': 'Sức khỏe',
    'Nghệ thuật': 'Nghệ thuật',
    'Thiếu nhi': 'Thiếu nhi',
    'Văn học': 'Văn học',
    'Quản lý': 'Quản lý',
    'Chung': 'Chung',
  },
  en: {
    Programming: 'Programming',
    'Self-help': 'Self-help',
    Systems: 'Systems',
    Productivity: 'Productivity',
    Business: 'Business',
    Science: 'Science',
    Technology: 'Technology',
    History: 'History',
    Romance: 'Romance',
    Fantasy: 'Fantasy',
    Fiction: 'Fiction',
    'Non-fiction': 'Non-fiction',
    Education: 'Education',
    Finance: 'Finance',
    Health: 'Health',
    Art: 'Art',
    Children: 'Children',
    Literature: 'Literature',
    Management: 'Management',
    Uncategorized: 'Uncategorized',
    General: 'General',
    'Lập trình': 'Programming',
    'Phát triển bản thân': 'Self-help',
    'Hệ thống': 'Systems',
    'Năng suất': 'Productivity',
    'Kinh doanh': 'Business',
    'Khoa học': 'Science',
    'Công nghệ': 'Technology',
    'Lịch sử': 'History',
    'Lãng mạn': 'Romance',
    'Giả tưởng': 'Fantasy',
    'Tiểu thuyết': 'Fiction',
    'Phi hư cấu': 'Non-fiction',
    'Giáo dục': 'Education',
    'Tài chính': 'Finance',
    'Sức khỏe': 'Health',
    'Nghệ thuật': 'Art',
    'Thiếu nhi': 'Children',
    'Văn học': 'Literature',
    'Quản lý': 'Management',
    'Chung': 'General',
    'Chưa phân loại': 'Uncategorized',
  },
};

const normalizeCategoryName = (value) => String(value ?? '').trim();

export const formatCategoryName = (value, language = 'vi') => {
  const name = normalizeCategoryName(value);
  if (!name) return '';

  return CATEGORY_TRANSLATIONS[language]?.[name] || name;
};

export const formatCategoryList = (value, language = 'vi') => {
  if (Array.isArray(value)) {
    return value
      .map((item) => formatCategoryName(typeof item === 'string' ? item : item?.name, language))
      .filter(Boolean)
      .join(', ');
  }

  return String(value ?? '')
    .split(',')
    .map((item) => formatCategoryName(item, language))
    .filter(Boolean)
    .join(', ');
};
