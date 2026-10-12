-- The picture is in a table of its own, so a query on users never reads it. users holds only what is needed
-- to say whether a user has one and to limit how often it is changed:
--   avatar_version  the version of the user's current picture, null when there is none; shown to callers
--   avatar_revision how many pictures the user has ever set. Only ever goes up, so a version is never reused
--                   after a picture is removed and another added
--   avatar_window_* the start of the current rate-limit window, and the uploads started in it
ALTER TABLE users
    ADD COLUMN avatar_version      INTEGER,
    ADD COLUMN avatar_revision     INTEGER   NOT NULL DEFAULT 0,
    ADD COLUMN avatar_window_start TIMESTAMP,
    ADD COLUMN avatar_window_count INTEGER   NOT NULL DEFAULT 0;

CREATE TABLE user_avatars
(
    user_id      BIGINT      NOT NULL PRIMARY KEY REFERENCES users (user_id) ON DELETE CASCADE,
    content_type VARCHAR(32) NOT NULL,
    image        BYTEA       NOT NULL
);
