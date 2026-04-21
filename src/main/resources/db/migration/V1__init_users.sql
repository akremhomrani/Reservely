CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    provider      VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    provider_id   VARCHAR(255),
    name          VARCHAR(100) NOT NULL,
    phone         VARCHAR(20),
    city          VARCHAR(100),
    role          VARCHAR(30)  NOT NULL DEFAULT 'CUSTOMER',
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_users_provider_id ON users (provider, provider_id)
    WHERE provider_id IS NOT NULL;

CREATE INDEX idx_users_email ON users (email);

COMMENT ON COLUMN users.password_hash IS 'NULL for OAuth2-only accounts';
COMMENT ON COLUMN users.provider_id   IS 'Google sub claim or similar';
