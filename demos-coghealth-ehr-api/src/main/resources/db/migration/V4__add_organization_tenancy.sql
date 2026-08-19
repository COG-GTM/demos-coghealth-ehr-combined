-- Organization (tenant) boundary between provider groups.
-- Every PHI-bearing row and every user belongs to exactly one organization so
-- that queries can be scoped to the caller's tenant.

CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

INSERT INTO organizations (code, name) VALUES ('DEFAULT', 'CogHealth Default Organization');

ALTER TABLE users ADD COLUMN organization_id BIGINT REFERENCES organizations(id);
ALTER TABLE providers ADD COLUMN organization_id BIGINT REFERENCES organizations(id);
ALTER TABLE patients ADD COLUMN organization_id BIGINT REFERENCES organizations(id);
ALTER TABLE encounters ADD COLUMN organization_id BIGINT REFERENCES organizations(id);

UPDATE users SET organization_id = (SELECT id FROM organizations WHERE code = 'DEFAULT');
UPDATE providers SET organization_id = (SELECT id FROM organizations WHERE code = 'DEFAULT');
UPDATE patients SET organization_id = (SELECT id FROM organizations WHERE code = 'DEFAULT');
UPDATE encounters SET organization_id = (SELECT id FROM organizations WHERE code = 'DEFAULT');

ALTER TABLE users ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE providers ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE patients ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE encounters ALTER COLUMN organization_id SET NOT NULL;

CREATE INDEX idx_users_organization ON users(organization_id);
CREATE INDEX idx_provider_organization ON providers(organization_id);
CREATE INDEX idx_patient_organization ON patients(organization_id);
CREATE INDEX idx_encounter_organization ON encounters(organization_id);
