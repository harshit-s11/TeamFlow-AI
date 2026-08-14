-- 1. Update tasks CHECK constraints to support IN_REVIEW status and URGENT priority
ALTER TABLE tasks DROP CONSTRAINT check_task_status;
ALTER TABLE tasks ADD CONSTRAINT check_task_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE'));

ALTER TABLE tasks DROP CONSTRAINT check_task_priority;
ALTER TABLE tasks ADD CONSTRAINT check_task_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'));

-- 2. Create task_activity_logs table for audit tracking
CREATE TABLE task_activity_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    task_id UUID REFERENCES tasks(id) ON DELETE SET NULL,
    actor_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    event_type VARCHAR(50) NOT NULL,
    field_changed VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Targeted indexes for audit activity queries
CREATE INDEX idx_task_activity_logs_project_id ON task_activity_logs(project_id);
CREATE INDEX idx_task_activity_logs_task_id ON task_activity_logs(task_id);
CREATE INDEX idx_task_activity_logs_created_at ON task_activity_logs(created_at);
