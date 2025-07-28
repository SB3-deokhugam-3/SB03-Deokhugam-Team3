DELETE
FROM popular_book_rankings;
DELETE
FROM reviews;
DELETE
FROM books;
DELETE
FROM users;

-- 유저 삽입
INSERT INTO users (id, created_at, updated_at, email, nickname, password, is_deleted)
VALUES ('11111111-1111-1111-1111-111111111111', now(), now(), 'user1@example.com', 'UserOne',
        'encoded_pw', false),
       ('22222222-2222-2222-2222-222222222222', now(), now(), 'user2@example.com', 'UserTwo',
        'encoded_pw', false),
       ('33333333-3333-3333-3333-333333333333', now(), now(), 'user3@example.com', 'UserThree',
        'encoded_pw', false);

-- 책 삽입
INSERT INTO books (id, created_at, updated_at, title, thumbnail_url, author, description, publisher,
                   published_date, rating, review_count, isbn, is_deleted)
VALUES ('33333333-3333-3333-3333-333333333234', now(), now(), 'Book One', 'url1', 'Author One',
        'Desc1', 'Publisher1', '2023-01-01', 4.0, 2, '9876543210333', false),
       ('55555555-5555-5555-5555-555555555555', now(), now(), 'Book Two', 'url2', 'Author Two',
        'Desc2', 'Publisher2', '2023-01-01', 4.5, 1, '9876543210222', false);

-- DAILY 범위 (2025-07-19 00:00:00 ~ 2025-07-20 00:00:00 KST)
INSERT INTO reviews (id, created_at, updated_at, book_id, user_id, rating, content, like_count,
                     comment_count, is_deleted)
VALUES ('12345678-1234-5678-7777-123456781234', '2025-07-18T10:00:00+09:00', now(),
        '33333333-3333-3333-3333-333333333234', '11111111-1111-1111-1111-111111111111', 5, '좋아요',
        10, 2, false),
       ('12323678-1234-5678-7777-123456781234', '2025-07-18T15:30:00+09:00', now(),
        '55555555-5555-5555-5555-555555555555', '22222222-2222-2222-2222-222222222222', 4, '괜찮아요',
        5, 1, false);

-- WEEKLY 범위 (2025-07-14 00:00:00 ~ 2025-07-20 00:00:00 KST)
INSERT INTO reviews (id, created_at, updated_at, book_id, user_id, rating, content, like_count,
                     comment_count, is_deleted)
VALUES ('12665678-1234-5678-7777-123456231234', '2025-07-16T13:00:00+09:00', now(),
        '33333333-3333-3333-3333-333333333234', '22222222-2222-2222-2222-222222222222', 3, '보통이에요',
        2, 0, false);

-- MONTHLY 범위 (2025-07-01 00:00:00 ~ 2025-08-01 00:00:00 KST)
INSERT INTO reviews (id, created_at, updated_at, book_id, user_id, rating, content, like_count,
                     comment_count, is_deleted)
VALUES ('12222678-2345-6789-7777-123456781234', '2025-07-05T08:00:00+09:00', now(),
        '55555555-5555-5555-5555-555555555555', '33333333-3333-3333-3333-333333333333', 5, '최고예요',
        8, 3, false);

-- ALL_TIME 범위 전용 (이전 연도)
INSERT INTO reviews (id, created_at, updated_at, book_id, user_id, rating, content, like_count,
                     comment_count, is_deleted)
VALUES ('12345678-1234-5678-7777-959493102934', '2024-11-03T12:00:00+09:00', now(),
        '33333333-3333-3333-3333-333333333234', '33333333-3333-3333-3333-333333333333', 4, '예전 리뷰',
        1, 0, false);

-- 삭제된 리뷰 (무시됨)
INSERT INTO reviews (id, created_at, updated_at, book_id, user_id, rating, content, like_count,
                     comment_count, is_deleted)
VALUES ('12345678-1234-9999-7777-123456781234', '2025-07-19T11:00:00+09:00', now(),
        '55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', 5, '삭제된 리뷰',
        100, 5, true);
