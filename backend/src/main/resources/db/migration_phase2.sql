-- ====================================================================================
-- PHASE 2 DATABASE MIGRATION SCRIPT
-- Transactional execution with pre-validations and safe constraints
-- ====================================================================================

BEGIN;

-- ------------------------------------------------------------------------------------
-- STEP 1: Unique Constraints Verification & Creation
-- ------------------------------------------------------------------------------------

-- A. Unique constraint on roles(role_name)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_roles_role_name'
    ) THEN
        ALTER TABLE roles ADD CONSTRAINT uk_roles_role_name UNIQUE (role_name);
    END IF;
END $$;

-- B. Unique constraint on permissions(permission_code)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_permissions_permission_code'
    ) THEN
        -- Verify no duplicates exist
        IF EXISTS (
            SELECT permission_code FROM permissions GROUP BY permission_code HAVING COUNT(*) > 1
        ) THEN
            RAISE EXCEPTION 'Cannot add unique constraint uk_permissions_permission_code: duplicate codes found.';
        END IF;

        ALTER TABLE permissions ADD CONSTRAINT uk_permissions_permission_code UNIQUE (permission_code);
    END IF;
END $$;

-- C. Unique constraint / primary key on role_permissions(permission_id, role_id)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conrelid = 'role_permissions'::regclass 
          AND contype IN ('p', 'u') 
          AND conname = 'role_permissions_pkey'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint WHERE conname = 'uk_role_permissions_role_perm'
        ) THEN
            ALTER TABLE role_permissions ADD CONSTRAINT uk_role_permissions_role_perm UNIQUE (permission_id, role_id);
        END IF;
    END IF;
END $$;

-- ------------------------------------------------------------------------------------
-- STEP 2: Ensure all 7 core modules exist in permissions
-- ------------------------------------------------------------------------------------
INSERT INTO permissions (permission_code, permission_name, module_name, description, created_at, updated_at)
VALUES 
    ('DASHBOARD', 'DASHBOARD', 'Dashboard', 'Overview dashboard with key asset metrics and status widgets', NOW(), NOW()),
    ('EMPLOYEES_DEVICES', 'EMPLOYEES_DEVICES', 'Employees & Devices', 'Manage employees, device assignments, and hardware details', NOW(), NOW()),
    ('ACCESSORIES', 'ACCESSORIES', 'Accessories', 'Manage peripheral assets and assignments', NOW(), NOW()),
    ('LICENSE_MANAGEMENT', 'LICENSE_MANAGEMENT', 'License Management', 'Manage software subscriptions, keys, and allocations', NOW(), NOW()),
    ('NOTIFICATIONS_VULNERABILITIES', 'NOTIFICATIONS_VULNERABILITIES', 'Notifications & Security', 'Security vulnerabilities and system alerts', NOW(), NOW()),
    ('ROLE_PERMISSIONS', 'ROLE_PERMISSIONS', 'Role Permissions', 'Configure roles and module access privileges', NOW(), NOW()),
    ('SETTINGS', 'SETTINGS', 'Settings', 'Application preferences and configuration', NOW(), NOW())
ON CONFLICT (permission_code) DO NOTHING;

-- Verification of the 7 modules
DO $$
DECLARE
    missing_count INT;
BEGIN
    SELECT COUNT(*) INTO missing_count
    FROM (
        VALUES 
            ('DASHBOARD'),
            ('EMPLOYEES_DEVICES'),
            ('ACCESSORIES'),
            ('LICENSE_MANAGEMENT'),
            ('NOTIFICATIONS_VULNERABILITIES'),
            ('ROLE_PERMISSIONS'),
            ('SETTINGS')
    ) AS expected(code)
    WHERE NOT EXISTS (
        SELECT 1 FROM permissions p WHERE p.permission_code = expected.code
    );

    IF missing_count > 0 THEN
        RAISE EXCEPTION 'Migration failed: % of the 7 core permission codes are missing from permissions table.', missing_count;
    END IF;
END $$;

-- ------------------------------------------------------------------------------------
-- STEP 3: Verify ADMIN and EMPLOYEE roles exist
-- ------------------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'ADMIN') THEN
        RAISE EXCEPTION 'Migration failed: Required role ADMIN does not exist in roles table.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'EMPLOYEE') THEN
        RAISE EXCEPTION 'Migration failed: Required role EMPLOYEE does not exist in roles table.';
    END IF;
END $$;

-- ------------------------------------------------------------------------------------
-- STEP 4: Extend role_permissions with data_scope column & constraints
-- ------------------------------------------------------------------------------------
ALTER TABLE role_permissions 
ADD COLUMN IF NOT EXISTS data_scope VARCHAR(20) DEFAULT 'OWN';

-- Backfill any NULL values to 'OWN'
UPDATE role_permissions 
SET data_scope = 'OWN' 
WHERE data_scope IS NULL;

-- Set default to 'OWN' and enforce NOT NULL
ALTER TABLE role_permissions 
ALTER COLUMN data_scope SET DEFAULT 'OWN';

ALTER TABLE role_permissions 
ALTER COLUMN data_scope SET NOT NULL;

-- Check constraint on data_scope
DO $$ 
BEGIN 
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_role_permissions_data_scope'
    ) THEN 
        ALTER TABLE role_permissions 
        ADD CONSTRAINT chk_role_permissions_data_scope 
        CHECK (data_scope IN ('ALL', 'OWN', 'ASSIGNED'));
    END IF; 
END $$;

-- ------------------------------------------------------------------------------------
-- STEP 5: Guarantee ADMIN role has WRITE access and ALL data scope for all modules
-- ------------------------------------------------------------------------------------
INSERT INTO role_permissions (permission_id, role_id, access_level, data_scope, assigned_at, updated_at)
SELECT 
    p.permission_id,
    r.role_id,
    'WRITE' AS access_level,
    'ALL' AS data_scope,
    NOW() AS assigned_at,
    NOW() AS updated_at
FROM permissions p
CROSS JOIN roles r
WHERE r.role_name = 'ADMIN'
ON CONFLICT (permission_id, role_id) 
DO UPDATE SET 
    access_level = 'WRITE',
    data_scope = 'ALL',
    updated_at = NOW();

-- ------------------------------------------------------------------------------------
-- STEP 6: Ensure EMPLOYEE role has OWN data scope
-- ------------------------------------------------------------------------------------
UPDATE role_permissions 
SET data_scope = 'OWN', updated_at = NOW() 
WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'EMPLOYEE') 
  AND data_scope IS DISTINCT FROM 'OWN';

-- ------------------------------------------------------------------------------------
-- STEP 7: Create employee_access_assignments table with distinct check constraint
-- ------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employee_access_assignments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    manager_user_id BIGINT NOT NULL,
    employee_user_id BIGINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_eaa_manager FOREIGN KEY (manager_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_eaa_employee FOREIGN KEY (employee_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uk_eaa_manager_employee UNIQUE (manager_user_id, employee_user_id),
    CONSTRAINT chk_eaa_distinct_users CHECK (manager_user_id <> employee_user_id)
);

CREATE INDEX IF NOT EXISTS idx_eaa_manager ON employee_access_assignments(manager_user_id);
CREATE INDEX IF NOT EXISTS idx_eaa_employee ON employee_access_assignments(employee_user_id);

-- ------------------------------------------------------------------------------------
-- STEP 8: Seed default IT_MANAGER role (DO NOT overwrite existing permissions)
-- ------------------------------------------------------------------------------------
INSERT INTO roles (role_name, description, is_system_role, created_at, updated_at)
SELECT 'IT_MANAGER', 'IT Manager with Assigned Employee Access', FALSE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'IT_MANAGER');

-- Insert default permissions for IT_MANAGER only if missing (DO NOTHING on conflict)
INSERT INTO role_permissions (permission_id, role_id, access_level, data_scope, assigned_at, updated_at)
SELECT 
    p.permission_id,
    r.role_id,
    CASE 
        WHEN p.permission_code IN ('ROLE_PERMISSIONS', 'SETTINGS') THEN 'HIDE'
        ELSE 'READ'
    END AS access_level,
    CASE 
        WHEN p.permission_code IN ('ROLE_PERMISSIONS', 'SETTINGS') THEN 'OWN'
        ELSE 'ASSIGNED'
    END AS data_scope,
    NOW() AS assigned_at,
    NOW() AS updated_at
FROM permissions p
CROSS JOIN roles r
WHERE r.role_name = 'IT_MANAGER'
ON CONFLICT (permission_id, role_id) DO NOTHING;

COMMIT;

