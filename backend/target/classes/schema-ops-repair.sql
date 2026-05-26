-- 修复旧库中 ops_notification 使用自增 id 导致 Hibernate 主键为 null 的问题（开发环境每次启动重建）
DROP TABLE IF EXISTS ops_notification;

CREATE TABLE ops_notification (
  notify_code VARCHAR(20) NOT NULL PRIMARY KEY,
  msg_type VARCHAR(20) NOT NULL,
  msg_category VARCHAR(30),
  title VARCHAR(200) NOT NULL,
  intro VARCHAR(500),
  content CLOB,
  jump_type VARCHAR(20),
  inner_link_type VARCHAR(30),
  jump_url VARCHAR(500),
  detail_html CLOB,
  cover_images_json CLOB,
  audience_json CLOB,
  send_type INT NOT NULL DEFAULT 1,
  scheduled_time TIMESTAMP,
  generate_types_json CLOB,
  app_push BOOLEAN NOT NULL DEFAULT FALSE,
  publish_status INT NOT NULL DEFAULT 0,
  published_at TIMESTAMP,
  push_count INT NOT NULL DEFAULT 0,
  push_volume INT NOT NULL DEFAULT 0,
  click_count INT NOT NULL DEFAULT 0,
  receive_volume INT NOT NULL DEFAULT 0,
  push_user_text VARCHAR(200),
  estimated_users INT NOT NULL DEFAULT 0,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ops_notify_type ON ops_notification (msg_type);
CREATE INDEX IF NOT EXISTS idx_ops_notify_pub ON ops_notification (publish_status);
