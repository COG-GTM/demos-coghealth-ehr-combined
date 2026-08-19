-- Link user accounts to the provider record they act as, so patient access can be
-- authorized against a documented care relationship instead of being open to all callers.
ALTER TABLE users ADD COLUMN provider_id BIGINT REFERENCES providers(id);

CREATE INDEX idx_users_provider ON users(provider_id);

-- Match existing accounts to providers by email where the addresses already line up.
UPDATE users u
SET provider_id = p.id
FROM providers p
WHERE u.provider_id IS NULL
  AND LOWER(u.email) = LOWER(p.email);
