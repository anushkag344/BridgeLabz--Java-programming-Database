-- DROP OLD OBJECTS

DROP TRIGGER IF EXISTS trg_log_greeting_audit ON greetings;
DROP FUNCTION IF EXISTS log_greeting_audit();
DROP FUNCTION IF EXISTS get_user_greeting_count(VARCHAR);

DROP TABLE IF EXISTS greeting_audit;
DROP TABLE IF EXISTS greetings;
DROP TABLE IF EXISTS users;

-- USERS TABLE

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(64) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       role VARCHAR(20) NOT NULL
                           CHECK (role IN ('ADMIN','USER')),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- GREETINGS TABLE


CREATE TABLE greetings (
                           id SERIAL PRIMARY KEY,
                           message VARCHAR(255) NOT NULL,
                           created_by INTEGER
                               REFERENCES users(id)
                                   ON DELETE CASCADE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- AUDIT TABLE

CREATE TABLE greeting_audit (
                                id SERIAL PRIMARY KEY,
                                greeting_id INTEGER,
                                action_type VARCHAR(10),
                                old_message VARCHAR(255),
                                new_message VARCHAR(255),
                                changed_by VARCHAR(50),
                                changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- INDEXES

CREATE UNIQUE INDEX idx_users_username
    ON users(username);

CREATE INDEX idx_greetings_created_by
    ON greetings(created_by);


-- STORED FUNCTION


CREATE OR REPLACE FUNCTION get_user_greeting_count(
    p_username VARCHAR
)
RETURNS INTEGER AS
$$
DECLARE
v_count INTEGER;
    v_user_id INTEGER;
BEGIN

SELECT id
INTO v_user_id
FROM users
WHERE username = p_username;

IF v_user_id IS NULL THEN
        RETURN 0;
END IF;

SELECT COUNT(*)
INTO v_count
FROM greetings
WHERE created_by = v_user_id;

RETURN v_count;

END;
$$
LANGUAGE plpgsql;


-- TRIGGER FUNCTION


CREATE OR REPLACE FUNCTION log_greeting_audit()
RETURNS TRIGGER AS
$$
DECLARE
v_user VARCHAR(50);
BEGIN

    IF TG_OP = 'DELETE' THEN

SELECT username
INTO v_user
FROM users
WHERE id = OLD.created_by;

INSERT INTO greeting_audit(
    greeting_id,
    action_type,
    old_message,
    new_message,
    changed_by
)
VALUES(
          OLD.id,
          'DELETE',
          OLD.message,
          NULL,
          COALESCE(v_user,'UNKNOWN')
      );

RETURN OLD;

ELSIF TG_OP = 'UPDATE' THEN

SELECT username
INTO v_user
FROM users
WHERE id = NEW.created_by;

INSERT INTO greeting_audit(
    greeting_id,
    action_type,
    old_message,
    new_message,
    changed_by
)
VALUES(
          NEW.id,
          'UPDATE',
          OLD.message,
          NEW.message,
          COALESCE(v_user,'UNKNOWN')
      );

RETURN NEW;

ELSIF TG_OP = 'INSERT' THEN

SELECT username
INTO v_user
FROM users
WHERE id = NEW.created_by;

INSERT INTO greeting_audit(
    greeting_id,
    action_type,
    old_message,
    new_message,
    changed_by
)
VALUES(
          NEW.id,
          'INSERT',
          NULL,
          NEW.message,
          COALESCE(v_user,'UNKNOWN')
      );

RETURN NEW;

END IF;

RETURN NULL;

END;
$$
LANGUAGE plpgsql;


-- TRIGGER


CREATE TRIGGER trg_log_greeting_audit
    AFTER INSERT OR UPDATE OR DELETE
                    ON greetings
                        FOR EACH ROW
                        EXECUTE FUNCTION log_greeting_audit();


-- SEED DATA


INSERT INTO users(username,password,email,role)
VALUES(
        'admin_kiran',
        '240a8e10093414072234f9975224e75a13de4e6d6b797b5e40b0abf1d431c4f5',
        'kiran@example.com',
        'ADMIN'
    ),
    (
        'user_rohan',
        'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3',
        'rohan@example.com',
        'USER'
    );

INSERT INTO greetings
(message,created_by)
VALUES('Hello, welcome to our workspace app!',1),
    ('Learning PostgreSQL JDBC raw today!',2);