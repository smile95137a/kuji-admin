-- V017: Enum Cleanup - Normalize coin_type values to UPPERCASE
-- Merges PointType (lowercase codes) → CoinTypeEnum (UPPERCASE codes)

UPDATE wallet_transaction SET coin_type = UPPER(coin_type) WHERE coin_type IN ('gold', 'bonus');
UPDATE point_log SET point_type = UPPER(point_type) WHERE point_type IN ('gold', 'bonus');
