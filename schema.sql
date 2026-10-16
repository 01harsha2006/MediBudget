-- ====================================================================
-- MediBudget Database Initialization Script
-- Medical Expense Planner for Household Financial Protection
-- ====================================================================

CREATE DATABASE IF NOT EXISTS medibudget_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE medibudget_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    password_salt VARCHAR(64) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    department VARCHAR(100) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Family Members Table
CREATE TABLE IF NOT EXISTS family_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    gender VARCHAR(20) DEFAULT 'Not Specified',
    blood_group VARCHAR(10) DEFAULT 'Unknown',
    notes VARCHAR(255) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_family_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Household Budgets Table
CREATE TABLE IF NOT EXISTS budgets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    budget_month INT NOT NULL,
    budget_year INT NOT NULL,
    monthly_budget DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    alert_threshold_percent INT NOT NULL DEFAULT 80,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_budget_period UNIQUE (user_id, budget_month, budget_year),
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Medical Expenses Table
CREATE TABLE IF NOT EXISTS medical_expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    family_member_id INT NULL,
    patient_name VARCHAR(100) NOT NULL,
    category VARCHAR(60) NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    expense_date DATE NOT NULL,
    payment_mode VARCHAR(40) DEFAULT 'Cash',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expense_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_expense_family FOREIGN KEY (family_member_id)
        REFERENCES family_members(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 5. Treatment Plans Table
CREATE TABLE IF NOT EXISTS treatments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    family_member_id INT NULL,
    patient_name VARCHAR(100) NOT NULL,
    treatment_name VARCHAR(120) NOT NULL,
    hospital VARCHAR(150) NOT NULL,
    doctor_name VARCHAR(100) DEFAULT '',
    estimated_cost DECIMAL(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Planned',
    start_date DATE NULL,
    notes VARCHAR(255) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_treatment_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_treatment_family FOREIGN KEY (family_member_id)
        REFERENCES family_members(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 6. Health Insurance Policies Table
CREATE TABLE IF NOT EXISTS insurance_policies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    provider VARCHAR(100) NOT NULL,
    policy_number VARCHAR(80) NOT NULL,
    policy_type VARCHAR(60) DEFAULT 'Health Floater',
    coverage_amount DECIMAL(12, 2) NOT NULL,
    premium_amount DECIMAL(12, 2) DEFAULT 0.00,
    expiry_date DATE NULL,
    notes VARCHAR(255) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_policy_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Health Insurance Claims Table
CREATE TABLE IF NOT EXISTS insurance_claims (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    policy_id INT NULL,
    family_member_id INT NULL,
    patient_name VARCHAR(100) NOT NULL,
    claim_amount DECIMAL(12, 2) NOT NULL,
    settled_amount DECIMAL(12, 2) DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'Submitted',
    claim_date DATE NOT NULL,
    remarks VARCHAR(255) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_claim_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_claim_policy FOREIGN KEY (policy_id)
        REFERENCES insurance_policies(id) ON DELETE SET NULL,
    CONSTRAINT fk_claim_family FOREIGN KEY (family_member_id)
        REFERENCES family_members(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 8. Reminders Table
CREATE TABLE IF NOT EXISTS reminders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    family_member_id INT NULL,
    patient_name VARCHAR(100) NOT NULL,
    reminder_type VARCHAR(60) NOT NULL,
    description VARCHAR(255) NOT NULL,
    reminder_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminder_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_family FOREIGN KEY (family_member_id)
        REFERENCES family_members(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ====================================================================
-- OPTIONAL SAMPLE DEMO DATA (COMMENTED OUT BY DEFAULT)
-- Uncomment to load initial test fixtures if needed.
-- ====================================================================
/*
-- Password hash for demo user 'harsha' (password: '1234')
-- Salt: a1b2c3d4e5f67890
-- Hash (SHA-256): 03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4 (raw demo)
INSERT INTO users (id, username, password_hash, password_salt, full_name, department)
VALUES (1, 'demo', '735b5a26c8b9b478c946be48b7fca1f3b2dbf77c3855ff48cb6d5b00c3b0dfb2', '8f12a9c3b4d5e6f7', 'Harsha Vardhan', 'Computer Science');

INSERT INTO family_members (id, user_id, name, age, relationship, gender, bloodGroup)
VALUES 
(1, 1, 'Harsha Vardhan', 21, 'Self', 'Male', 'O+'),
(2, 1, 'Lakshmi (Mother)', 48, 'Mother', 'Female', 'B+'),
(3, 1, 'Ramesh (Father)', 54, 'Father', 'Male', 'A+');

INSERT INTO budgets (user_id, budget_month, budget_year, monthly_budget, alert_threshold_percent)
VALUES (1, MONTH(CURRENT_DATE()), YEAR(CURRENT_DATE()), 25000.00, 80);

INSERT INTO medical_expenses (user_id, family_member_id, patient_name, category, description, amount, expense_date, payment_mode)
VALUES
(1, 3, 'Ramesh (Father)', 'Doctor Consultation', 'Cardiologist routine checkup', 1200.00, CURRENT_DATE(), 'UPI'),
(1, 2, 'Lakshmi (Mother)', 'Pharmacy', 'Monthly BP and diabetes medicines', 3450.00, CURRENT_DATE(), 'Card'),
(1, 1, 'Harsha Vardhan', 'Dental', 'Routine dental cleaning', 1500.00, CURRENT_DATE(), 'Cash');

INSERT INTO treatments (user_id, family_member_id, patient_name, treatment_name, hospital, doctor_name, estimated_cost, status, start_date)
VALUES (1, 3, 'Ramesh (Father)', 'Knee Physiotherapy', 'Apollo Hospital', 'Dr. Sharma', 8500.00, 'Ongoing', CURRENT_DATE());

INSERT INTO insurance_policies (id, user_id, provider, policy_number, policy_type, coverage_amount, premium_amount, expiry_date)
VALUES (1, 1, 'Star Health Allied', 'SH-98421034', 'Family Floater', 500000.00, 18500.00, DATE_ADD(CURRENT_DATE(), INTERVAL 6 MONTH));

INSERT INTO insurance_claims (user_id, policy_id, family_member_id, patient_name, claim_amount, settled_amount, status, claim_date, remarks)
VALUES (1, 1, 3, 'Ramesh (Father)', 8500.00, 0.00, 'Under Review', CURRENT_DATE(), 'Submitted OPD physiotherapy bill');

INSERT INTO reminders (user_id, family_member_id, patient_name, reminder_type, description, reminder_date, status)
VALUES
(1, 2, 'Lakshmi (Mother)', 'Doctor Appointment', 'Follow-up with Dr. Rao for blood sugar review', DATE_ADD(CURRENT_DATE(), INTERVAL 3 DAY), 'Pending'),
(1, 3, 'Ramesh (Father)', 'Medication Refill', 'Purchase next month blood pressure tablets', DATE_ADD(CURRENT_DATE(), INTERVAL 5 DAY), 'Pending');
*/
