CREATE TABLE employee_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    job_title VARCHAR(100),
    department VARCHAR(100),
    CONSTRAINT fk_employee_profile_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
