-- T001: Add unique index on referral_record.user_id to enforce one referral per user
ALTER TABLE referral_record ADD UNIQUE INDEX idx_referral_record_user_id (user_id);
