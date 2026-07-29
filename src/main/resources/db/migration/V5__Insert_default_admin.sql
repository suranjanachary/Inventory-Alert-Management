-- V5: default ADMIN (password: AdminPass123!)
-- BCrypt hash generated for Spring Security BCryptPasswordEncoder.
INSERT INTO users (full_name, email, password, role, enabled, created_at, updated_at)
SELECT
    'System Admin',
    'admin@inventory.local',
    '$2a$10$XbucIYm0DIhjLTNqY7FKDOnSgoswGkV4Me.Oa78WT0rLi4HLYahXe',
    'ADMIN',
    1,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@inventory.local'
);
