-- Refill requests schema and demo data

CREATE TABLE refill_requests (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    medication_id BIGINT NOT NULL REFERENCES medications(id),
    pharmacy_name VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_date DATE NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_refill_status ON refill_requests(status);
CREATE INDEX idx_refill_patient ON refill_requests(patient_id);
CREATE INDEX idx_refill_date ON refill_requests(requested_date);

-- Demo refill requests
INSERT INTO refill_requests (patient_id, medication_id, pharmacy_name, status, requested_date, notes, created_at, updated_at, version) VALUES
(1, 2, 'CVS Pharmacy', 'PENDING', '2024-03-15', 'Patient requested 90-day supply', '2024-03-15 08:00:00', '2024-03-15 08:00:00', 0),
(2, 4, 'Walgreens', 'PENDING', '2024-03-18', 'Due for refill this week', '2024-03-18 09:30:00', '2024-03-18 09:30:00', 0),
(3, 7, 'Rite Aid', 'PENDING', '2024-03-20', 'Pharmacy sent renewal request', '2024-03-20 11:15:00', '2024-03-20 11:15:00', 0),
(7, 1, 'CVS Pharmacy', 'APPROVED', '2024-03-10', 'Approved for 90-day supply', '2024-03-10 10:00:00', '2024-03-11 14:30:00', 0),
(10, 13, 'Walmart Pharmacy', 'DENIED', '2024-03-12', 'Requires prior authorization', '2024-03-12 13:45:00', '2024-03-13 09:00:00', 0);
