CREATE TABLE IF NOT EXISTS users
(
    user_id          BIGINT       NOT NULL PRIMARY KEY,
    email            VARCHAR(255) UNIQUE,
    phone            VARCHAR(255) UNIQUE,
    full_name        VARCHAR(100),
    preferred_name   VARCHAR(100),
    created          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
