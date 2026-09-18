-- Widen lab result PHI columns to hold AES-256-GCM ciphertext (base64, "enc:v1:" prefixed).
-- Sized for the worst case of 4-byte UTF-8 characters filling the previous limits.
ALTER TABLE lab_results ALTER COLUMN value TYPE VARCHAR(1024);
ALTER TABLE lab_results ALTER COLUMN interpretation TYPE VARCHAR(4096);
ALTER TABLE lab_results ALTER COLUMN comments TYPE VARCHAR(4096);
