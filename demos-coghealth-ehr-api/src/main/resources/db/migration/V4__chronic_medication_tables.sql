-- V4__chronic_medication_tables.sql
-- Tables for chronic medication management and adherence tracking

-- Medication adherence tracking table
CREATE TABLE medication_adherence (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    medication_order_id BIGINT NOT NULL,
    chronic_condition_id BIGINT,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    pdc_score DECIMAL(5,4),
    days_supply INTEGER,
    days_covered INTEGER,
    refills_on_time INTEGER DEFAULT 0,
    refills_late INTEGER DEFAULT 0,
    refills_missed INTEGER DEFAULT 0,
    adherence_status VARCHAR(30),
    last_fill_date DATE,
    next_fill_due DATE,
    pharmacy_npi VARCHAR(10),
    pharmacy_name VARCHAR(200),
    alert_sent BOOLEAN DEFAULT FALSE,
    alert_sent_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_med_adherence_patient ON medication_adherence(patient_id);
CREATE INDEX idx_med_adherence_medication ON medication_adherence(medication_order_id);
CREATE INDEX idx_med_adherence_status ON medication_adherence(adherence_status);
CREATE INDEX idx_med_adherence_period ON medication_adherence(period_start, period_end);

-- Medication fills table for tracking pharmacy dispenses
CREATE TABLE medication_fills (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    medication_order_id BIGINT NOT NULL,
    fill_date DATE NOT NULL,
    days_supply INTEGER NOT NULL,
    ndc VARCHAR(11),
    pharmacy_npi VARCHAR(10),
    pharmacy_name VARCHAR(200),
    rx_number VARCHAR(30),
    quantity_dispensed INTEGER,
    on_time BOOLEAN,
    days_late INTEGER,
    fill_source VARCHAR(50),
    external_reference_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_med_fill_patient ON medication_fills(patient_id);
CREATE INDEX idx_med_fill_medication ON medication_fills(medication_order_id);
CREATE INDEX idx_med_fill_date ON medication_fills(fill_date);
CREATE INDEX idx_med_fill_ndc ON medication_fills(ndc);

-- Chronic conditions table
CREATE TABLE chronic_conditions (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    condition_type VARCHAR(50) NOT NULL,
    icd10_code VARCHAR(20),
    severity VARCHAR(30),
    status VARCHAR(30) NOT NULL,
    diagnosis_date DATE NOT NULL,
    managing_provider_id BIGINT,
    enrollment_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chronic_condition_patient ON chronic_conditions(patient_id);
CREATE INDEX idx_chronic_condition_type ON chronic_conditions(condition_type);
CREATE INDEX idx_chronic_condition_status ON chronic_conditions(status);

-- Diabetes management table
CREATE TABLE diabetes_management (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    chronic_condition_id BIGINT NOT NULL,
    last_hba1c_date DATE,
    last_hba1c_value DECIMAL(5,2),
    target_hba1c DECIMAL(5,2),
    last_eye_exam_date DATE,
    last_foot_exam_date DATE,
    last_ldl_date DATE,
    last_ldl_value DECIMAL(5,2),
    target_ldl DECIMAL(5,2),
    blood_glucose_monitoring BOOLEAN,
    medication_regimen TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_diabetes_patient ON diabetes_management(patient_id);
CREATE INDEX idx_diabetes_condition ON diabetes_management(chronic_condition_id);