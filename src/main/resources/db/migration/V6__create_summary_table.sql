CREATE TABLE IF NOT EXISTS summaries (
    id           BIGSERIAL   PRIMARY KEY,
    note_id      BIGINT      NOT NULL REFERENCES notes(id) ON DELETE CASCADE,
    summary_json JSONB       NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_summaries_note_id ON summaries (note_id);
