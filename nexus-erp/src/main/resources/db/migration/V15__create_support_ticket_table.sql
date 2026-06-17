CREATE TABLE support_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    employee_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    deadline_sla TIMESTAMP,
    CONSTRAINT fk_support_tickets_employee FOREIGN KEY (employee_id) REFERENCES employee_profiles (id) ON DELETE CASCADE
);

CREATE INDEX idx_support_tickets_employee ON support_tickets (employee_id);
