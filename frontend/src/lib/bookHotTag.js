const toNumber = (value) => {
  const num = Number(value);
  return Number.isFinite(num) ? num : 0;
};

export const isHotBook = (book) => {
  const borrowedQuantity = toNumber(book?.borrowedQuantity);
  const totalQuantity = toNumber(book?.totalQuantity);
  const borrowRatio = totalQuantity > 0 ? borrowedQuantity / totalQuantity : 0;

  // HOT when borrow count is high, or borrow pressure is high vs stock.
  return borrowedQuantity >= 5 || (borrowedQuantity >= 3 && borrowRatio >= 0.5);
};

export const buildTopRankMap = (books = []) => {
  const rankMap = {};
  books.forEach((book, index) => {
    if (book?.id) {
      rankMap[String(book.id)] = index + 1;
    }
  });
  return rankMap;
};

export const getBookBadgeText = (book, topRankMap = {}) => {
  const rank = topRankMap[String(book?.id)];
  if (rank) return `TOP ${rank}`;
  if (isHotBook(book)) return 'HOT';
  return null;
};
