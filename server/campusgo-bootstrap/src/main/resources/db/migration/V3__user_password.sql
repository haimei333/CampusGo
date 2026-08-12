-- 密码登录：BCrypt 哈希存储
ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(100);
