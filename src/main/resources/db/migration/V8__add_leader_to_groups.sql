ALTER TABLE groups
ADD COLUMN leader_id BIGINT REFERENCES users (id) ON DELETE SET NULL;

-- 기존 그룹들에 대해 가장 먼저 가입한 멤버를 leader로 설정
UPDATE groups g
SET leader_id = (
    SELECT gm.user_id
    FROM group_members gm
    WHERE gm.group_id = g.id
    ORDER BY gm.joined_at ASC
    LIMIT 1
);
