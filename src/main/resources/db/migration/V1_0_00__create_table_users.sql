CREATE TABLE IF NOT EXISTS users
(
    user_id          BIGINT       NOT NULL PRIMARY KEY,
    identifier_type  VARCHAR(16)  NOT NULL,
    identifier_value VARCHAR(255) NOT NULL,
    full_name        VARCHAR(100),
    preferred_name   VARCHAR(100),
    token_subject    VARCHAR(100),
    created          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (identifier_type, identifier_value)
);
