CREATE TABLE meetings (
                          id BIGSERIAL PRIMARY KEY,
                          group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                          creator_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          title VARCHAR(255) NOT NULL,
                          meeting_link TEXT,
                          status VARCHAR(20) NOT NULL CHECK (status IN ('ONGOING', 'ENDED')),
                          created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          ended_at TIMESTAMP
);

CREATE TABLE meeting_join (
                          id BIGSERIAL PRIMARY KEY,
                          meeting_id BIGINT NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
                          user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          UNIQUE (meeting_id, user_id)
);
