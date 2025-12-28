-- Table principale pour les dossiers médicaux
CREATE TABLE IF NOT EXISTS medical_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL UNIQUE,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    INDEX idx_medical_record_patient (patient_id),
    INDEX idx_medical_record_created (created_at)
);

-- Table pour les consultations/visites
CREATE TABLE IF NOT EXISTS visits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medical_record_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    visit_date DATE NOT NULL,
    visit_time TIME NULL,
    reason TEXT NULL,
    symptoms TEXT NULL,
    diagnosis TEXT NULL,
    treatment TEXT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (medical_record_id) REFERENCES medical_records(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    INDEX idx_visit_medical_record (medical_record_id),
    INDEX idx_visit_doctor (doctor_id),
    INDEX idx_visit_date (visit_date),
    INDEX idx_visit_created (created_at)
);

-- Table pour les pièces jointes (analyses, images, documents)
CREATE TABLE IF NOT EXISTS medical_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medical_record_id BIGINT NOT NULL,
    doctor_id BIGINT NULL,
    file_name VARCHAR(100) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    attachment_type VARCHAR(50) NOT NULL,
    description VARCHAR(500) NULL,
    file_size BIGINT NOT NULL,
    grid_fs_id VARCHAR(100) NULL,
    file_path VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medical_record_id) REFERENCES medical_records(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    INDEX idx_attachment_medical_record (medical_record_id),
    INDEX idx_attachment_type (attachment_type),
    INDEX idx_attachment_created (created_at)
);

