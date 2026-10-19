-- The user's avatar is an item in tn-media-service; this is the reference to it. The avatar table and avatar_version stay
-- until the apps no longer use them, and are then dropped.
ALTER TABLE users
    ADD COLUMN avatar_media_id BIGINT;
