-- CampusGo 数据库表注释

-- 核心用户表
COMMENT ON TABLE app_user IS '用户表：存储用户基本信息、信用分、角色状态';
COMMENT ON TABLE wallet IS '钱包表：用户余额、冻结金额、收支统计（单位：分）';
COMMENT ON TABLE wallet_ledger IS '钱包流水表：记录每一笔资金变动明细';

-- 任务相关表
COMMENT ON TABLE task IS '任务表：跑腿任务的核心信息（类型、金额、状态、地址等）';
COMMENT ON TABLE task_group_member IS '任务拼单成员表：记录拼单任务的参与成员';
COMMENT ON TABLE task_reserve_slot IS '任务预约时段表：任务可预约的时间段';
COMMENT ON TABLE task_status_log IS '任务状态日志表：记录任务状态变更历史';

-- 聊天相关表
COMMENT ON TABLE chat_conversation IS '聊天会话表：用户之间的对话会话';
COMMENT ON TABLE chat_message IS '聊天消息表：会话中的具体消息记录';

-- 签到与积分表
COMMENT ON TABLE check_in_record IS '签到记录表：用户每日签到记录、连续天数、奖励积分';
COMMENT ON TABLE points_wallet IS '积分钱包表：用户积分余额和累计收入';
COMMENT ON TABLE points_transaction IS '积分流水表：积分收支明细记录';

-- 商城与兑换表
COMMENT ON TABLE mall_product IS '积分商城商品表：可用积分兑换的商品信息';
COMMENT ON TABLE redeem_record IS '兑换记录表：用户使用积分兑换商品的记录';

-- 评价与通知表
COMMENT ON TABLE review IS '评价表：任务完成后的用户评价';
COMMENT ON TABLE notification IS '通知表：系统推送给用户的消息通知';

-- 地址表
COMMENT ON TABLE user_address IS '用户地址表：用户的收货/取货地址';

-- 系统表
COMMENT ON TABLE flyway_schema_history IS 'Flyway 迁移历史表：记录数据库版本迁移历史（框架自动生成）';
