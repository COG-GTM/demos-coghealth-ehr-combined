-- Widen lab result PHI columns to hold AES-256-GCM ciphertext (base64, "enc:v1:" prefixed)
ALTER TABLE lab_results ALTER COLUMN value TYPE VARCHAR(512);
ALTER TABLE lab_results ALTER COLUMN interpretation TYPE VARCHAR(2048);
ALTER TABLE lab_results ALTER COLUMN comments TYPE VARCHAR(2048);
