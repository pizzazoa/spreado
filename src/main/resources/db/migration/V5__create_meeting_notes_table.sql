CREATE TABLE notes (
                               id BIGSERIAL PRIMARY KEY,
                               meeting_id BIGINT NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
                               content JSONB NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
