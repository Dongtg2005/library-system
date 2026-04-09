export const dashboardStats = [
  { label: 'Total Users', value: '2,148', delta: '+12.5%', tone: 'primary' },
  { label: 'Total Books', value: '8,420', delta: '+6.8%', tone: 'amber' },
  { label: 'Borrowed Books', value: '1,284', delta: '+18.2%', tone: 'cyan' },
  { label: 'Overdue Fees', value: '$4,860', delta: '+3.1%', tone: 'rose' },
];

export const userRows = [
  { id: 1, name: 'Nguyễn Hoàng Phụng', email: 'phung@example.com', role: 'ADMIN', status: 'Active', lastLogin: '2m ago' },
  { id: 2, name: 'Trần Minh Khoa', email: 'khoa@example.com', role: 'LIBRARIAN', status: 'Active', lastLogin: '20m ago' },
  { id: 3, name: 'Lê Thị Hoa', email: 'hoa@example.com', role: 'USER', status: 'Suspended', lastLogin: '3h ago' },
  { id: 4, name: 'Phạm Văn Long', email: 'long@example.com', role: 'USER', status: 'Active', lastLogin: '1d ago' },
  { id: 5, name: 'Ngô Thùy Dương', email: 'duong@example.com', role: 'USER', status: 'Pending', lastLogin: '5d ago' },
];

export const bookRows = [
  { id: 1, title: 'Clean Code', author: 'Robert C. Martin', category: 'Programming', isbn: '9780132350884', stock: 12, available: 9, status: 'Available' },
  { id: 2, title: 'Atomic Habits', author: 'James Clear', category: 'Self-help', isbn: '9780735211292', stock: 8, available: 2, status: 'Low stock' },
  { id: 3, title: 'Designing Data-Intensive Applications', author: 'Martin Kleppmann', category: 'Systems', isbn: '9781449373320', stock: 5, available: 0, status: 'Out of stock' },
  { id: 4, title: 'The Pragmatic Programmer', author: 'Andrew Hunt', category: 'Programming', isbn: '9780201616224', stock: 15, available: 11, status: 'Available' },
  { id: 5, title: 'Deep Work', author: 'Cal Newport', category: 'Productivity', isbn: '9781455586691', stock: 7, available: 5, status: 'Available' },
];

export const borrowRows = [
  { id: 1, user: 'Nguyễn Hoàng Phụng', book: 'Clean Code', borrowDate: '2026-04-01', dueDate: '2026-04-08', status: 'Overdue', fine: '$12.00' },
  { id: 2, user: 'Trần Minh Khoa', book: 'Atomic Habits', borrowDate: '2026-04-03', dueDate: '2026-04-17', status: 'Borrowed', fine: '$0.00' },
  { id: 3, user: 'Lê Thị Hoa', book: 'Deep Work', borrowDate: '2026-03-28', dueDate: '2026-04-11', status: 'Returned', fine: '$0.00' },
  { id: 4, user: 'Phạm Văn Long', book: 'The Pragmatic Programmer', borrowDate: '2026-04-06', dueDate: '2026-04-20', status: 'Borrowed', fine: '$0.00' },
];

export const chartData = [
  { name: 'Mon', users: 120, books: 300, borrows: 80 },
  { name: 'Tue', users: 145, books: 280, borrows: 92 },
  { name: 'Wed', users: 180, books: 340, borrows: 110 },
  { name: 'Thu', users: 160, books: 320, borrows: 105 },
  { name: 'Fri', users: 210, books: 370, borrows: 125 },
  { name: 'Sat', users: 190, books: 355, borrows: 117 },
  { name: 'Sun', users: 170, books: 330, borrows: 100 },
];
