CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255),
                       name VARCHAR(255),
                       created_at TIMESTAMPTZ,
                       updated_at TIMESTAMPTZ,
                       refresh_token VARCHAR(255)
);
