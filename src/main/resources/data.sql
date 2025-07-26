-- ============= 사용자 데이터 =============
INSERT INTO users (id, email, nickname, password, is_deleted, created_at, updated_at) VALUES
                                                                                          ('550e8400-e29b-41d4-a716-446655440001', 'user1@test.com', '독서왕김철수', 'password123', false, CURRENT_TIMESTAMP - INTERVAL '10' DAY, CURRENT_TIMESTAMP - INTERVAL '10' DAY),
                                                                                          ('550e8400-e29b-41d4-a716-446655440002', 'user2@test.com', '리뷰마스터이영희', 'password123', false, CURRENT_TIMESTAMP - INTERVAL '15' DAY, CURRENT_TIMESTAMP - INTERVAL '15' DAY),
                                                                                          ('550e8400-e29b-41d4-a716-446655440003', 'user3@test.com', '책벌레박민수', 'password123', false, CURRENT_TIMESTAMP - INTERVAL '20' DAY, CURRENT_TIMESTAMP - INTERVAL '20' DAY),
                                                                                          ('550e8400-e29b-41d4-a716-446655440004', 'user4@test.com', '평점킬러정수진', 'password123', false, CURRENT_TIMESTAMP - INTERVAL '25' DAY, CURRENT_TIMESTAMP - INTERVAL '25' DAY),
                                                                                          ('550e8400-e29b-41d4-a716-446655440005', 'user5@test.com', '댓글요정한지민', 'password123', false, CURRENT_TIMESTAMP - INTERVAL '30' DAY, CURRENT_TIMESTAMP - INTERVAL '30' DAY);

-- ============= 도서 데이터 =============
INSERT INTO books (id, title, author, description, publisher, published_date, isbn, thumbnail_url, review_count, rating, is_deleted, created_at, updated_at) VALUES
                                                                                                                                                                 ('660e8400-e29b-41d4-a716-446655440001', '클린 코드', '로버트 C. 마틴', '애자일 소프트웨어 장인 정신', '인사이트', '2013-12-24', '9788966260959', 'https://example.com/clean-code.jpg', 0, 0.0, false, CURRENT_TIMESTAMP - INTERVAL '60' DAY, CURRENT_TIMESTAMP - INTERVAL '60' DAY),
                                                                                                                                                                 ('660e8400-e29b-41d4-a716-446655440002', '이펙티브 자바', '조슈아 블로크', '자바 플랫폼 모범사례', '인사이트', '2018-10-30', '9788966262281', 'https://example.com/effective-java.jpg', 0, 0.0, false, CURRENT_TIMESTAMP - INTERVAL '60' DAY, CURRENT_TIMESTAMP - INTERVAL '60' DAY),
                                                                                                                                                                 ('660e8400-e29b-41d4-a716-446655440003', '스프링 부트와 AWS', '이동욱', '실습으로 배우는 스프링', '프리렉', '2019-12-24', '9788965402602', 'https://example.com/spring-boot.jpg', 0, 0.0, false, CURRENT_TIMESTAMP - INTERVAL '60' DAY, CURRENT_TIMESTAMP - INTERVAL '60' DAY),
                                                                                                                                                                 ('660e8400-e29b-41d4-a716-446655440004', '해리포터와 마법사의 돌', 'J.K. 롤링', '마법의 세계로의 초대', '문학수첩', '1999-12-01', '9788983920683', 'https://example.com/harry-potter.jpg', 0, 0.0, false, CURRENT_TIMESTAMP - INTERVAL '60' DAY, CURRENT_TIMESTAMP - INTERVAL '60' DAY),
                                                                                                                                                                 ('660e8400-e29b-41d4-a716-446655440005', '1984', '조지 오웰', '디스토피아 소설의 걸작', '민음사', '2003-02-15', '9788937460777', 'https://example.com/1984.jpg', 0, 0.0, false, CURRENT_TIMESTAMP - INTERVAL '60' DAY, CURRENT_TIMESTAMP - INTERVAL '60' DAY);

-- ============= DAILY용 리뷰 데이터 (어제) =============
INSERT INTO reviews (id, content, rating, like_count, comment_count, is_deleted, user_id, book_id, created_at, updated_at) VALUES
-- 어제 활동 (DAILY 배치에서 집계됨)
('770e8400-e29b-41d4-a716-446655440101', '어제 클린 코드 읽었는데 정말 좋네요!', 5, 12, 5, false, '550e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY),
('770e8400-e29b-41d4-a716-446655440102', '해리포터 어제 다시 읽어도 재밌어요!', 5, 20, 8, false, '550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440004', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY);

-- ============= WEEKLY용 리뷰 데이터 (지난 3-6일) =============
INSERT INTO reviews (id, content, rating, like_count, comment_count, is_deleted, user_id, book_id, created_at, updated_at) VALUES
-- 3일 전 활동 (WEEKLY 배치에서 집계됨)
('770e8400-e29b-41d4-a716-446655440201', '이펙티브 자바 주간 베스트!', 5, 25, 10, false, '550e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '3' DAY),
('770e8400-e29b-41d4-a716-446655440202', '스프링 부트 실습했어요', 4, 15, 6, false, '550e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440003', CURRENT_TIMESTAMP - INTERVAL '5' DAY, CURRENT_TIMESTAMP - INTERVAL '5' DAY);

-- ============= MONTHLY용 리뷰 데이터 (지난 15-25일) =============
INSERT INTO reviews (id, content, rating, like_count, comment_count, is_deleted, user_id, book_id, created_at, updated_at) VALUES
-- 15일 전 활동 (MONTHLY 배치에서 집계됨)
('770e8400-e29b-41d4-a716-446655440301', '1984 월간 화제작! 무서워요', 5, 30, 15, false, '550e8400-e29b-41d4-a716-446655440005', '660e8400-e29b-41d4-a716-446655440005', CURRENT_TIMESTAMP - INTERVAL '15' DAY, CURRENT_TIMESTAMP - INTERVAL '15' DAY),
('770e8400-e29b-41d4-a716-446655440302', '클린 코드 월간 베스트', 5, 28, 12, false, '550e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP - INTERVAL '20' DAY, CURRENT_TIMESTAMP - INTERVAL '20' DAY);

-- ============= ALL_TIME용 리뷰 데이터 (오래 전) =============
INSERT INTO reviews (id, content, rating, like_count, comment_count, is_deleted, user_id, book_id, created_at, updated_at) VALUES
-- 60일 전 활동 (ALL_TIME에서 역대 1위)
('770e8400-e29b-41d4-a716-446655440401', '해리포터 역대 최고작!', 5, 50, 25, false, '550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440004', CURRENT_TIMESTAMP - INTERVAL '60' DAY, CURRENT_TIMESTAMP - INTERVAL '60' DAY),
('770e8400-e29b-41d4-a716-446655440402', '클린 코드 역대급 개발서', 5, 45, 20, false, '550e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP - INTERVAL '90' DAY, CURRENT_TIMESTAMP - INTERVAL '90' DAY);

-- ============= 댓글 데이터 =============
INSERT INTO comments (id, content, is_deleted, user_id, review_id, created_at, updated_at) VALUES
-- DAILY용 댓글
('880e8400-e29b-41d4-a716-446655440101', '클린 코드 저도 감명받았어요!', false, '550e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440101', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY),
('880e8400-e29b-41d4-a716-446655440102', '해리포터 최고!', false, '550e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440102', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY),

-- WEEKLY용 댓글
('880e8400-e29b-41d4-a716-446655440201', '이펙티브 자바 정말 좋아요!', false, '550e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440201', CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '3' DAY),
('880e8400-e29b-41d4-a716-446655440202', '스프링 실습 도움됐어요', false, '550e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440202', CURRENT_TIMESTAMP - INTERVAL '5' DAY, CURRENT_TIMESTAMP - INTERVAL '5' DAY),

-- MONTHLY용 댓글
('880e8400-e29b-41d4-a716-446655440301', '1984 정말 무서워요', false, '550e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440301', CURRENT_TIMESTAMP - INTERVAL '15' DAY, CURRENT_TIMESTAMP - INTERVAL '15' DAY),
('880e8400-e29b-41d4-a716-446655440302', '클린 코드 명작이죠', false, '550e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440302', CURRENT_TIMESTAMP - INTERVAL '20' DAY, CURRENT_TIMESTAMP - INTERVAL '20' DAY),

-- ALL_TIME용 댓글
('880e8400-e29b-41d4-a716-446655440401', '해리포터는 전설이에요!', false, '550e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440401', CURRENT_TIMESTAMP - INTERVAL '60' DAY, CURRENT_TIMESTAMP - INTERVAL '60' DAY),
('880e8400-e29b-41d4-a716-446655440402', '클린 코드 개발자 필독서', false, '550e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440402', CURRENT_TIMESTAMP - INTERVAL '90' DAY, CURRENT_TIMESTAMP - INTERVAL '90' DAY);

-- ============= 좋아요 데이터 =============
INSERT INTO review_likes (id, user_id, review_id, created_at) VALUES
-- DAILY용 좋아요 (어제)
('990e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440101', CURRENT_TIMESTAMP - INTERVAL '1' DAY),
('990e8400-e29b-41d4-a716-446655440102', '550e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440102', CURRENT_TIMESTAMP - INTERVAL '1' DAY),

-- WEEKLY용 좋아요 (지난 주)
('990e8400-e29b-41d4-a716-446655440201', '550e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440201', CURRENT_TIMESTAMP - INTERVAL '3' DAY),
('990e8400-e29b-41d4-a716-446655440202', '550e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440202', CURRENT_TIMESTAMP - INTERVAL '5' DAY),

-- MONTHLY용 좋아요 (지난 달)
('990e8400-e29b-41d4-a716-446655440301', '550e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440301', CURRENT_TIMESTAMP - INTERVAL '15' DAY),
('990e8400-e29b-41d4-a716-446655440302', '550e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440302', CURRENT_TIMESTAMP - INTERVAL '20' DAY),

-- ALL_TIME용 좋아요 (역대)
('990e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440401', CURRENT_TIMESTAMP - INTERVAL '60' DAY),
('990e8400-e29b-41d4-a716-446655440402', '550e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440402', CURRENT_TIMESTAMP - INTERVAL '90' DAY);
