UPDATE users SET password = '$2a$10$SJKM3Mo4/h5ZDrDUWTL9nOQQ.iYf3xtVFLgir1QXB/9sF0o7h3jNm' WHERE email = 'abc123@gmail.com';
SELECT id, email, password FROM users WHERE email='abc123@gmail.com';
