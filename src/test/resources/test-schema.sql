-- spring batch 메타데이터 테이블
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_PARAMS;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION;
DROP TABLE IF EXISTS BATCH_JOB_INSTANCE;

DROP SEQUENCE IF EXISTS BATCH_STEP_EXECUTION_SEQ;
DROP SEQUENCE IF EXISTS BATCH_JOB_EXECUTION_SEQ;
DROP SEQUENCE IF EXISTS BATCH_JOB_SEQ;

CREATE TABLE BATCH_JOB_INSTANCE
(
    JOB_INSTANCE_ID BIGINT       NOT NULL PRIMARY KEY,
    VERSION         BIGINT,
    JOB_NAME        VARCHAR(100) NOT NULL,
    JOB_KEY         VARCHAR(32)  NOT NULL,
    constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
);

CREATE TABLE BATCH_JOB_EXECUTION
(
    JOB_EXECUTION_ID BIGINT    NOT NULL PRIMARY KEY,
    VERSION          BIGINT,
    JOB_INSTANCE_ID  BIGINT    NOT NULL,
    CREATE_TIME      TIMESTAMP NOT NULL,
    START_TIME       TIMESTAMP DEFAULT NULL,
    END_TIME         TIMESTAMP DEFAULT NULL,
    STATUS           VARCHAR(10),
    EXIT_CODE        VARCHAR(2500),
    EXIT_MESSAGE     VARCHAR(2500),
    LAST_UPDATED     TIMESTAMP,
    constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
        references BATCH_JOB_INSTANCE (JOB_INSTANCE_ID)
);

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS
(
    JOB_EXECUTION_ID BIGINT       NOT NULL,
    PARAMETER_NAME   VARCHAR(100) NOT NULL,
    PARAMETER_TYPE   VARCHAR(100) NOT NULL,
    PARAMETER_VALUE  VARCHAR(2500),
    IDENTIFYING      CHAR(1)      NOT NULL,
    constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

CREATE TABLE BATCH_STEP_EXECUTION
(
    STEP_EXECUTION_ID  BIGINT       NOT NULL PRIMARY KEY,
    VERSION            BIGINT       NOT NULL,
    STEP_NAME          VARCHAR(100) NOT NULL,
    JOB_EXECUTION_ID   BIGINT       NOT NULL,
    CREATE_TIME        TIMESTAMP    NOT NULL,
    START_TIME         TIMESTAMP DEFAULT NULL,
    END_TIME           TIMESTAMP DEFAULT NULL,
    STATUS             VARCHAR(10),
    COMMIT_COUNT       BIGINT,
    READ_COUNT         BIGINT,
    FILTER_COUNT       BIGINT,
    WRITE_COUNT        BIGINT,
    READ_SKIP_COUNT    BIGINT,
    WRITE_SKIP_COUNT   BIGINT,
    PROCESS_SKIP_COUNT BIGINT,
    ROLLBACK_COUNT     BIGINT,
    EXIT_CODE          VARCHAR(2500),
    EXIT_MESSAGE       VARCHAR(2500),
    LAST_UPDATED       TIMESTAMP,
    constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT
(
    STEP_EXECUTION_ID  BIGINT        NOT NULL PRIMARY KEY,
    SHORT_CONTEXT      VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT,
    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
        references BATCH_STEP_EXECUTION (STEP_EXECUTION_ID)
);

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT
(
    JOB_EXECUTION_ID   BIGINT        NOT NULL PRIMARY KEY,
    SHORT_CONTEXT      VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT,
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_SEQ MAXVALUE 9223372036854775807 NO CYCLE;


-- user table
CREATE TABLE IF NOT EXISTS users
(
    -- Primary Key
    id         VARCHAR(36) PRIMARY KEY,

    -- Column
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP,
    email      VARCHAR(320) NOT NULL UNIQUE,
    nickname   VARCHAR(50)  NOT NULL,
    password   VARCHAR(100) NOT NULL,
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS books
(
    -- Primary Key
    id             VARCHAR(36) PRIMARY KEY,

    -- Column
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

CREATE TABLE IF NOT EXISTS reviews
(
    -- Primary Key
    id            VARCHAR(36) PRIMARY KEY,

    -- Column
    created_at    TIMESTAMP   NOT NULL,
    updated_at    TIMESTAMP,
    book_id       VARCHAR(36) NOT NULL,
    user_id       VARCHAR(36) NOT NULL,
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

CREATE TABLE IF NOT EXISTS comments
(
    -- Primary Key
    id         VARCHAR(36) PRIMARY KEY,

    -- Column
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP,
    user_id    VARCHAR(36) NOT NULL,
    review_id  VARCHAR(36) NOT NULL,
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

CREATE TABLE IF NOT EXISTS notifications
(
    -- Primary Key
    id           VARCHAR(36) PRIMARY KEY,

    -- Column
    created_at   TIMESTAMP   NOT NULL,
    updated_at   TIMESTAMP,
    review_id    VARCHAR(36) NOT NULL,
    user_id      VARCHAR(36) NOT NULL,
    content      VARCHAR(255),
    confirmed BOOLEAN     NOT NULL,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_notifications_review
        FOREIGN KEY (review_id)
            REFERENCES reviews (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review_likes
(
    -- PRIMARY KEY
    id         VARCHAR(36) NOT NULL PRIMARY KEY,

    -- COLUMN
    created_at TIMESTAMP   NOT NULL,
    review_id  VARCHAR(36) NOT NULL,
    user_id    VARCHAR(36) NOT NULL,

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

CREATE TABLE IF NOT EXISTS power_users
(
    -- Primary Key
    id               VARCHAR(36) PRIMARY KEY,

    -- Column
    created_at       TIMESTAMP        NOT NULL,
    user_id          VARCHAR(36)      NOT NULL,
    period           VARCHAR(10)      NOT NULL CHECK ( period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    score            DOUBLE PRECISION NOT NULL,
    rank             BIGINT           NOT NULL,
    review_score_sum DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    like_count       BIGINT           NOT NULL DEFAULT 0,
    comment_count    BIGINT           NOT NULL DEFAULT 0,

    CONSTRAINT fk_power_users_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS popular_book_rankings
(
    -- Primary Key
    id           VARCHAR(36) PRIMARY KEY,

    -- Column
    created_at   TIMESTAMP        NOT NULL,
    book_id      VARCHAR(36)      NOT NULL,
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

CREATE TABLE IF NOT EXISTS popular_review_rankings
(
    -- Primary Key
    id            VARCHAR(36) PRIMARY KEY,

    -- Column
    created_at    TIMESTAMP        NOT NULL,
    review_id     VARCHAR(36)      NOT NULL,
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

-- Indexes

CREATE INDEX IF NOT EXISTS idx_power_users ON power_users (period, created_at);

-- popular_book_rankings index 생성
CREATE INDEX IF NOT EXISTS idx_popular_book_rankings ON popular_book_rankings (period, created_at);

-- popular_review_rankings index 생성
CREATE INDEX IF NOT EXISTS idx_popular_review_rankings ON popular_review_rankings (period, created_at);

-- notifications index 생성
CREATE INDEX IF NOT EXISTS idx_notifications ON notifications (user_id, confirmed, created_at);

-- reviews index 생성
CREATE INDEX IF NOT EXISTS idx_reviews_created_at ON reviews (book_id, created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_rating ON reviews (book_id, rating);

-- comments index 생성
CREATE INDEX IF NOT EXISTS idx_comments ON comments (review_id, created_at DESC);

-- review likes index 생성
CREATE INDEX IF NOT EXISTS idx_review_likes ON review_likes (user_id);

-- 지워지지 않은 review에 대한 unique index 생성
CREATE UNIQUE INDEX IF NOT EXISTS review_active_unique
    ON reviews (user_id, book_id)
--     WHERE is_deleted = false;