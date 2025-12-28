-- Table pour les rendez-vous
CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    date DATE NOT NULL,
    heure TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    motif VARCHAR(500),
    notes VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    INDEX idx_appointment_doctor (doctor_id),
    INDEX idx_appointment_patient (patient_id),
    INDEX idx_appointment_date (date),
    INDEX idx_appointment_status (status),
    INDEX idx_appointment_doctor_date_heure (doctor_id, date, heure)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

