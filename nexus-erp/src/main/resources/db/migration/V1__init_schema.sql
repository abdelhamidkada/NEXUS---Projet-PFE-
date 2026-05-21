CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cin VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Insertion des rôles natifs définis dans ton périmètre PFE
INSERT INTO roles (name) VALUES ('EMPLOYEE');

INSERT INTO roles (name) VALUES ('MANAGER');

INSERT INTO roles (name) VALUES ('HR_ADMIN');

INSERT INTO roles (name) VALUES ('IT_ADMIN');

INSERT INTO roles (name) VALUES ('DIRECTION');