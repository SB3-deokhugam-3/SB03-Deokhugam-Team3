-- user table
CREATE TABLE users
(
    -- Primary Key
    id         UUID PRIMARY KEY,

    -- Column
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ,
    email      VARCHAR(320) NOT NULL UNIQUE,
    nickname   VARCHAR(50)  NOT NULL,
    password   VARCHAR(100) NOT NULL,
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE books
(
    -- Primary Key
    id             UUID PRIMARY KEY,

    -- Column
    created_at     TIMESTAMPTZ      NOT NULL,
    updated_at     TIMESTAMPTZ,
    title          VARCHAR(100)     NOT NULL,
    thumbnail_url  VARCHAR(512),
    author         VARCHAR(50)      NOT NULL,
    description    TEXT             NOT NULL,
    publisher      VARCHAR(50)      NOT NULL,
    published_date DATE             NOT NULL,
    rating         DOUBLE PRECISION NOT NULL CHECK (rating BETWEEN 0 AND 5),
    review_count   BIGINT           NOT NULL DEFAULT 0,
    isbn           VARCHAR(16)      NOT NULL UNIQUE,
    is_deleted     BOOLEAN          NOT NULL DEFAULT FALSE
);

CREATE TABLE reviews
(
    -- Primary Key
    id            UUID PRIMARY KEY,

    -- Column
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ,
    book_id       UUID        NOT NULL,
    user_id       UUID        NOT NULL,
    rating        INTEGER     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content       TEXT        NOT NULL,
    like_count    BIGINT      NOT NULL DEFAULT 0,
    comment_count BIGINT      NOT NULL DEFAULT 0,
    is_deleted    BOOLEAN     NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_reviews_book
        FOREIGN KEY (book_id)
            REFERENCES books (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE TABLE comments
(
    -- Primary Key
    id         UUID PRIMARY KEY,

    -- Column
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    user_id    UUID        NOT NULL,
    review_id  UUID        NOT NULL,
    content    TEXT        NOT NULL,
    is_deleted BOOLEAN     NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_comments_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_comments_review
        FOREIGN KEY (review_id)
            REFERENCES reviews (id)
            ON DELETE CASCADE
);

CREATE TABLE notifications
(
    -- Primary Key
    id         UUID PRIMARY KEY,

    -- Column
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    review_id  UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    content    VARCHAR(255),
    confirmed  BOOLEAN     NOT NULL,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_notifications_review
        FOREIGN KEY (review_id)
            REFERENCES reviews (id)
            ON DELETE CASCADE
);

CREATE TABLE review_likes
(
    -- PRIMARY KEY
    id         UUID        NOT NULL PRIMARY KEY,

    -- COLUMN
    created_at TIMESTAMPTZ NOT NULL,
    review_id  UUID        NOT NULL,
    user_id    UUID        NOT NULL,

    CONSTRAINT fk_review_likes_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_review_likes_review
        FOREIGN KEY (review_id)
            REFERENCES reviews (id)
            ON DELETE CASCADE,

    CONSTRAINT review_like_unique UNIQUE (review_id, user_id)
);

CREATE TABLE power_users
(
    -- Primary Key
    id               UUID PRIMARY KEY,

    -- Column
    created_at       TIMESTAMPTZ      NOT NULL,
    user_id          UUID             NOT NULL,
    period           VARCHAR(10)      NOT NULL CHECK ( period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    score            DOUBLE PRECISION NOT NULL,
    rank             BIGINT           NOT NULL,
    review_score_num DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    like_count       BIGINT           NOT NULL DEFAULT 0,
    comment_count    BIGINT           NOT NULL DEFAULT 0,

    CONSTRAINT fk_power_users_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE TABLE popular_book_rankings
(
    -- Primary Key
    id           UUID PRIMARY KEY,

    -- Column
    created_at   TIMESTAMPTZ      NOT NULL,
    book_id      UUID             NOT NULL,
    period       VARCHAR(10)      NOT NULL CHECK ( period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    rank         BIGINT           NOT NULL,
    score        DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    review_count BIGINT           NOT NULL DEFAULT 0,
    rating       DOUBLE PRECISION NOT NULL,

    CONSTRAINT fk_popular_book_rankings_book
        FOREIGN KEY (book_id)
            REFERENCES books (id)
            ON DELETE CASCADE
);

CREATE TABLE popular_review_rankings
(
    -- Primary Key
    id            UUID PRIMARY KEY,

    -- Column
    created_at    TIMESTAMPTZ      NOT NULL,
    review_id     UUID             NOT NULL,
    period        VARCHAR(10)      NOT NULL CHECK ( period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    rank          BIGINT           NOT NULL,
    score         DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    like_count    BIGINT           NOT NULL DEFAULT 0,
    comment_count BIGINT           NOT NULL DEFAULT 0,

    CONSTRAINT fk_popular_review_rankings_review
        FOREIGN KEY (review_id)
            REFERENCES reviews (id)
            ON DELETE CASCADE
);

-- power_users index 생성
CREATE INDEX idx_power_users ON power_users (period, created_at);

-- popular_book_rankings index 생성
CREATE INDEX idx_popular_book_rankings ON popular_book_rankings (period, created_at);

-- popular_review_rankings index 생성
CREATE INDEX idx_popular_review_rankings ON popular_review_rankings (period, created_at);

-- notifications index 생성
CREATE INDEX idx_notifications ON notifications (user_id, confirmed, created_at);

-- reviews index 생성
CREATE INDEX idx_reviews_created_at ON reviews (book_id, created_at);
CREATE INDEX idx_reviews_rating ON reviews (book_id, rating);

-- comments index 생성
CREATE INDEX idx_comments ON comments (review_id, created_at DESC);

-- review likes index 생성
CREATE INDEX idx_review_likes ON review_likes (user_id);

-- 지워지지 않은 review에 대한 unique index 생성
CREATE UNIQUE INDEX review_active_unique
    ON reviews (user_id, book_id)
    WHERE is_deleted = false;