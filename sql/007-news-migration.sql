-- 007-news-migration.sql
-- Migrate ARCHIVED status to UNPUBLISHED
UPDATE news SET status = 'UNPUBLISHED' WHERE status = 'ARCHIVED';
