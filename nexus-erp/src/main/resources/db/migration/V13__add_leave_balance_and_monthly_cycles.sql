-- Migration V13: Add leave_balance and create monthly_cycles table
ALTER TABLE employee_profiles
    ADD COLUMN leave_balance DOUBLE NOT NULL DEFAULT 30.0;

CREATE TABLE monthly_cycles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_profile_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    validated_as_worked BOOLEAN NOT NULL DEFAULT FALSE,
    processed_for_accrual BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_monthly_cycles_profile FOREIGN KEY (employee_profile_id) REFERENCES employee_profiles (id) ON DELETE CASCADE
);

CREATE INDEX idx_monthly_cycles_employee ON monthly_cycles(employee_profile_id);
