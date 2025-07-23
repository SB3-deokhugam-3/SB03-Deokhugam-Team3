-- 테스트용 사용자 데이터 (is_deleted 추가)
INSERT INTO users (id, email, nickname, password, created_at, updated_at, is_deleted) VALUES
                                                                                          ('550e8400-e29b-41d4-a716-446655440001', 'poweruser1@example.com', '파워유저1', 'password', NOW(), NOW(), false),
                                                                                          ('550e8400-e29b-41d4-a716-446655440002', 'poweruser2@example.com', '파워유저2', 'password', NOW(), NOW(), false),
                                                                                          ('550e8400-e29b-41d4-a716-446655440003', 'poweruser3@example.com', '파워유저3', 'password', NOW(), NOW(), false),
                                                                                          ('550e8400-e29b-41d4-a716-446655440004', 'poweruser4@example.com', '파워유저4', 'password', NOW(), NOW(), false),
                                                                                          ('550e8400-e29b-41d4-a716-446655440005', 'poweruser5@example.com', '파워유저5', 'password', NOW(), NOW(), false);

-- 테스트용 도서 데이터 (이미 정상)
INSERT INTO books (id, title, author, description, publisher, published_date, isbn, thumbnail_url, rating, review_count, created_at, updated_at, is_deleted) VALUES
                                                                                                                                                                 ('660e8400-e29b-41d4-a716-446655440001', '자바 완전정복', '김자바', '자바 프로그래밍 입문서', '코딩출판사', '2024-01-01', '9788123456789', 'http://example.com/java.jpg', 4.5, 10, NOW(), NOW(), false),
                                                                                                                                                                 ('660e8400-e29b-41d4-a716-446655440002', '스프링 부트 실전', '박스프링', '스프링 부트 실무 가이드', '개발출판사', '2024-02-01', '9788123456790', 'http://example.com/spring.jpg', 4.8, 15, NOW(), NOW(), false);

-- 테스트용 리뷰 데이터 (이제 사용자가 존재하므로 정상 실행)
INSERT INTO reviews (id, user_id, book_id, rating, content, like_count, comment_count, created_at, updated_at, is_deleted) VALUES
                                                                                                                               ('770e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440001', 5, '정말 좋은 책입니다!', 10, 5, NOW(), NOW(), false),
                                                                                                                               ('770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440001', 4, '도움이 많이 되었어요', 8, 3, NOW(), NOW(), false),
                                                                                                                               ('770e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440002', 5, '최고의 스프링 책!', 15, 7, NOW(), NOW(), false);

-- 테스트용 파워 유저 데이터 (updated_at 추가)
INSERT INTO power_users (id, user_id, period, rank, score, review_score_sum, like_count, comment_count, created_at) VALUES
                                                                                                                                    ('880e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'ALL_TIME', 1, 95.5, 85.0, 25, 15, NOW()),
                                                                                                                                    ('880e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', 'ALL_TIME', 2, 88.2, 78.0, 20, 12, NOW()),
                                                                                                                                    ('880e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', 'ALL_TIME', 3, 82.1, 72.5, 18, 10, NOW()),
                                                                                                                                    ('880e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440004', 'DAILY', 1, 75.8, 65.0, 15, 8, NOW()),
                                                                                                                                    ('880e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440005', 'WEEKLY', 1, 68.5, 58.0, 12, 6, NOW());
