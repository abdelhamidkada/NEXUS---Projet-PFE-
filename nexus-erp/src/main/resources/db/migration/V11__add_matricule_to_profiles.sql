-- V11 : Ajout de la colonne matricule (identifiant métier unique)
-- Cette colonne permet d'identifier les employés avec un ID métier lisible (ex: E0001)

ALTER TABLE employee_profiles
    ADD COLUMN matricule VARCHAR(50) NULL UNIQUE COMMENT 'Identifiant metier unique de l employe';
