-- The avatar is an item in tn-media-service and the user holds only its id (avatar_media_id). Nothing was live, so the
-- avatar table and the version and upload-limit columns that went with it are dropped, not kept.
DROP TABLE user_avatars;

ALTER TABLE users
    DROP COLUMN avatar_version,
    DROP COLUMN avatar_revision,
    DROP COLUMN avatar_window_start,
    DROP COLUMN avatar_window_count;
