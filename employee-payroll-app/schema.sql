-- ============================================================
-- EMPLOYEE PAYROLL SYSTEM DATABASE SCHEMA
-- ============================================================

-- =========================
-- 1. USERS TABLE
-- =========================
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(64) NOT NULL, -- SHA-256 hash
                       email VARCHAR(100) UNIQUE NOT NULL,
                       role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
                       created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- UNIQUE INDEX for login optimization
CREATE UNIQUE INDEX idx_users_username ON users(username);

-- =========================
-- 2. EMPLOYEES TABLE
-- =========================
CREATE TABLE employees (
                           id SERIAL PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           profile_image VARCHAR(100) NOT NULL,
                           gender VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female')),
                           salary NUMERIC(10,2) NOT NULL CHECK (salary >= 0),
                           start_date DATE NOT NULL,
                           notes TEXT,
                           created_by INTEGER,
                           created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_created_by
                               FOREIGN KEY (created_by)
                                   REFERENCES users(id)
                                   ON DELETE SET NULL
);

-- =========================
-- 3. EMPLOYEE DEPARTMENTS (JUNCTION TABLE)
-- =========================
CREATE TABLE employee_departments (
                                      employee_id INTEGER NOT NULL,
                                      department VARCHAR(50) NOT NULL,

                                      PRIMARY KEY (employee_id, department),

                                      CONSTRAINT fk_emp
                                          FOREIGN KEY (employee_id)
                                              REFERENCES employees(id)
                                              ON DELETE CASCADE
);

-- Index for fast lookup
CREATE INDEX idx_emp_dept_id ON employee_departments(employee_id);

-- =========================
-- 4. PAYROLL AUDIT TABLE
-- =========================
CREATE TABLE payroll_audit (
                               id SERIAL PRIMARY KEY,
                               employee_id INTEGER NOT NULL,
                               action_type VARCHAR(10) NOT NULL, -- INSERT / UPDATE / DELETE
                               old_salary NUMERIC(10,2),
                               new_salary NUMERIC(10,2),
                               changed_by VARCHAR(50),
                               changed_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 5. TRIGGER FUNCTION
-- =========================
CREATE OR REPLACE FUNCTION log_salary_change()
RETURNS TRIGGER AS $$

BEGIN

    -- INSERT
    IF TG_OP = 'INSERT' THEN
        INSERT INTO payroll_audit(employee_id, action_type, old_salary, new_salary, changed_by)
        VALUES (NEW.id, 'INSERT', NULL, NEW.salary, current_user);

RETURN NEW;
END IF;

    -- UPDATE
    IF TG_OP = 'UPDATE' THEN

        IF OLD.salary <> NEW.salary THEN
            INSERT INTO payroll_audit(employee_id, action_type, old_salary, new_salary, changed_by)
            VALUES (NEW.id, 'UPDATE', OLD.salary, NEW.salary, current_user);
END IF;

RETURN NEW;
END IF;

    -- DELETE
    IF TG_OP = 'DELETE' THEN
        INSERT INTO payroll_audit(employee_id, action_type, old_salary, new_salary, changed_by)
        VALUES (OLD.id, 'DELETE', OLD.salary, NULL, current_user);

RETURN OLD;
END IF;

END;

$$ LANGUAGE plpgsql;

-- =========================
-- 6. TRIGGER
-- =========================
CREATE TRIGGER trg_log_salary_change
    AFTER INSERT OR UPDATE OR DELETE ON employees
    FOR EACH ROW
    EXECUTE FUNCTION log_salary_change();

-- =========================
-- 7. STORED FUNCTION
-- =========================
CREATE OR REPLACE FUNCTION get_total_payroll_by_dept(p_dept VARCHAR)
RETURNS NUMERIC AS $$

DECLARE total NUMERIC;

BEGIN

SELECT SUM(e.salary)
INTO total
FROM employees e
         JOIN employee_departments ed
              ON e.id = ed.employee_id
WHERE ed.department = p_dept;

RETURN COALESCE(total, 0);

END;

$$ LANGUAGE plpgsql;

-- =========================
-- 8. SEED DATA
-- =========================

-- Admin user (password = admin hashed placeholder)
INSERT INTO users(username, password, email, role)
VALUES (
           'admin',
           '8c6976e5b5410415bde908bd4dee15dfb16a6c7b7e1f3c8b1b7b2c8a9e9d8f1', -- SHA-256("admin")
           'admin@payroll.com',
           'ADMIN'
       );

-- Standard user (password = user hashed placeholder)
INSERT INTO users(username, password, email, role)
VALUES (
           'user',
           '04f8996da763b7a969b102d2d4c5f3c9d2f3c8b1b7b2c8a9e9d8f1a1b2c3d4e5', -- SHA-256("user")
           'user@payroll.com',
           'USER'
       );

-- Mock employee
INSERT INTO employees(name, profile_image, gender, salary, start_date, notes, created_by)
VALUES (
           'Amarpa Keerthi Kumar',
           'ellipse-1.png',
           'Male',
           75000.00,
           CURRENT_DATE,
           'Mock employee for testing',
           1
       );

-- Get employee id for mapping
INSERT INTO employee_departments(employee_id, department)
VALUES
    (1, 'Sales'),
    (1, 'HR'),
    (1, 'Finance');