-- User 테이블에서 refresh_token 컬럼 삭제
ALTER TABLE users
DROP COLUMN IF EXISTS refresh_token;

-- 새 UserRefreshToken 테이블 생성
CREATE TABLE user_refresh_token (
                                    id BIGSERIAL PRIMARY KEY,
                                    user_id BIGINT NOT NULL,
                                    refresh_token_hash VARCHAR(100) NOT NULL,
                                    issued_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    expires_at TIMESTAMPTZ NOT NULL,
                                    revoked_at TIMESTAMPTZ,

                                    CONSTRAINT fk_user_refresh_token_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                                    CONSTRAINT uq_user_refresh_token_user UNIQUE (user_id, refresh_token_hash)
);

-- 인덱스 추가 (조회 속도 향상)
CREATE INDEX idx_user_refresh_token_user_id ON user_refresh_token(user_id);
CREATE INDEX idx_user_refresh_token_hash ON user_refresh_token(refresh_token_hash);
