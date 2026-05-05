-- CogHealth EHR Seed Data
-- Run: docker exec -i coghealth-postgres psql -U coghealth coghealth < seed.sql

-- =============================================================================
-- SCHEMA (from V1__initial_schema.sql)
-- =============================================================================

CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
    mrn VARCHAR(20) UNIQUE NOT NULL,
    ssn VARCHAR(11),
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20),
    marital_status VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    phone_home VARCHAR(20),
    phone_mobile VARCHAR(20),
    phone_work VARCHAR(20),
    address_line1 VARCHAR(200),
    address_line2 VARCHAR(200),
    street1 VARCHAR(200),
    street2 VARCHAR(200),
    city VARCHAR(100),
    state VARCHAR(50),
    zip_code VARCHAR(20),
    country VARCHAR(50),
    mail_street1 VARCHAR(200),
    mail_street2 VARCHAR(200),
    mail_city VARCHAR(100),
    mail_state VARCHAR(50),
    mail_zip_code VARCHAR(20),
    mail_country VARCHAR(50),
    preferred_language VARCHAR(50),
    ethnicity VARCHAR(50),
    race VARCHAR(50),
    religion VARCHAR(100),
    emergency_contact_name VARCHAR(200),
    emergency_contact_phone VARCHAR(20),
    primary_provider_id BIGINT,
    insurance_id VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deceased BOOLEAN NOT NULL DEFAULT FALSE,
    deceased_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_patient_mrn ON patients(mrn);
CREATE INDEX IF NOT EXISTS idx_patient_ssn ON patients(ssn);
CREATE INDEX IF NOT EXISTS idx_patient_last_name ON patients(last_name);

CREATE TABLE IF NOT EXISTS patient_identifiers (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    identifier_type VARCHAR(50) NOT NULL,
    identifier_value VARCHAR(100) NOT NULL,
    issuing_authority VARCHAR(100),
    effective_date DATE,
    expiration_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_patient_identifier_value ON patient_identifiers(identifier_value);
CREATE INDEX IF NOT EXISTS idx_patient_identifier_type ON patient_identifiers(identifier_type);

CREATE TABLE IF NOT EXISTS emergency_contacts (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    relationship VARCHAR(50),
    phone_home VARCHAR(20),
    phone_mobile VARCHAR(20),
    phone_work VARCHAR(20),
    email VARCHAR(100),
    street1 VARCHAR(200),
    street2 VARCHAR(200),
    city VARCHAR(100),
    state VARCHAR(50),
    zip_code VARCHAR(20),
    country VARCHAR(50),
    priority INTEGER NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS providers (
    id BIGSERIAL PRIMARY KEY,
    npi VARCHAR(10) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    suffix VARCHAR(20),
    credentials VARCHAR(50),
    provider_type VARCHAR(30),
    specialty VARCHAR(100),
    subspecialty VARCHAR(100),
    department VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    phone_office VARCHAR(20),
    phone_mobile VARCHAR(20),
    pager VARCHAR(20),
    fax VARCHAR(20),
    hire_date DATE,
    termination_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    accepting_patients BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_provider_npi ON providers(npi);
CREATE INDEX IF NOT EXISTS idx_provider_last_name ON providers(last_name);

CREATE TABLE IF NOT EXISTS provider_licenses (
    provider_id BIGINT NOT NULL REFERENCES providers(id),
    license_type VARCHAR(50),
    license_number VARCHAR(50),
    state VARCHAR(50),
    issue_date DATE,
    expiration_date DATE,
    status VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS encounters (
    id BIGSERIAL PRIMARY KEY,
    encounter_number VARCHAR(30) UNIQUE NOT NULL,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    provider_id BIGINT REFERENCES providers(id),
    attending_provider_id BIGINT REFERENCES providers(id),
    encounter_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    encounter_date DATE,
    encounter_date_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    admit_date_time TIMESTAMP,
    discharge_date_time TIMESTAMP,
    department VARCHAR(50),
    location VARCHAR(100),
    room VARCHAR(20),
    bed VARCHAR(20),
    chief_complaint VARCHAR(500),
    priority VARCHAR(20),
    visit_type VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_encounter_patient ON encounters(patient_id);
CREATE INDEX IF NOT EXISTS idx_encounter_provider ON encounters(attending_provider_id);
CREATE INDEX IF NOT EXISTS idx_encounter_date ON encounters(encounter_date_time);
CREATE INDEX IF NOT EXISTS idx_encounter_status ON encounters(status);

CREATE TABLE IF NOT EXISTS diagnoses (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    icd10_code VARCHAR(20) NOT NULL,
    description VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    severity VARCHAR(20),
    onset_date DATE NOT NULL,
    abatement_date DATE,
    diagnosed_by BIGINT REFERENCES providers(id),
    notes VARCHAR(1000),
    chronic BOOLEAN NOT NULL DEFAULT FALSE,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_diagnosis_patient ON diagnoses(patient_id);
CREATE INDEX IF NOT EXISTS idx_diagnosis_icd10 ON diagnoses(icd10_code);
CREATE INDEX IF NOT EXISTS idx_diagnosis_status ON diagnoses(status);

CREATE TABLE IF NOT EXISTS encounter_diagnoses (
    id BIGSERIAL PRIMARY KEY,
    encounter_id BIGINT NOT NULL REFERENCES encounters(id),
    diagnosis_id BIGINT NOT NULL REFERENCES diagnoses(id),
    role VARCHAR(20),
    rank INTEGER NOT NULL DEFAULT 1,
    primary_diagnosis BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS allergies (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    allergy_type VARCHAR(30) NOT NULL,
    allergen VARCHAR(200) NOT NULL,
    allergen_code VARCHAR(50),
    allergen_code_system VARCHAR(20),
    severity VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    reaction VARCHAR(500),
    onset_date DATE,
    recorded_date DATE,
    notes VARCHAR(1000),
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_allergy_patient ON allergies(patient_id);
CREATE INDEX IF NOT EXISTS idx_allergy_status ON allergies(status);

CREATE TABLE IF NOT EXISTS clinical_notes (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    encounter_id BIGINT REFERENCES encounters(id),
    author_id BIGINT NOT NULL REFERENCES providers(id),
    note_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    signed_date_time TIMESTAMP,
    cosigner_id BIGINT REFERENCES providers(id),
    cosigned_date_time TIMESTAMP,
    amended_from_id BIGINT REFERENCES clinical_notes(id),
    amendment_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_note_patient ON clinical_notes(patient_id);
CREATE INDEX IF NOT EXISTS idx_note_encounter ON clinical_notes(encounter_id);
CREATE INDEX IF NOT EXISTS idx_note_author ON clinical_notes(author_id);
CREATE INDEX IF NOT EXISTS idx_note_type ON clinical_notes(note_type);

CREATE TABLE IF NOT EXISTS vitals (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    encounter_id BIGINT REFERENCES encounters(id),
    recorded_date_time TIMESTAMP NOT NULL,
    temperature_fahrenheit DECIMAL(5,1),
    heart_rate INTEGER,
    respiratory_rate INTEGER,
    systolic_bp INTEGER,
    diastolic_bp INTEGER,
    oxygen_saturation DECIMAL(5,1),
    height_inches DECIMAL(5,1),
    weight_pounds DECIMAL(6,2),
    bmi DECIMAL(4,1),
    pain_level INTEGER,
    blood_pressure_position VARCHAR(50),
    blood_pressure_site VARCHAR(50),
    temperature_site VARCHAR(50),
    oxygen_delivery_method VARCHAR(50),
    oxygen_flow_rate DECIMAL(4,1),
    notes VARCHAR(500),
    recorded_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_vitals_patient ON vitals(patient_id);
CREATE INDEX IF NOT EXISTS idx_vitals_encounter ON vitals(encounter_id);
CREATE INDEX IF NOT EXISTS idx_vitals_recorded ON vitals(recorded_date_time);

CREATE TABLE IF NOT EXISTS medications (
    id BIGSERIAL PRIMARY KEY,
    ndc_code VARCHAR(20),
    rxnorm_code VARCHAR(20),
    generic_name VARCHAR(200) NOT NULL,
    brand_name VARCHAR(200),
    manufacturer VARCHAR(100),
    strength VARCHAR(100),
    form VARCHAR(100),
    route VARCHAR(100),
    schedule VARCHAR(20),
    controlled BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    warnings VARCHAR(1000),
    contraindications VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_medication_ndc ON medications(ndc_code);
CREATE INDEX IF NOT EXISTS idx_medication_rxnorm ON medications(rxnorm_code);
CREATE INDEX IF NOT EXISTS idx_medication_name ON medications(generic_name);

CREATE TABLE IF NOT EXISTS medication_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(30) UNIQUE NOT NULL,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    encounter_id BIGINT REFERENCES encounters(id),
    medication_id BIGINT NOT NULL REFERENCES medications(id),
    prescriber_id BIGINT NOT NULL REFERENCES providers(id),
    order_date_time TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    dose VARCHAR(100) NOT NULL,
    dose_unit VARCHAR(50) NOT NULL,
    route VARCHAR(50) NOT NULL,
    frequency VARCHAR(100) NOT NULL,
    sig VARCHAR(200),
    start_date DATE,
    end_date DATE,
    quantity INTEGER,
    refills INTEGER,
    refills_remaining INTEGER,
    days_supply INTEGER,
    dispense_as_written BOOLEAN NOT NULL DEFAULT FALSE,
    prn BOOLEAN NOT NULL DEFAULT FALSE,
    prn_reason VARCHAR(200),
    instructions VARCHAR(500),
    pharmacy_notes VARCHAR(500),
    pharmacy VARCHAR(200),
    pharmacy_npi VARCHAR(20),
    sent_to_pharmacy_at TIMESTAMP,
    discontinued_reason VARCHAR(100),
    discontinued_at TIMESTAMP,
    discontinued_by BIGINT REFERENCES providers(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_med_order_patient ON medication_orders(patient_id);
CREATE INDEX IF NOT EXISTS idx_med_order_provider ON medication_orders(prescriber_id);
CREATE INDEX IF NOT EXISTS idx_med_order_status ON medication_orders(status);
CREATE INDEX IF NOT EXISTS idx_med_order_date ON medication_orders(order_date_time);

CREATE TABLE IF NOT EXISTS medication_administrations (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    encounter_id BIGINT REFERENCES encounters(id),
    medication_order_id BIGINT NOT NULL REFERENCES medication_orders(id),
    administered_by BIGINT NOT NULL REFERENCES providers(id),
    scheduled_date_time TIMESTAMP NOT NULL,
    administered_date_time TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    dose_given VARCHAR(100),
    route VARCHAR(50),
    site VARCHAR(50),
    notes VARCHAR(500),
    reason_not_given VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_med_admin_patient ON medication_administrations(patient_id);
CREATE INDEX IF NOT EXISTS idx_med_admin_order ON medication_administrations(medication_order_id);
CREATE INDEX IF NOT EXISTS idx_med_admin_time ON medication_administrations(administered_date_time);

CREATE TABLE IF NOT EXISTS insurance_coverages (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    payer_name VARCHAR(100) NOT NULL,
    payer_id VARCHAR(50) NOT NULL,
    plan_name VARCHAR(100),
    plan_type VARCHAR(50),
    member_id VARCHAR(50) NOT NULL,
    group_number VARCHAR(50),
    group_name VARCHAR(100),
    coverage_type VARCHAR(20) NOT NULL,
    coverage_order VARCHAR(20) NOT NULL,
    effective_date DATE NOT NULL,
    termination_date DATE,
    subscriber_first_name VARCHAR(100),
    subscriber_last_name VARCHAR(100),
    subscriber_dob DATE,
    subscriber_ssn VARCHAR(11),
    subscriber_relationship VARCHAR(50),
    copay_amount VARCHAR(20),
    deductible VARCHAR(20),
    deductible_met VARCHAR(20),
    out_of_pocket_max VARCHAR(20),
    out_of_pocket_met VARCHAR(20),
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_verified_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_coverage_patient ON insurance_coverages(patient_id);
CREATE INDEX IF NOT EXISTS idx_coverage_member_id ON insurance_coverages(member_id);
CREATE INDEX IF NOT EXISTS idx_coverage_payer ON insurance_coverages(payer_id);

CREATE TABLE IF NOT EXISTS lab_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(30) UNIQUE NOT NULL,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    encounter_id BIGINT REFERENCES encounters(id),
    ordering_provider_id BIGINT NOT NULL REFERENCES providers(id),
    order_date_time TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    test_code VARCHAR(20) NOT NULL,
    test_name VARCHAR(200) NOT NULL,
    clinical_indication VARCHAR(500),
    icd10_code VARCHAR(20),
    specimen_type VARCHAR(200),
    specimen_collected_at TIMESTAMP,
    collected_by VARCHAR(100),
    specimen_received_at TIMESTAMP,
    lab_location VARCHAR(50),
    special_instructions VARCHAR(500),
    fasting BOOLEAN NOT NULL DEFAULT FALSE,
    stat BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_lab_order_patient ON lab_orders(patient_id);
CREATE INDEX IF NOT EXISTS idx_lab_order_provider ON lab_orders(ordering_provider_id);
CREATE INDEX IF NOT EXISTS idx_lab_order_status ON lab_orders(status);

CREATE TABLE IF NOT EXISTS lab_results (
    id BIGSERIAL PRIMARY KEY,
    lab_order_id BIGINT NOT NULL REFERENCES lab_orders(id),
    result_code VARCHAR(20) NOT NULL,
    result_name VARCHAR(200) NOT NULL,
    value VARCHAR(100),
    numeric_value DECIMAL(10,4),
    unit VARCHAR(50),
    reference_range VARCHAR(100),
    flag VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    result_date_time TIMESTAMP,
    interpretation VARCHAR(500),
    comments VARCHAR(500),
    performing_lab VARCHAR(100),
    verified_by VARCHAR(100),
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_lab_result_order ON lab_results(lab_order_id);
CREATE INDEX IF NOT EXISTS idx_lab_result_code ON lab_results(result_code);

CREATE TABLE IF NOT EXISTS audit_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50),
    entity_type VARCHAR(100),
    entity_id VARCHAR(100),
    user_id VARCHAR(100) NOT NULL,
    username VARCHAR(100),
    user_name VARCHAR(100),
    patient_id BIGINT,
    patient_mrn VARCHAR(20),
    action VARCHAR(30) NOT NULL,
    resource_type VARCHAR(100),
    resource_id VARCHAR(100),
    description VARCHAR(500),
    ip_address VARCHAR(50),
    user_agent VARCHAR(200),
    session_id VARCHAR(100),
    details TEXT,
    request_details TEXT,
    response_details TEXT,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_events(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_patient ON audit_events(patient_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_events(action);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_events(timestamp);

-- Flyway schema history (so Flyway doesn't try to re-run migrations)
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INTEGER NOT NULL PRIMARY KEY,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INTEGER,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INTEGER NOT NULL,
    success BOOLEAN NOT NULL
);

INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success) VALUES
(1, '1', 'initial schema', 'SQL', 'V1__initial_schema.sql', 0, 'seed', 0, true),
(2, '2', 'seed data', 'SQL', 'V2__seed_data.sql', 0, 'seed', 0, true)
ON CONFLICT (installed_rank) DO NOTHING;

-- =============================================================================
-- SEED DATA (from V2__seed_data.sql)
-- =============================================================================

-- Providers
INSERT INTO providers (npi, first_name, last_name, credentials, provider_type, specialty, department, email, phone_office, active, created_at, updated_at, version) VALUES
('1234567890', 'Sarah', 'Chen', 'MD, FACP', 'PHYSICIAN', 'Internal Medicine', 'Primary Care', 'sarah.chen@medchart.local', '555-0101', true, '2019-03-15 09:00:00', '2024-01-10 14:30:00', 0),
('1234567891', 'Michael', 'Rodriguez', 'MD', 'PHYSICIAN', 'Cardiology', 'Cardiology', 'michael.rodriguez@medchart.local', '555-0102', true, '2019-06-22 10:00:00', '2024-02-15 11:00:00', 0),
('1234567892', 'Jennifer', 'Williams', 'MD, PhD', 'PHYSICIAN', 'Oncology', 'Oncology', 'jennifer.williams@medchart.local', '555-0103', true, '2020-01-08 08:30:00', '2024-03-01 09:15:00', 0),
('1234567893', 'David', 'Kim', 'DO', 'PHYSICIAN', 'Family Medicine', 'Primary Care', 'david.kim@medchart.local', '555-0104', true, '2020-04-12 11:00:00', '2024-01-20 16:45:00', 0),
('1234567894', 'Emily', 'Johnson', 'MD', 'PHYSICIAN', 'Pediatrics', 'Pediatrics', 'emily.johnson@medchart.local', '555-0105', true, '2021-02-28 09:30:00', '2024-02-28 10:00:00', 0),
('1234567895', 'Robert', 'Thompson', 'MD, FACS', 'PHYSICIAN', 'General Surgery', 'Surgery', 'robert.thompson@medchart.local', '555-0106', true, '2018-11-05 14:00:00', '2023-12-15 08:30:00', 0),
('1234567896', 'Lisa', 'Martinez', 'NP', 'NURSE_PRACTITIONER', 'Family Medicine', 'Primary Care', 'lisa.martinez@medchart.local', '555-0107', true, '2022-03-10 10:30:00', '2024-03-10 15:00:00', 0),
('1234567897', 'James', 'Anderson', 'MD', 'PHYSICIAN', 'Psychiatry', 'Behavioral Health', 'james.anderson@medchart.local', '555-0108', false, '2019-08-20 13:00:00', '2023-06-30 17:00:00', 0)
ON CONFLICT (npi) DO NOTHING;

-- Patients
INSERT INTO patients (mrn, first_name, last_name, middle_name, date_of_birth, gender, ssn, email, phone, street1, street2, city, state, zip_code, emergency_contact_name, emergency_contact_phone, primary_provider_id, insurance_id, active, created_at, updated_at, version) VALUES
('MRN-2019-00001', 'John', 'Smith', 'Robert', '1965-03-15', 'MALE', '123-45-6789', 'john.smith@email.com', '555-1001', '123 Oak Street', 'Apt 4B', 'Springfield', 'IL', '62701', 'Mary Smith', '555-1002', 1, 'INS-BC-001', true, '2019-03-20 10:00:00', '2024-01-15 09:30:00', 0),
('MRN-2019-00002', 'Maria', 'Garcia', 'Elena', '1978-07-22', 'FEMALE', '234-56-7890', 'maria.garcia@email.com', '555-1003', '456 Maple Avenue', NULL, 'Springfield', 'IL', '62702', 'Carlos Garcia', '555-1004', 4, 'INS-AE-002', true, '2019-04-10 14:30:00', '2024-02-20 11:15:00', 0),
('MRN-2019-00003', 'William', 'Johnson', NULL, '1952-11-08', 'MALE', '345-67-8901', 'wjohnson@email.com', '555-1005', '789 Pine Road', 'Suite 100', 'Springfield', 'IL', '62703', 'Susan Johnson', '555-1006', 2, 'INS-MC-003', true, '2019-05-15 09:00:00', '2024-03-01 14:00:00', 0),
('MRN-2020-00004', 'Emily', 'Brown', 'Anne', '1990-02-28', 'FEMALE', '456-78-9012', 'emily.brown@email.com', '555-1007', '321 Elm Street', NULL, 'Springfield', 'IL', '62704', 'Thomas Brown', '555-1008', 1, 'INS-UH-004', true, '2020-01-08 11:30:00', '2024-01-25 10:45:00', 0),
('MRN-2020-00005', 'James', 'Davis', 'Michael', '1985-09-12', 'MALE', '567-89-0123', 'jdavis85@email.com', '555-1009', '654 Cedar Lane', 'Apt 12', 'Springfield', 'IL', '62705', 'Jennifer Davis', '555-1010', 4, 'INS-BC-005', true, '2020-03-22 15:00:00', '2024-02-10 16:30:00', 0),
('MRN-2020-00006', 'Patricia', 'Miller', 'Lynn', '1970-06-05', 'FEMALE', '678-90-1234', 'pmiller@email.com', '555-1011', '987 Birch Court', NULL, 'Springfield', 'IL', '62706', 'Robert Miller', '555-1012', 3, 'INS-AE-006', true, '2020-06-18 08:45:00', '2024-03-05 13:20:00', 0),
('MRN-2021-00007', 'Christopher', 'Wilson', NULL, '1988-12-20', 'MALE', '789-01-2345', 'cwilson@email.com', '555-1013', '147 Walnut Drive', 'Unit 3', 'Springfield', 'IL', '62707', 'Amanda Wilson', '555-1014', 1, 'INS-CI-007', true, '2021-02-14 10:15:00', '2024-01-30 09:00:00', 0),
('MRN-2021-00008', 'Jessica', 'Taylor', 'Marie', '1995-04-18', 'FEMALE', '890-12-3456', 'jtaylor95@email.com', '555-1015', '258 Spruce Way', NULL, 'Springfield', 'IL', '62708', 'Mark Taylor', '555-1016', 5, 'INS-UH-008', true, '2021-05-30 13:45:00', '2024-02-28 11:30:00', 0),
('MRN-2022-00009', 'Daniel', 'Anderson', 'Lee', '1960-08-30', 'MALE', '901-23-4567', 'danderson@email.com', '555-1017', '369 Ash Boulevard', 'Apt 7A', 'Springfield', 'IL', '62709', 'Karen Anderson', '555-1018', 2, 'INS-MC-009', true, '2022-01-12 09:30:00', '2024-03-10 15:45:00', 0),
('MRN-2022-00010', 'Sarah', 'Thomas', NULL, '1982-01-25', 'FEMALE', '012-34-5678', 'sthomas82@email.com', '555-1019', '741 Hickory Street', NULL, 'Springfield', 'IL', '62710', 'David Thomas', '555-1020', 4, 'INS-BC-010', true, '2022-04-05 14:00:00', '2024-01-18 10:15:00', 0),
('MRN-2023-00011', 'Matthew', 'Jackson', 'James', '1975-10-10', 'MALE', '123-45-0011', 'mjackson@email.com', '555-1021', '852 Poplar Avenue', 'Suite 5', 'Springfield', 'IL', '62711', 'Linda Jackson', '555-1022', 1, 'INS-AE-011', true, '2023-02-20 11:00:00', '2024-02-15 14:30:00', 0),
('MRN-2023-00012', 'Ashley', 'White', 'Nicole', '1992-05-14', 'FEMALE', '234-56-0012', 'awhite92@email.com', '555-1023', '963 Willow Lane', NULL, 'Springfield', 'IL', '62712', 'Brian White', '555-1024', 7, 'INS-CI-012', true, '2023-06-08 16:30:00', '2024-03-01 09:45:00', 0),
('MRN-2019-00013', 'Robert', 'Harris', NULL, '1948-03-02', 'MALE', '345-67-0013', 'rharris@email.com', '555-1025', '159 Chestnut Road', 'Apt 2C', 'Springfield', 'IL', '62713', 'Dorothy Harris', '555-1026', 2, 'INS-MC-013', false, '2019-07-25 10:30:00', '2023-11-20 08:00:00', 0),
('MRN-2024-00014', 'Megan', 'Martin', 'Rose', '2018-11-30', 'FEMALE', NULL, 'martin.family@email.com', '555-1027', '357 Sycamore Circle', NULL, 'Springfield', 'IL', '62714', 'Kevin Martin', '555-1028', 5, 'INS-BC-014', true, '2024-01-05 09:15:00', '2024-03-12 11:00:00', 0),
('MRN-2024-00015', 'Kevin', 'Lee', 'Andrew', '1998-07-08', 'MALE', '456-78-0015', 'klee98@email.com', '555-1029', '468 Magnolia Drive', 'Unit 8', 'Springfield', 'IL', '62715', 'Susan Lee', '555-1030', 4, 'INS-UH-015', true, '2024-02-10 14:45:00', '2024-03-08 16:00:00', 0)
ON CONFLICT (mrn) DO NOTHING;

-- Encounters
INSERT INTO encounters (encounter_number, patient_id, attending_provider_id, encounter_type, status, encounter_date, encounter_date_time, chief_complaint, notes, location, created_at, updated_at, version) VALUES
('ENC-2024-000001', 1, 1, 'OFFICE_VISIT', 'COMPLETED', '2024-01-15', '2024-01-15 09:00:00', 'Annual physical examination', 'Patient presents for routine annual physical. No acute complaints. BP slightly elevated at 142/88.', 'Main Campus - Room 101', '2024-01-15 09:00:00', '2024-01-15 10:30:00', 0),
('ENC-2024-000002', 2, 4, 'OFFICE_VISIT', 'COMPLETED', '2024-01-18', '2024-01-18 14:00:00', 'Follow-up for diabetes management', 'HbA1c improved to 7.2%. Continue current medication regimen. Discussed diet modifications.', 'Main Campus - Room 203', '2024-01-18 14:00:00', '2024-01-18 14:45:00', 0),
('ENC-2024-000003', 3, 2, 'OFFICE_VISIT', 'COMPLETED', '2024-01-22', '2024-01-22 10:30:00', 'Chest pain evaluation', 'Patient reports intermittent chest discomfort. EKG normal. Ordered stress test and lipid panel.', 'Cardiology Center - Room 305', '2024-01-22 10:30:00', '2024-01-22 11:45:00', 0),
('ENC-2024-000004', 4, 1, 'OFFICE_VISIT', 'COMPLETED', '2024-01-25', '2024-01-25 11:00:00', 'Upper respiratory infection', 'Symptoms for 5 days. Exam consistent with viral URI. Supportive care recommended.', 'Main Campus - Room 102', '2024-01-25 11:00:00', '2024-01-25 11:30:00', 0),
('ENC-2024-000005', 5, 4, 'OFFICE_VISIT', 'COMPLETED', '2024-02-01', '2024-02-01 09:30:00', 'Back pain', 'Lower back pain x 2 weeks. No radicular symptoms. Prescribed physical therapy and NSAIDs.', 'Main Campus - Room 201', '2024-02-01 09:30:00', '2024-02-01 10:15:00', 0),
('ENC-2024-000006', 6, 3, 'OFFICE_VISIT', 'COMPLETED', '2024-02-05', '2024-02-05 13:00:00', 'Oncology follow-up', 'Post-treatment surveillance. CT scan shows no evidence of recurrence. Continue monitoring.', 'Oncology Center - Room 410', '2024-02-05 13:00:00', '2024-02-05 14:00:00', 0),
('ENC-2024-000007', 7, 1, 'OFFICE_VISIT', 'COMPLETED', '2024-02-08', '2024-02-08 15:00:00', 'Medication refill', 'Routine visit for hypertension medication refill. BP well controlled at 128/82.', 'Main Campus - Room 103', '2024-02-08 15:00:00', '2024-02-08 15:20:00', 0),
('ENC-2024-000008', 8, 5, 'OFFICE_VISIT', 'COMPLETED', '2024-02-12', '2024-02-12 10:00:00', 'Well child visit', 'Routine pediatric wellness visit. Growth and development on track. Vaccines administered.', 'Pediatrics - Room 501', '2024-02-12 10:00:00', '2024-02-12 10:45:00', 0),
('ENC-2024-000009', 9, 2, 'OFFICE_VISIT', 'COMPLETED', '2024-02-15', '2024-02-15 11:30:00', 'Pacemaker check', 'Routine pacemaker interrogation. Device functioning normally. Battery life adequate.', 'Cardiology Center - Room 302', '2024-02-15 11:30:00', '2024-02-15 12:00:00', 0),
('ENC-2024-000010', 10, 4, 'OFFICE_VISIT', 'COMPLETED', '2024-02-20', '2024-02-20 14:30:00', 'Anxiety follow-up', 'Patient reports improved symptoms with current SSRI. Continue current dose.', 'Main Campus - Room 204', '2024-02-20 14:30:00', '2024-02-20 15:00:00', 0),
('ENC-2024-000011', 11, 1, 'OFFICE_VISIT', 'COMPLETED', '2024-02-25', '2024-02-25 09:00:00', 'Knee pain', 'Right knee pain x 3 months. X-ray shows moderate osteoarthritis. Discussed treatment options.', 'Main Campus - Room 105', '2024-02-25 09:00:00', '2024-02-25 09:45:00', 0),
('ENC-2024-000012', 12, 7, 'OFFICE_VISIT', 'COMPLETED', '2024-03-01', '2024-03-01 10:30:00', 'Prenatal visit', 'Routine prenatal visit at 20 weeks. Fetal heart tones normal. Anatomy scan scheduled.', 'Women Health - Room 601', '2024-03-01 10:30:00', '2024-03-01 11:15:00', 0),
('ENC-2024-000013', 1, 2, 'OFFICE_VISIT', 'COMPLETED', '2024-03-05', '2024-03-05 13:30:00', 'Cardiology referral', 'Referred for elevated BP. Echo ordered. Started on ACE inhibitor.', 'Cardiology Center - Room 301', '2024-03-05 13:30:00', '2024-03-05 14:30:00', 0),
('ENC-2024-000014', 14, 5, 'OFFICE_VISIT', 'COMPLETED', '2024-03-08', '2024-03-08 09:15:00', 'Ear infection', 'Left otitis media. Started on amoxicillin. Follow-up in 10 days if not improved.', 'Pediatrics - Room 502', '2024-03-08 09:15:00', '2024-03-08 09:45:00', 0),
('ENC-2024-000015', 15, 4, 'OFFICE_VISIT', 'IN_PROGRESS', '2024-03-12', '2024-03-12 14:00:00', 'New patient visit', 'Establishing care. Complete history and physical in progress.', 'Main Campus - Room 202', '2024-03-12 14:00:00', '2024-03-12 14:00:00', 0),
('ENC-2024-000016', 3, 2, 'OFFICE_VISIT', 'SCHEDULED', '2024-03-20', '2024-03-20 10:00:00', 'Stress test follow-up', NULL, 'Cardiology Center - Room 303', '2024-03-01 08:00:00', '2024-03-01 08:00:00', 0),
('ENC-2024-000017', 6, 3, 'OFFICE_VISIT', 'SCHEDULED', '2024-03-25', '2024-03-25 13:00:00', '6-month oncology surveillance', NULL, 'Oncology Center - Room 411', '2024-02-25 09:00:00', '2024-02-25 09:00:00', 0),
('ENC-2024-000018', 2, 4, 'OFFICE_VISIT', 'SCHEDULED', '2024-03-28', '2024-03-28 14:00:00', 'Diabetes quarterly check', NULL, 'Main Campus - Room 205', '2024-03-01 10:00:00', '2024-03-01 10:00:00', 0)
ON CONFLICT (encounter_number) DO NOTHING;

-- Diagnoses
INSERT INTO diagnoses (patient_id, icd10_code, description, status, severity, onset_date, diagnosed_by, chronic, principal, created_at, version) VALUES
(1, 'I10', 'Essential (primary) hypertension', 'ACTIVE', 'MODERATE', '2019-03-20', 1, true, true, '2019-03-20 10:30:00', 0),
(1, 'E78.5', 'Hyperlipidemia, unspecified', 'ACTIVE', 'MILD', '2020-01-15', 1, true, false, '2020-01-15 11:00:00', 0),
(2, 'E11.9', 'Type 2 diabetes mellitus without complications', 'ACTIVE', 'MODERATE', '2019-04-10', 4, true, true, '2019-04-10 15:00:00', 0),
(3, 'I25.10', 'Atherosclerotic heart disease of native coronary artery', 'ACTIVE', 'MODERATE', '2019-05-15', 2, true, true, '2019-05-15 10:00:00', 0),
(3, 'I48.91', 'Unspecified atrial fibrillation', 'ACTIVE', 'MODERATE', '2020-06-20', 2, true, false, '2020-06-20 14:00:00', 0),
(5, 'M54.5', 'Low back pain', 'ACTIVE', 'MILD', '2024-02-01', 4, false, true, '2024-02-01 10:00:00', 0),
(6, 'C50.919', 'Malignant neoplasm of unspecified site of unspecified female breast', 'RESOLVED', 'SEVERE', '2022-08-15', 3, false, true, '2022-08-15 09:00:00', 0),
(7, 'I10', 'Essential (primary) hypertension', 'ACTIVE', 'MILD', '2021-02-14', 1, true, true, '2021-02-14 11:00:00', 0),
(9, 'I49.9', 'Cardiac arrhythmia, unspecified', 'ACTIVE', 'MODERATE', '2018-03-10', 2, true, true, '2018-03-10 13:00:00', 0),
(10, 'F41.1', 'Generalized anxiety disorder', 'ACTIVE', 'MILD', '2022-04-05', 4, true, true, '2022-04-05 14:30:00', 0),
(11, 'M17.11', 'Primary osteoarthritis, right knee', 'ACTIVE', 'MODERATE', '2024-02-25', 1, true, true, '2024-02-25 09:30:00', 0);

-- Allergies
INSERT INTO allergies (patient_id, allergy_type, allergen, severity, status, reaction, onset_date, verified, created_at, version) VALUES
(1, 'MEDICATION', 'Penicillin', 'SEVERE', 'ACTIVE', 'Anaphylaxis', '1990-01-01', true, '2019-03-20 10:00:00', 0),
(1, 'MEDICATION', 'Sulfa drugs', 'MODERATE', 'ACTIVE', 'Rash', '2005-06-15', true, '2019-03-20 10:00:00', 0),
(2, 'FOOD', 'Shellfish', 'SEVERE', 'ACTIVE', 'Hives, difficulty breathing', '1985-01-01', true, '2019-04-10 14:30:00', 0),
(3, 'MEDICATION', 'Aspirin', 'MODERATE', 'ACTIVE', 'GI bleeding', '2010-03-20', true, '2019-05-15 09:00:00', 0),
(4, 'ENVIRONMENTAL', 'Pollen', 'MILD', 'ACTIVE', 'Sneezing, runny nose', '2015-04-01', true, '2020-01-08 11:30:00', 0),
(5, 'MEDICATION', 'Codeine', 'MODERATE', 'ACTIVE', 'Nausea, vomiting', '2018-07-10', true, '2020-03-22 15:00:00', 0),
(8, 'FOOD', 'Peanuts', 'SEVERE', 'ACTIVE', 'Anaphylaxis', '2021-05-30', true, '2021-05-30 13:45:00', 0),
(12, 'MEDICATION', 'Ibuprofen', 'MILD', 'ACTIVE', 'Stomach upset', '2020-01-15', true, '2023-06-08 16:30:00', 0);

-- Medications reference data
INSERT INTO medications (ndc_code, rxnorm_code, generic_name, brand_name, strength, form, route, controlled, active) VALUES
('00071015523', '314076', 'Lisinopril', 'Prinivil', '10 mg', 'Tablet', 'Oral', false, true),
('00071015540', '314077', 'Lisinopril', 'Prinivil', '20 mg', 'Tablet', 'Oral', false, true),
('00069154001', '860975', 'Metformin HCl', 'Glucophage', '500 mg', 'Tablet', 'Oral', false, true),
('00069154002', '860981', 'Metformin HCl', 'Glucophage', '1000 mg', 'Tablet', 'Oral', false, true),
('00074305014', '197361', 'Atorvastatin', 'Lipitor', '20 mg', 'Tablet', 'Oral', false, true),
('00074305020', '197361', 'Atorvastatin', 'Lipitor', '40 mg', 'Tablet', 'Oral', false, true),
('00093005801', '310798', 'Metoprolol Succinate', 'Toprol-XL', '50 mg', 'Tablet ER', 'Oral', false, true),
('00093005825', '310798', 'Metoprolol Succinate', 'Toprol-XL', '100 mg', 'Tablet ER', 'Oral', false, true),
('00093317701', '312938', 'Omeprazole', 'Prilosec', '20 mg', 'Capsule DR', 'Oral', false, true),
('00093317740', '312938', 'Omeprazole', 'Prilosec', '40 mg', 'Capsule DR', 'Oral', false, true),
('00378180501', '197591', 'Amlodipine', 'Norvasc', '5 mg', 'Tablet', 'Oral', false, true),
('00378180510', '197591', 'Amlodipine', 'Norvasc', '10 mg', 'Tablet', 'Oral', false, true),
('00093505601', '311354', 'Sertraline', 'Zoloft', '50 mg', 'Tablet', 'Oral', false, true),
('00093505650', '311354', 'Sertraline', 'Zoloft', '100 mg', 'Tablet', 'Oral', false, true),
('00781107001', '197318', 'Amoxicillin', 'Amoxil', '500 mg', 'Capsule', 'Oral', false, true)
;

-- Medication orders
INSERT INTO medication_orders (order_number, patient_id, medication_id, prescriber_id, order_date_time, status, dose, dose_unit, route, frequency, sig, start_date, quantity, refills, days_supply, created_at, updated_at, version) VALUES
('RX-2024-000001', 1, 2, 1, '2024-01-15 10:00:00', 'ACTIVE', '20', 'mg', 'Oral', 'Once daily', 'Take one tablet by mouth once daily', '2024-01-15', 90, 3, 90, '2024-01-15 10:00:00', '2024-01-15 10:00:00', 0),
('RX-2024-000002', 1, 5, 1, '2024-01-15 10:05:00', 'ACTIVE', '20', 'mg', 'Oral', 'Once daily at bedtime', 'Take one tablet by mouth at bedtime', '2024-01-15', 90, 3, 90, '2024-01-15 10:05:00', '2024-01-15 10:05:00', 0),
('RX-2024-000003', 2, 4, 4, '2024-01-18 14:30:00', 'ACTIVE', '1000', 'mg', 'Oral', 'Twice daily', 'Take one tablet by mouth twice daily with meals', '2024-01-18', 180, 3, 90, '2024-01-18 14:30:00', '2024-01-18 14:30:00', 0),
('RX-2024-000004', 3, 7, 2, '2024-01-22 11:30:00', 'ACTIVE', '50', 'mg', 'Oral', 'Once daily', 'Take one tablet by mouth once daily', '2024-01-22', 90, 3, 90, '2024-01-22 11:30:00', '2024-01-22 11:30:00', 0),
('RX-2024-000005', 7, 1, 1, '2024-02-08 15:15:00', 'ACTIVE', '10', 'mg', 'Oral', 'Once daily', 'Take one tablet by mouth once daily', '2024-02-08', 90, 3, 90, '2024-02-08 15:15:00', '2024-02-08 15:15:00', 0),
('RX-2024-000006', 10, 13, 4, '2024-02-20 14:45:00', 'ACTIVE', '50', 'mg', 'Oral', 'Once daily', 'Take one tablet by mouth once daily in the morning', '2024-02-20', 90, 3, 90, '2024-02-20 14:45:00', '2024-02-20 14:45:00', 0),
('RX-2024-000007', 14, 15, 5, '2024-03-08 09:30:00', 'ACTIVE', '500', 'mg', 'Oral', 'Three times daily', 'Take one capsule by mouth three times daily for 10 days', '2024-03-08', 30, 0, 10, '2024-03-08 09:30:00', '2024-03-08 09:30:00', 0)
ON CONFLICT (order_number) DO NOTHING;

-- Vitals
INSERT INTO vitals (patient_id, encounter_id, recorded_date_time, temperature_fahrenheit, heart_rate, respiratory_rate, systolic_bp, diastolic_bp, oxygen_saturation, height_inches, weight_pounds, bmi, pain_level, recorded_by, created_at) VALUES
(1, 1, '2024-01-15 09:05:00', 98.6, 72, 16, 142, 88, 98.0, 70, 195, 28.0, 0, 'nurse.jones', '2024-01-15 09:05:00'),
(2, 2, '2024-01-18 14:05:00', 98.4, 78, 14, 128, 82, 99.0, 64, 155, 26.6, 0, 'nurse.smith', '2024-01-18 14:05:00'),
(3, 3, '2024-01-22 10:35:00', 98.2, 68, 18, 138, 86, 97.0, 72, 210, 28.5, 3, 'nurse.jones', '2024-01-22 10:35:00'),
(4, 4, '2024-01-25 11:05:00', 100.2, 88, 20, 118, 76, 98.0, 66, 140, 22.6, 2, 'nurse.smith', '2024-01-25 11:05:00'),
(5, 5, '2024-02-01 09:35:00', 98.6, 74, 16, 122, 78, 99.0, 71, 185, 25.8, 6, 'nurse.jones', '2024-02-01 09:35:00'),
(6, 6, '2024-02-05 13:05:00', 98.4, 70, 14, 124, 80, 98.0, 65, 145, 24.1, 0, 'nurse.smith', '2024-02-05 13:05:00'),
(7, 7, '2024-02-08 15:05:00', 98.6, 68, 16, 128, 82, 99.0, 69, 175, 25.8, 0, 'nurse.jones', '2024-02-08 15:05:00'),
(8, 8, '2024-02-12 10:05:00', 98.8, 90, 22, 100, 65, 99.0, 48, 52, 16.2, 0, 'nurse.smith', '2024-02-12 10:05:00'),
(9, 9, '2024-02-15 11:35:00', 98.4, 62, 14, 132, 84, 97.0, 68, 180, 27.4, 0, 'nurse.jones', '2024-02-15 11:35:00'),
(10, 10, '2024-02-20 14:35:00', 98.6, 76, 16, 120, 78, 99.0, 67, 150, 23.5, 0, 'nurse.smith', '2024-02-20 14:35:00'),
(1, 13, '2024-03-05 13:35:00', 98.4, 70, 16, 136, 84, 98.0, 70, 192, 27.6, 0, 'nurse.jones', '2024-03-05 13:35:00'),
(14, 14, '2024-03-08 09:20:00', 100.8, 110, 24, 95, 60, 98.0, 38, 32, 15.6, 4, 'nurse.smith', '2024-03-08 09:20:00');

-- Audit events (sample)
INSERT INTO audit_events (event_type, entity_type, entity_id, user_id, username, action, ip_address, user_agent, details, created_at, timestamp) VALUES
('PATIENT_ACCESS', 'Patient', '1', 'user-001', 'sarah.chen', 'VIEW', '192.168.1.100', 'Mozilla/5.0 Chrome/120.0', '{"reason": "Treatment"}', '2024-01-15 09:05:00', '2024-01-15 09:05:00'),
('PATIENT_ACCESS', 'Patient', '2', 'user-002', 'david.kim', 'VIEW', '192.168.1.101', 'Mozilla/5.0 Chrome/120.0', '{"reason": "Treatment"}', '2024-01-18 14:02:00', '2024-01-18 14:02:00'),
('PATIENT_UPDATE', 'Patient', '1', 'user-001', 'sarah.chen', 'UPDATE', '192.168.1.100', 'Mozilla/5.0 Chrome/120.0', '{"fields": ["phone"]}', '2024-01-15 09:30:00', '2024-01-15 09:30:00'),
('MEDICATION_ORDER', 'MedicationOrder', '1', 'user-001', 'sarah.chen', 'CREATE', '192.168.1.100', 'Mozilla/5.0 Chrome/120.0', '{"medication": "Lisinopril 20mg"}', '2024-01-15 10:00:00', '2024-01-15 10:00:00'),
('ENCOUNTER_CREATE', 'Encounter', '1', 'user-001', 'sarah.chen', 'CREATE', '192.168.1.100', 'Mozilla/5.0 Chrome/120.0', '{"type": "OFFICE_VISIT"}', '2024-01-15 09:00:00', '2024-01-15 09:00:00');
