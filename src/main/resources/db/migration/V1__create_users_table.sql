CREATE TABLE users (
   id UUID PRIMARY KEY,
   username VARCHAR(50) NOT NULL UNIQUE,
   email VARCHAR(180) NOT NULL UNIQUE,
   password_hash VARCHAR(255) NOT NULL,
   role VARCHAR(30) NOT NULL,
   avatar_id UUID NULL,
   biography TEXT,
   is_active BOOLEAN NOT NULL DEFAULT TRUE,
   last_login_at TIMESTAMPTZ,
   last_password_change_at TIMESTAMPTZ,
   created_at TIMESTAMPTZ NOT NULL,
   updated_at TIMESTAMPTZ
);