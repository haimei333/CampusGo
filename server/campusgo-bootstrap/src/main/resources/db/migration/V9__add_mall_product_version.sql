-- Add optimistic locking version column to mall_product
ALTER TABLE mall_product ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 0;
