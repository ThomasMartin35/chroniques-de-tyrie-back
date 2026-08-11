CREATE TABLE media
(
    id                UUID         PRIMARY KEY,
    storage_key       VARCHAR(500) NOT NULL UNIQUE,
    original_filename VARCHAR(255) NOT NULL,
    mime_type         VARCHAR(100) NOT NULL,
    size_bytes        BIGINT       NOT NULL,
    purpose           VARCHAR(50)  NOT NULL,
    alt_text          VARCHAR(255),
    uploaded_by_id    UUID         NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ,

    CONSTRAINT ck_media_size_bytes_positive
        CHECK (size_bytes > 0),

    CONSTRAINT ck_media_purpose
        CHECK (purpose IN ('AVATAR')),

    CONSTRAINT fk_media_uploaded_by
        FOREIGN KEY (uploaded_by_id)
            REFERENCES users (id)
);

ALTER TABLE users
    ADD CONSTRAINT uk_users_avatar
        UNIQUE (avatar_id),
    ADD CONSTRAINT fk_users_avatar
        FOREIGN KEY (avatar_id)
            REFERENCES media (id)
            ON DELETE SET NULL;

CREATE INDEX idx_media_uploaded_by_id
    ON media (uploaded_by_id);