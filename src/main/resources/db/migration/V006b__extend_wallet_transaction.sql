ALTER TABLE wallet_transaction ADD COLUMN IF NOT EXISTS gold_delta BIGINT;
ALTER TABLE wallet_transaction ADD COLUMN IF NOT EXISTS bonus_delta BIGINT;
ALTER TABLE wallet_transaction ADD COLUMN IF NOT EXISTS gold_after BIGINT;
ALTER TABLE wallet_transaction ADD COLUMN IF NOT EXISTS bonus_after BIGINT;
ALTER TABLE wallet_transaction ADD COLUMN IF NOT EXISTS reference_id VARCHAR(36);
ALTER TABLE wallet_transaction ADD COLUMN IF NOT EXISTS reason VARCHAR(500);
