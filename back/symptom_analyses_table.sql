-- Table principale pour les analyses de symptômes
CREATE TABLE IF NOT EXISTS symptom_analyses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    appointment_id BIGINT NULL,
    original_description TEXT NOT NULL,
    severity INT NULL,
    duration INT NULL,
    summary TEXT NULL,
    urgent_recommendation BOOLEAN NOT NULL DEFAULT FALSE,
    recommendation_message TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL,
    INDEX idx_symptom_analysis_patient (patient_id),
    INDEX idx_symptom_analysis_appointment (appointment_id),
    INDEX idx_symptom_analysis_created (created_at)
);

-- Table pour les symptômes extraits
CREATE TABLE IF NOT EXISTS symptom_analysis_symptoms (
    analysis_id BIGINT NOT NULL,
    symptom VARCHAR(200) NOT NULL,
    PRIMARY KEY (analysis_id, symptom),
    FOREIGN KEY (analysis_id) REFERENCES symptom_analyses(id) ON DELETE CASCADE
);

-- Table pour les red flags
CREATE TABLE IF NOT EXISTS symptom_analysis_red_flags (
    analysis_id BIGINT NOT NULL,
    red_flag VARCHAR(200) NOT NULL,
    PRIMARY KEY (analysis_id, red_flag),
    FOREIGN KEY (analysis_id) REFERENCES symptom_analyses(id) ON DELETE CASCADE
);

-- Table pour les spécialités suggérées
CREATE TABLE IF NOT EXISTS symptom_analysis_specialties (
    analysis_id BIGINT NOT NULL,
    specialty VARCHAR(50) NOT NULL,
    PRIMARY KEY (analysis_id, specialty),
    FOREIGN KEY (analysis_id) REFERENCES symptom_analyses(id) ON DELETE CASCADE
);

-- Table pour les questions de clarification
CREATE TABLE IF NOT EXISTS symptom_analysis_questions (
    analysis_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    FOREIGN KEY (analysis_id) REFERENCES symptom_analyses(id) ON DELETE CASCADE
);

