CREATE TABLE hr_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_profile_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_signed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_hr_documents_employee FOREIGN KEY (employee_profile_id) REFERENCES employee_profiles (id) ON DELETE CASCADE
);

CREATE INDEX idx_hr_documents_employee ON hr_documents(employee_profile_id);
