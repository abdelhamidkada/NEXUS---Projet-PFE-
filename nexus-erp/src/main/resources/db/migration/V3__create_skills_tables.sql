CREATE TABLE skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL
);

CREATE TABLE employee_skills (
    employee_profile_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    proficiency_level INT NOT NULL,
    PRIMARY KEY (employee_profile_id, skill_id),
    CONSTRAINT fk_employee_skills_profile FOREIGN KEY (employee_profile_id) REFERENCES employee_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE,
    CONSTRAINT chk_proficiency_level CHECK (proficiency_level BETWEEN 1 AND 5)
);
