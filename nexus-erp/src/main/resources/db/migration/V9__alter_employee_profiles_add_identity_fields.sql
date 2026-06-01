-- Migration V9: Add Core HR identity and contract columns to employee_profiles table
ALTER TABLE employee_profiles
    ADD COLUMN cin VARCHAR(50) UNIQUE,
    ADD COLUMN adresse VARCHAR(255),
    ADD COLUMN contact VARCHAR(50),
    ADD COLUMN type_contrat VARCHAR(50),
    ADD COLUMN date_debut_contrat DATE,
    ADD COLUMN duree_contrat INT,
    ADD COLUMN hierarchie_id BIGINT,
    ADD COLUMN photo_url VARCHAR(255),
    ADD COLUMN signature_numerique VARCHAR(255),
    ADD CONSTRAINT fk_employee_profiles_manager FOREIGN KEY (hierarchie_id) REFERENCES users (id) ON DELETE SET NULL;
