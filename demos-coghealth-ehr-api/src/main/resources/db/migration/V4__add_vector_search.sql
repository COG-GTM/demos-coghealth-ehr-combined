CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE patients ADD COLUMN embedding vector(1536);

CREATE INDEX idx_patient_embedding ON patients
  USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
