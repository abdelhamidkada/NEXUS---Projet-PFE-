-- V10 : Ajout des champs metiers enrichis au profil employe
-- Segregation Public [E] / Prive [P]

ALTER TABLE employee_profiles
    ADD COLUMN hire_date           DATE         NULL,
    ADD COLUMN work_model          VARCHAR(20)  NULL,
    ADD COLUMN pay_frequency       VARCHAR(30)  NULL,
    ADD COLUMN employment_fraction VARCHAR(30)  NULL,
    ADD COLUMN location            VARCHAR(100) NULL,
    ADD COLUMN seniority_level     VARCHAR(20)  NULL,
    ADD COLUMN spoken_languages    VARCHAR(150) NULL;
