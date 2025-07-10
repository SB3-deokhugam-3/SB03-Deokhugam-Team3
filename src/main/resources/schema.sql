-- user table
CREATE TABLE users
(
    -- Primary Key
    id         UUID PRIMARY KEY,

    -- Column
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    email      VARCHAR(20) NOT NULL UNIQUE,
    nickname   VARCHAR(20) NOT NULL,
    password   VARCHAR(20) NOT NULL,
    is_deleted BOOLEAN     NOT NULL
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
    rating         DOUBLE PRECISION NOT NULL,
    review_count   BIGINT           NOT NULL,
    isbn           VARCHAR(16)      NOT NULL UNIQUE,
    is_deleted     BOOLEAN          NOT NULL
);

CREATE TABLE reviews
(
    -- Primary Key
    id            UUID PRIMARY KEY,

    -- Column
    created_at    TIMESTAMPTZ      NOT NULL,
    updated_at    TIMESTAMPTZ,
    book_id       UUID             NOT NULL,
    user_id       UUID             NOT NULL,
    rating        DOUBLE PRECISION NOT NULL,
    content       TEXT             NOT NULL,
    like_count    BIGINT           NOT NULL,
    comment_count BIGINT           NOT NULL,
    is_deleted    BOOLEAN          NOT NULL,

    CONSTRAINT fk_book
        FOREIGN KEY (book_id)
            REFERENCES books (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT review_unique UNIQUE (book_id, user_id)
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
    is_deleted BOOLEAN     NOT NULL,

    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_review
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

    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_review
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

    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_review
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
    review_score_num DOUBLE PRECISION NOT NULL,
    like_count       BIGINT           NOT NULL,
    comment_count    BIGINT           NOT NULL,

    CONSTRAINT fk_user
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
    score        DOUBLE PRECISION NOT NULL,
    review_count BIGINT           NOT NULL,
    rating       DOUBLE PRECISION NOT NULL,

    CONSTRAINT fk_book
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
    score         DOUBLE PRECISION NOT NULL,
    like_count    BIGINT           NOT NULL,
    comment_count BIGINT           NOT NULL,

    CONSTRAINT fk_review
        FOREIGN KEY (review_id)
            REFERENCES reviews (id)
            ON DELETE CASCADE
);