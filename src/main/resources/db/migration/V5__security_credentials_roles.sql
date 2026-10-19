CREATE TABLE user_credentials (
 user_id BIGINT PRIMARY KEY REFERENCES users(id),
 password_hash VARCHAR(100) NOT NULL,
 CONSTRAINT bcrypt_hash CHECK (password_hash ~ '^\$2[aby]\$[0-9]{2}\$[./A-Za-z0-9]{53}$')
);
CREATE TABLE user_roles (
 user_id BIGINT NOT NULL REFERENCES users(id),
 role VARCHAR(10) NOT NULL CHECK (role IN ('USER','STAFF','ADMIN')),
 PRIMARY KEY (user_id, role)
);
-- Usuarios legados: sin credenciales ni privilegios hasta activación verificada.
