-- Seed working test users with pre-hashed BCrypt passwords
-- admin123 -> $2a$12$MVTo1p4gFERuh/hHnqDs6OgkEV/6CWhhN3t2HPN8mcDL9HEcdQk/W
-- employee123 -> $2a$12$nQIxRIvMAqtTyk.KRk0KteAzTxEMANwbyxBJhcCnc89LUSuuI/bEy

INSERT INTO users (id, email, password_hash, first_name, last_name, is_active) VALUES
(1, 'admin@nexus.com', '$2a$12$MVTo1p4gFERuh/hHnqDs6OgkEV/6CWhhN3t2HPN8mcDL9HEcdQk/W', 'Admin', 'NEXUS', TRUE),
(2, 'employee@nexus.com', '$2a$12$nQIxRIvMAqtTyk.KRk0KteAzTxEMANwbyxBJhcCnc89LUSuuI/bEy', 'Jane', 'Doe', TRUE);

-- Map roles to users (based on V1 insertion IDs)
-- User 1 (admin@nexus.com) gets HR_ADMIN (id=3) and DIRECTION (id=5)
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 3),
(1, 5);

-- User 2 (employee@nexus.com) gets EMPLOYEE (id=1)
INSERT INTO user_roles (user_id, role_id) VALUES
(2, 1);
