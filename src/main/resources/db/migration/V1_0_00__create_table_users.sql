CREATE SEQUENCE IF NOT EXISTS user_id_seq;

CREATE TABLE IF NOT EXISTS users
(
    user_id       INT          NOT NULL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    token_subject VARCHAR(100) NULL,
    created       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (email)
);