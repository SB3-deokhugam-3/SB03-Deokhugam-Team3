-- 유저 삽입
INSERT INTO users (id, created_at, updated_at, email, nickname, password, is_deleted)
VALUES
    ('user-1', now(), now(), 'user1@example.com', 'UserOne', 'encoded_pw', false);

-- 책 삽입
INSERT INTO books (id, created_at, updated_at, title, thumbnail_url, author, description, publisher, published_date, rating, review_count, isbn, is_deleted)
VALUES
    ('book-1', now(), now(), 'Book One', 'url1', 'Author One', 'Desc1', 'Publisher1', '2023-01-01', 4.0, 2, '1234567890123', false),
    ('book-2', now(), now(), 'Book Two', 'url2', 'Author Two', 'Desc2', 'Publisher2', '2023-01-01', 4.5, 1, '1234567890124', false);

-- 날짜 기준 설정
-- today 기준: 2025-07-20
-- DAILY 범위: 2025-07-19 00:00:00 ~ 2025-07-20 00:00:00
-- WEEKLY 범위: 2025-07-14 ~ 2025-07-21
-- MONTHLY 범위: 2025-07-01 ~ 2025-08-01
-- ALL_TIME: 전부 포함

-- 리뷰 삽입 (DAILY 범위)
INSERT INTO reviews (id, created_at, updated_at, book_id, user_id, rating, content, like_count, comment_count, is_deleted)
VALUES
    ('review-daily-1', '2025-07-19T10:00:00', now(), 'book-1', 'user-1', 5, '좋아요', 10, 2, false),
    ('review-daily-2', '2025-07-19T15:30:00', now(), 'book-2', 'user-1', 4, '괜찮아요', 5, 1, false);

-- 리뷰 삽입 (WEEKLY 범위)
INSERT INTO reviews (id, created_at, updated_at, book_id, user_id, rating, content, like_count, comment_count, is_deleted)
VALUES
    ('review-weekly-1', '2025-07-16T13:00:00', now(), 'book-1', 'user-1', 3, '보통이에요', 2, 0, false);

-- 리뷰 삽입 (MONTHLY 범위)
INSERT INTO reviews (id, created_at, updated_at, book_id, user_id, rating, content, like_count, comment_count, is_deleted)
VALUES
    ('review-monthly-1', '2025-07-05T08:00:00', now(), 'book-2', 'user-1', 5, '최고예요', 8, 3, false);

-- 리뷰 삽입 (ALL_TIME 범위만 포함)
INSERT INTO reviews (id, created_at, updated_at, book_id, user_id, rating, content, like_count, comment_count, is_deleted)
VALUES
    ('review-alltime-1', '2024-11-03T12:00:00', now(), 'book-1', 'user-1', 4, '예전 리뷰', 1, 0, false);

-- 삭제된 리뷰 (집계 대상에서 제외됨)
INSERT INTO reviews (id, created_at, updated_at, book_id, user_id, rating, content, like_count, comment_count, is_deleted)
VALUES
    ('review-deleted', '2025-07-19T11:00:00', now(), 'book-1', 'user-1', 5, '삭제된 리뷰', 100, 5, true);
