-- =========================================================================
-- REMOTE DESKTOP & ANYDESK-LIKE SESSION AUDITING MIGRATION
-- Database: Supabase PostgreSQL
-- =========================================================================

-- 1. Create remote_access_sessions table
CREATE TABLE IF NOT EXISTS remote_access_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) UNIQUE NOT NULL,
    device_id BIGINT,
    serial_number VARCHAR(100),
    admin_user_id BIGINT,
    admin_email VARCHAR(150),
    admin_name VARCHAR(150),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
    requested_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    started_at TIMESTAMP WITHOUT TIME ZONE,
    ended_at TIMESTAMP WITHOUT TIME ZONE,
    termination_reason VARCHAR(255),
    connection_type VARCHAR(50) DEFAULT 'WEBSOCKET_STREAM'
);

-- Indexes for fast lookup
CREATE INDEX IF NOT EXISTS idx_ras_session_id ON remote_access_sessions(session_id);
CREATE INDEX IF NOT EXISTS idx_ras_device_id ON remote_access_sessions(device_id);
CREATE INDEX IF NOT EXISTS idx_ras_serial_number ON remote_access_sessions(serial_number);
CREATE INDEX IF NOT EXISTS idx_ras_status ON remote_access_sessions(status);
CREATE INDEX IF NOT EXISTS idx_ras_requested_at ON remote_access_sessions(requested_at DESC);

-- 2. Create remote_access_audit table
CREATE TABLE IF NOT EXISTS remote_access_audit (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    device_id BIGINT,
    admin_user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    details VARCHAR(500),
    recorded_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for audit trail
CREATE INDEX IF NOT EXISTS idx_raa_session_id ON remote_access_audit(session_id);
CREATE INDEX IF NOT EXISTS idx_raa_device_id ON remote_access_audit(device_id);
CREATE INDEX IF NOT EXISTS idx_raa_admin_user_id ON remote_access_audit(admin_user_id);
CREATE INDEX IF NOT EXISTS idx_raa_recorded_at ON remote_access_audit(recorded_at DESC);
