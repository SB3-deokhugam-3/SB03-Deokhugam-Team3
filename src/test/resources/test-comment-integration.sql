DELETE
FROM users;
INSERT INTO users (id, email, nickname, password, is_deleted, created_at, updated_at)
VALUES ('36404724-4603-4cf4-8a8c-ebff46deb51b', 'user1@example.com', 'user1', 'hashed_password',
        FALSE, '2025-01-02 00:00:00', NULL);
INSERT INTO users (id, email, nickname, password, is_deleted, created_at, updated_at)
VALUES ('04e8e411-dd9c-451e-b03e-b393557b283e', 'user2@example.com', 'user2', 'hashed_password',
        FALSE, '2025-01-03 00:00:00', NULL);
INSERT INTO users (id, email, nickname, password, is_deleted, created_at, updated_at)
VALUES ('f3787b03-a74a-4593-97d5-a8316d2bef72', 'user3@example.com', 'user3', 'hashed_password',
        FALSE, '2025-01-04 00:00:00', NULL);
INSERT INTO users (id, email, nickname, password, is_deleted, created_at, updated_at)
VALUES ('5efac318-3702-46a9-a6e6-890e259660d7', 'user4@example.com', 'user4', 'hashed_password',
        FALSE, '2025-01-05 00:00:00', NULL);
INSERT INTO users (id, email, nickname, password, is_deleted, created_at, updated_at)
VALUES ('4bff24b1-85a5-41f2-9f09-09fa98bec488', 'user5@example.com', 'user5', 'hashed_password',
        FALSE, '2025-01-06 00:00:00', NULL);

DELETE
FROM books;
INSERT INTO books (id, title, thumbnail_url, author, description, publisher, published_date, rating,
                   review_count, isbn, is_deleted, created_at, updated_at)
VALUES ('f6601c1d-c9b9-4ae1-a7aa-b4345921f4ca', 'book1', 'https://placehold.co/600x400?text=book',
        'author1', 'description of book1', 'publisher1', '2025-01-01', 2.2826, 0, '1000000000001',
        FALSE, '2025-01-02 00:00:00', NULL);
INSERT INTO books (id, title, thumbnail_url, author, description, publisher, published_date, rating,
                   review_count, isbn, is_deleted, created_at, updated_at)
VALUES ('17fede2c-5df9-4655-999c-03829265850e', 'book2', 'https://placehold.co/600x400?text=book',
        'author2', 'description of book2', 'publisher2', '2025-01-01', 0.5491, 0, '1000000000002',
        FALSE, '2025-01-03 00:00:00', NULL);
INSERT INTO books (id, title, thumbnail_url, author, description, publisher, published_date, rating,
                   review_count, isbn, is_deleted, created_at, updated_at)
VALUES ('f9f50fe8-86b4-4e9b-8684-2f54393af572', 'book3', 'https://placehold.co/600x400?text=book',
        'author3', 'description of book3', 'publisher3', '2025-01-01', 1.2040, 0, '1000000000003',
        FALSE, '2025-01-04 00:00:00', NULL);
INSERT INTO books (id, title, thumbnail_url, author, description, publisher, published_date, rating,
                   review_count, isbn, is_deleted, created_at, updated_at)
VALUES ('63245a07-e3d8-4f92-b4d2-9cbe08ae8063', 'book4', 'https://placehold.co/600x400?text=book',
        'author4', 'description of book4', 'publisher4', '2025-01-01', 3.4218, 0, '1000000000004',
        FALSE, '2025-01-05 00:00:00', NULL);
INSERT INTO books (id, title, thumbnail_url, author, description, publisher, published_date, rating,
                   review_count, isbn, is_deleted, created_at, updated_at)
VALUES ('ea166b1e-7094-4fc5-af39-db7873fd2471', 'book5', 'https://placehold.co/600x400?text=book',
        'author5', 'description of book5', 'publisher5', '2025-01-01', 4.9223, 0, '1000000000005',
        FALSE, '2025-01-06 00:00:00', NULL);

DELETE
FROM reviews;
INSERT INTO reviews (id, book_id, user_id, rating, content, like_count, comment_count, is_deleted,
                     created_at, updated_at)
VALUES ('cea1a965-2817-4431-90e3-e5701c70d43d', 'f6601c1d-c9b9-4ae1-a7aa-b4345921f4ca',
        '36404724-4603-4cf4-8a8c-ebff46deb51b', 2.0000, 'review1', 80, 16, FALSE,
        '2025-01-02 00:00:00', NULL);
INSERT INTO reviews (id, book_id, user_id, rating, content, like_count, comment_count, is_deleted,
                     created_at, updated_at)
VALUES ('044458f4-72a3-49aa-96f8-1a5160f444e2', '17fede2c-5df9-4655-999c-03829265850e',
        '04e8e411-dd9c-451e-b03e-b393557b283e', 3.0000, 'review2', 38, 44, FALSE,
        '2025-01-03 00:00:00', NULL);
INSERT INTO reviews (id, book_id, user_id, rating, content, like_count, comment_count, is_deleted,
                     created_at, updated_at)
VALUES ('b8f772ec-3ad4-49fd-a846-14dda80f2fb8', 'f9f50fe8-86b4-4e9b-8684-2f54393af572',
        'f3787b03-a74a-4593-97d5-a8316d2bef72', 4.0000, 'review3', 32, 33, FALSE,
        '2025-01-04 00:00:00', NULL);
INSERT INTO reviews (id, book_id, user_id, rating, content, like_count, comment_count, is_deleted,
                     created_at, updated_at)
VALUES ('236ed902-90d8-42b7-8df9-af50fe0f3ff5', '63245a07-e3d8-4f92-b4d2-9cbe08ae8063',
        '5efac318-3702-46a9-a6e6-890e259660d7', 5.0000, 'review4', 8, 26, FALSE,
        '2025-01-05 00:00:00', NULL);
INSERT INTO reviews (id, book_id, user_id, rating, content, like_count, comment_count, is_deleted,
                     created_at, updated_at)
VALUES ('a972a5ad-863a-4b03-b41d-c5360fdd4f6d', 'ea166b1e-7094-4fc5-af39-db7873fd2471',
        '4bff24b1-85a5-41f2-9f09-09fa98bec488', 1.0000, 'review5', 84, 50, FALSE,
        '2025-01-06 00:00:00', NULL);

DELETE
FROM comments;
INSERT INTO comments (id, user_id, review_id, content, is_deleted, created_at, updated_at)
VALUES ('01bd234c-d175-41b1-bd89-84995596b6f2', '36404724-4603-4cf4-8a8c-ebff46deb51b',
        'cea1a965-2817-4431-90e3-e5701c70d43d', 'comment1', FALSE, '2025-01-02 00:00:00', NULL);
INSERT INTO comments (id, user_id, review_id, content, is_deleted, created_at, updated_at)
VALUES ('2f499b88-fd3b-4fb7-9a33-f0976d8c76b8', '04e8e411-dd9c-451e-b03e-b393557b283e',
        '044458f4-72a3-49aa-96f8-1a5160f444e2', 'comment2', TRUE, '2025-01-03 00:00:00', NULL);
INSERT INTO comments (id, user_id, review_id, content, is_deleted, created_at, updated_at)
VALUES ('c2c1cf27-2d21-4b87-8a89-aeffb19c8dae', 'f3787b03-a74a-4593-97d5-a8316d2bef72',
        'b8f772ec-3ad4-49fd-a846-14dda80f2fb8', 'comment3', FALSE, '2025-01-04 00:00:00', NULL);
INSERT INTO comments (id, user_id, review_id, content, is_deleted, created_at, updated_at)
VALUES ('25365949-eaf3-47e6-bca8-1faf76c7a21b', '5efac318-3702-46a9-a6e6-890e259660d7',
        'cea1a965-2817-4431-90e3-e5701c70d43d', 'comment4', FALSE, '2025-01-05 00:00:00', NULL);
INSERT INTO comments (id, user_id, review_id, content, is_deleted, created_at, updated_at)
VALUES ('8fe37992-017c-4871-be52-0ee635aed4f2', '4bff24b1-85a5-41f2-9f09-09fa98bec488',
        'cea1a965-2817-4431-90e3-e5701c70d43d', 'comment5', FALSE, '2025-01-06 00:00:00', NULL);

DELETE
FROM review_likes;
INSERT INTO review_likes (id, review_id, user_id, created_at)
VALUES ('6d3785a0-32b0-41d5-be6e-f081bb2d74f5', 'cea1a965-2817-4431-90e3-e5701c70d43d',
        '04e8e411-dd9c-451e-b03e-b393557b283e', '2025-01-02 00:00:00');
INSERT INTO review_likes (id, review_id, user_id, created_at)
VALUES ('7c6b5dae-9e50-408d-8ecb-efd9db176bd8', '044458f4-72a3-49aa-96f8-1a5160f444e2',
        '04e8e411-dd9c-451e-b03e-b393557b283e', '2025-01-03 00:00:00');
INSERT INTO review_likes (id, review_id, user_id, created_at)
VALUES ('2529eedd-941a-4708-a37a-35c140149db0', 'b8f772ec-3ad4-49fd-a846-14dda80f2fb8',
        'f3787b03-a74a-4593-97d5-a8316d2bef72', '2025-01-04 00:00:00');
INSERT INTO review_likes (id, review_id, user_id, created_at)
VALUES ('61eab2b4-6a5f-46f5-8dd2-827a0a2c2efb', '236ed902-90d8-42b7-8df9-af50fe0f3ff5',
        '4bff24b1-85a5-41f2-9f09-09fa98bec488', '2025-01-05 00:00:00');
INSERT INTO review_likes (id, review_id, user_id, created_at)
VALUES ('c5cd13f8-111b-4213-aecf-bfc96a92f121', 'a972a5ad-863a-4b03-b41d-c5360fdd4f6d',
        '4bff24b1-85a5-41f2-9f09-09fa98bec488', '2025-01-06 00:00:00');