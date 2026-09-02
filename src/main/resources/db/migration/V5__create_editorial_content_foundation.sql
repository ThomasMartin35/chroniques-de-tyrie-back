CREATE TABLE categories
(
    id          UUID         PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ,

    CONSTRAINT uk_categories_slug
        UNIQUE (slug)
);

CREATE UNIQUE INDEX uk_categories_name_ci
    ON categories (LOWER(name));

CREATE TABLE tags
(
    id          UUID         PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ,

    CONSTRAINT uk_tags_slug
        UNIQUE (slug)
);

CREATE UNIQUE INDEX uk_tags_name_ci
    ON tags (LOWER(name));

CREATE TABLE contents
(
    id                      UUID         PRIMARY KEY,
    title                   VARCHAR(180) NOT NULL,
    slug                    VARCHAR(220) NOT NULL,
    excerpt                 VARCHAR(500),
    body                    JSONB,
    type                    VARCHAR(30)  NOT NULL,
    status                  VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    author_id               UUID         NOT NULL,
    category_id             UUID,
    featured_image_id       UUID,
    featured_image_alt_text VARCHAR(250),
    reading_time            INTEGER,
    submitted_at            TIMESTAMPTZ,
    review_note             VARCHAR(1000),
    reviewed_by_id          UUID,
    reviewed_at             TIMESTAMPTZ,
    published_by_id         UUID,
    published_at            TIMESTAMPTZ,
    archived_by_id          UUID,
    archived_at             TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL,
    updated_at              TIMESTAMPTZ,

    CONSTRAINT uk_contents_slug
        UNIQUE (slug),

    CONSTRAINT uk_contents_featured_image
        UNIQUE (featured_image_id),

    CONSTRAINT ck_contents_type
        CHECK (type IN ('NEWS', 'CHRONICLE', 'GUIDE')),

    CONSTRAINT ck_contents_status
        CHECK (status IN (
            'DRAFT',
            'PENDING_REVIEW',
            'CHANGES_REQUESTED',
            'PUBLISHED',
            'ARCHIVED'
        )),

    CONSTRAINT ck_contents_reading_time_positive
        CHECK (reading_time IS NULL OR reading_time > 0),

    CONSTRAINT fk_contents_author
        FOREIGN KEY (author_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_contents_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_contents_featured_image
        FOREIGN KEY (featured_image_id)
            REFERENCES media (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_contents_reviewed_by
        FOREIGN KEY (reviewed_by_id)
            REFERENCES users (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_contents_published_by
        FOREIGN KEY (published_by_id)
            REFERENCES users (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_contents_archived_by
        FOREIGN KEY (archived_by_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);

CREATE TABLE content_tags
(
    content_id UUID NOT NULL,
    tag_id     UUID NOT NULL,

    CONSTRAINT pk_content_tags
        PRIMARY KEY (content_id, tag_id),

    CONSTRAINT fk_content_tags_content
        FOREIGN KEY (content_id)
            REFERENCES contents (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_content_tags_tag
        FOREIGN KEY (tag_id)
            REFERENCES tags (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_contents_author_id
    ON contents (author_id);

CREATE INDEX idx_contents_status
    ON contents (status);

CREATE INDEX idx_contents_category_id
    ON contents (category_id);

CREATE INDEX idx_contents_review_queue
    ON contents (status, submitted_at);

CREATE INDEX idx_contents_public_news
    ON contents (type, status, published_at DESC);

CREATE INDEX idx_content_tags_tag_id
    ON content_tags (tag_id);
