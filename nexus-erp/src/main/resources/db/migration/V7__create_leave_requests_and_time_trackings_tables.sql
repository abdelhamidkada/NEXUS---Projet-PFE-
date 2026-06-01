CREATE TABLE leave_requests (
    id VARCHAR(36) PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    reason VARCHAR(500),
    manager_comment VARCHAR(500),
    employee_profile_id BIGINT NOT NULL,
    CONSTRAINT fk_leave_requests_employee FOREIGN KEY (employee_profile_id) REFERENCES employee_profiles (id) ON DELETE CASCADE
);

CREATE INDEX idx_leave_requests_employee ON leave_requests(employee_profile_id);

CREATE TABLE time_trackings (
    id VARCHAR(36) PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    type VARCHAR(50) NOT NULL,
    latitude DOUBLE,
    longitude DOUBLE,
    employee_profile_id BIGINT NOT NULL,
    CONSTRAINT fk_time_trackings_employee FOREIGN KEY (employee_profile_id) REFERENCES employee_profiles (id) ON DELETE CASCADE
);

CREATE INDEX idx_time_trackings_employee ON time_trackings(employee_profile_id);
