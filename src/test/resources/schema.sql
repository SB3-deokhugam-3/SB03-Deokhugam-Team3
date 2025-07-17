-- user table
CREATE TABLE users
(
    id         VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP,
    email      VARCHAR(320)   NOT NULL UNIQUE,
    nickname   VARCHAR(50)    NOT NULL,
    password   VARCHAR(100)   NOT NULL,
    is_deleted BOOLEAN        NOT NULL DEFAULT FALSE
);

CREATE TABLE books
(
    id             VARCHAR(36) PRIMARY KEY,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP,
    title          VARCHAR(100) NOT NULL,
    thumbnail_url  VARCHAR(512),
    author         VARCHAR(50)  NOT NULL,
    description    TEXT         NOT NULL,
    publisher      VARCHAR(50)  NOT NULL,
    published_date DATE         NOT NULL,
    rating         DOUBLE       NOT NULL CHECK (rating BETWEEN 0 AND 5),
    review_count   BIGINT       NOT NULL DEFAULT 0,
    isbn           VARCHAR(16)  NOT NULL UNIQUE,
    is_deleted     BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE reviews
(
    id            VARCHAR(36) PRIMARY KEY,
    created_at    TIMESTAMP  NOT NULL,
    updated_at    TIMESTAMP,
    book_id       VARCHAR(36) NOT NULL,
    user_id       VARCHAR(36) NOT NULL,
    rating        INTEGER     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content       TEXT        NOT NULL,
    like_count    BIGINT      NOT NULL DEFAULT 0,
    comment_count BIGINT      NOT NULL DEFAULT 0,
    is_deleted    BOOLEAN     NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_reviews_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT review_unique UNIQUE (book_id, user_id)
);

CREATE TABLE comments
(
    id         VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP  NOT NULL,
    updated_at TIMESTAMP,
    user_id    VARCHAR(36) NOT NULL,
    review_id  VARCHAR(36) NOT NULL,
    content    TEXT        NOT NULL,
    is_deleted BOOLEAN     NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE
);

CREATE TABLE notifications
(
    id           VARCHAR(36) PRIMARY KEY,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP,
    review_id    VARCHAR(36)  NOT NULL,
    user_id      VARCHAR(36)  NOT NULL,
    content      VARCHAR(255),
    is_confirmed BOOLEAN      NOT NULL,

    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE
);

CREATE TABLE review_likes
(
    id         VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP    NOT NULL,
    review_id  VARCHAR(36)  NOT NULL,
    user_id    VARCHAR(36)  NOT NULL,

    CONSTRAINT fk_review_likes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_likes_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT review_like_unique UNIQUE (review_id, user_id)
);

CREATE TABLE power_users
(
    id               VARCHAR(36) PRIMARY KEY,
    created_at       TIMESTAMP NOT NULL,
    user_id          VARCHAR(36) NOT NULL,
    period           VARCHAR(10) NOT NULL CHECK ( period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    score            DOUBLE NOT NULL,
    rank             BIGINT NOT NULL,
    review_score_num DOUBLE NOT NULL DEFAULT 0.0,
    like_count       BIGINT NOT NULL DEFAULT 0,
    comment_count    BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_power_users_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE popular_book_rankings
(
    id           VARCHAR(36) PRIMARY KEY,
    created_at   TIMESTAMP NOT NULL,
    book_id      VARCHAR(36) NOT NULL,
    period       VARCHAR(10) NOT NULL CHECK ( period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    rank         BIGINT NOT NULL,
    score        DOUBLE NOT NULL DEFAULT 0.0,
    review_count BIGINT NOT NULL DEFAULT 0,
    rating       DOUBLE NOT NULL,

    CONSTRAINT fk_popular_book_rankings_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

CREATE TABLE popular_review_rankings
(
    id            VARCHAR(36) PRIMARY KEY,
    created_at    TIMESTAMP NOT NULL,
    review_id     VARCHAR(36) NOT NULL,
    period        VARCHAR(10) NOT NULL CHECK ( period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    rank          BIGINT NOT NULL,
    score         DOUBLE NOT NULL DEFAULT 0.0,
    like_count    BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_popular_review_rankings_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE
);

-- Indexes

CREATE INDEX idx_power_users ON power_users (period, created_at);
CREATE INDEX idx_popular_book_rankings ON popular_book_rankings (period, created_at);
CREATE INDEX idx_popular_review_rankings ON popular_review_rankings (period, created_at);
CREATE INDEX idx_notifications ON notifications (user_id, is_confirmed, created_at);
CREATE INDEX idx_reviews_created_at ON reviews (book_id, created_at);
CREATE INDEX idx_reviews_rating ON reviews (book_id, rating);
CREATE INDEX idx_comments ON comments (review_id, created_at DESC);
CREATE INDEX idx_review_likes ON review_likes (user_id);
