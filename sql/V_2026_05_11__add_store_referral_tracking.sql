ALTER TABLE store
    ADD COLUMN IF NOT EXISTS referrer_store_id VARCHAR(36) NULL COMMENT '推薦該店家進駐的來源店家 ID' AFTER owner_id,
    ADD COLUMN IF NOT EXISTS referral_code_id VARCHAR(36) NULL COMMENT '店家進駐時使用的推薦碼 ID' AFTER referrer_store_id,
    ADD COLUMN IF NOT EXISTS activated_at DATETIME NULL COMMENT '店家啟用成功時間' AFTER status;

ALTER TABLE store
    ADD CONSTRAINT IF NOT EXISTS fk_store_referrer_store
        FOREIGN KEY (referrer_store_id) REFERENCES store(id) ON DELETE SET NULL;

ALTER TABLE store
    ADD CONSTRAINT IF NOT EXISTS fk_store_referral_code
        FOREIGN KEY (referral_code_id) REFERENCES referral_code(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_store_referrer_store_id ON store(referrer_store_id);
CREATE INDEX IF NOT EXISTS idx_store_referral_code_id ON store(referral_code_id);
CREATE INDEX IF NOT EXISTS idx_store_activated_at ON store(activated_at);