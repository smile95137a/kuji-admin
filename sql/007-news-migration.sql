-- T002: Rename ARCHIVED status to UNPUBLISHED for news table
-- Run this migration before deploying the new code
UPDATE news SET status = 'UNPUBLISHED' WHERE status = 'ARCHIVED';
