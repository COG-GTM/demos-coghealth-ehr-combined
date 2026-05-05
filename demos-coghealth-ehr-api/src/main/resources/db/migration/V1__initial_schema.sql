-- MedChart EHR Initial Schema
-- Version 1.0.0

CREATE TABLE patients (
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

CREATE INDEX idx_patient_mrn ON patients(mrn);
CREATE INDEX idx_patient_ssn ON patients(ssn);
CREATE INDEX idx_patient_last_name ON patients(last_name);

CREATE TABLE patient_identifiers (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    identifier_type VARCHAR(50) NOT NULL,
    identifier_value VARCHAR(100) NOT NULL,
    issuing_authority VARCHAR(100),
    effective_date DATE,
    expiration_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_patient_identifier_value ON patient_identifiers(identifier_value);
CREATE INDEX idx_patient_identifier_type ON patient_identifiers(identifier_type);

CREATE TABLE emergency_contacts (
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

CREATE TABLE providers (
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

CREATE INDEX idx_provider_npi ON providers(npi);
CREATE INDEX idx_provider_last_name ON providers(last_name);

CREATE TABLE provider_licenses (
    provider_id BIGINT NOT NULL REFERENCES providers(id),
    license_type VARCHAR(50),
    license_number VARCHAR(50),
    state VARCHAR(50),
    issue_date DATE,
    expiration_date DATE,
    status VARCHAR(20)
);

CREATE TABLE encounters (
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

CREATE INDEX idx_encounter_patient ON encounters(patient_id);
CREATE INDEX idx_encounter_provider ON encounters(attending_provider_id);
CREATE INDEX idx_encounter_date ON encounters(encounter_date_time);
CREATE INDEX idx_encounter_status ON encounters(status);

CREATE TABLE diagnoses (
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

CREATE INDEX idx_diagnosis_patient ON diagnoses(patient_id);
CREATE INDEX idx_diagnosis_icd10 ON diagnoses(icd10_code);
CREATE INDEX idx_diagnosis_status ON diagnoses(status);

CREATE TABLE encounter_diagnoses (
    id BIGSERIAL PRIMARY KEY,
    encounter_id BIGINT NOT NULL REFERENCES encounters(id),
    diagnosis_id BIGINT NOT NULL REFERENCES diagnoses(id),
    role VARCHAR(20),
    rank INTEGER NOT NULL DEFAULT 1,
    primary_diagnosis BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE allergies (
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

CREATE INDEX idx_allergy_patient ON allergies(patient_id);
CREATE INDEX idx_allergy_status ON allergies(status);

CREATE TABLE clinical_notes (
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

CREATE INDEX idx_note_patient ON clinical_notes(patient_id);
CREATE INDEX idx_note_encounter ON clinical_notes(encounter_id);
CREATE INDEX idx_note_author ON clinical_notes(author_id);
CREATE INDEX idx_note_type ON clinical_notes(note_type);

CREATE TABLE vitals (
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

CREATE INDEX idx_vitals_patient ON vitals(patient_id);
CREATE INDEX idx_vitals_encounter ON vitals(encounter_id);
CREATE INDEX idx_vitals_recorded ON vitals(recorded_date_time);

CREATE TABLE medications (
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

CREATE INDEX idx_medication_ndc ON medications(ndc_code);
CREATE INDEX idx_medication_rxnorm ON medications(rxnorm_code);
CREATE INDEX idx_medication_name ON medications(generic_name);

CREATE TABLE medication_orders (
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

CREATE INDEX idx_med_order_patient ON medication_orders(patient_id);
CREATE INDEX idx_med_order_provider ON medication_orders(prescriber_id);
CREATE INDEX idx_med_order_status ON medication_orders(status);
CREATE INDEX idx_med_order_date ON medication_orders(order_date_time);

CREATE TABLE medication_administrations (
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

CREATE INDEX idx_med_admin_patient ON medication_administrations(patient_id);
CREATE INDEX idx_med_admin_order ON medication_administrations(medication_order_id);
CREATE INDEX idx_med_admin_time ON medication_administrations(administered_date_time);

CREATE TABLE insurance_coverages (
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

CREATE INDEX idx_coverage_patient ON insurance_coverages(patient_id);
CREATE INDEX idx_coverage_member_id ON insurance_coverages(member_id);
CREATE INDEX idx_coverage_payer ON insurance_coverages(payer_id);

CREATE TABLE lab_orders (
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

CREATE INDEX idx_lab_order_patient ON lab_orders(patient_id);
CREATE INDEX idx_lab_order_provider ON lab_orders(ordering_provider_id);
CREATE INDEX idx_lab_order_status ON lab_orders(status);

CREATE TABLE lab_results (
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

CREATE INDEX idx_lab_result_order ON lab_results(lab_order_id);
CREATE INDEX idx_lab_result_code ON lab_results(result_code);

CREATE TABLE audit_events (
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

CREATE INDEX idx_audit_user ON audit_events(user_id);
CREATE INDEX idx_audit_patient ON audit_events(patient_id);
CREATE INDEX idx_audit_action ON audit_events(action);
CREATE INDEX idx_audit_timestamp ON audit_events(timestamp);
