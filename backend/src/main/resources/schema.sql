CREATE TABLE IF NOT EXISTS sys_admin_user (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
  password VARCHAR(100) NOT NULL COMMENT 'BCrypt密码',
  nickname VARCHAR(50) DEFAULT NULL,
  phone VARCHAR(20) DEFAULT NULL,
  email VARCHAR(100) DEFAULT NULL,
  gender VARCHAR(10) DEFAULT 'unknown',
  avatar VARCHAR(255) DEFAULT NULL,
  role_name VARCHAR(50) DEFAULT '管理员',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理员';

CREATE TABLE IF NOT EXISTS dashboard_daily_metric (
  stat_date DATE NOT NULL PRIMARY KEY,
  order_count INT NOT NULL DEFAULT 0,
  new_user_count INT NOT NULL DEFAULT 0,
  pending_payment_count INT NOT NULL DEFAULT 0,
  sales_amount DECIMAL(14, 2) NOT NULL DEFAULT 0,
  yesterday_sales_amount DECIMAL(14, 2) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页日指标';

CREATE TABLE IF NOT EXISTS dashboard_pending_task (
  task_key VARCHAR(50) NOT NULL PRIMARY KEY,
  label VARCHAR(100) NOT NULL,
  count_value INT NOT NULL DEFAULT 0,
  sort_num INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待处理事务计数';

CREATE TABLE IF NOT EXISTS pms_category (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  parent_id BIGINT NOT NULL DEFAULT 0,
  level_num INT NOT NULL DEFAULT 1,
  product_count INT NOT NULL DEFAULT 0,
  unit VARCHAR(20) DEFAULT '件',
  visible BOOLEAN NOT NULL DEFAULT TRUE,
  sort_num INT NOT NULL DEFAULT 0,
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS pms_brand (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  initial_char VARCHAR(5),
  product_count INT NOT NULL DEFAULT 0,
  supplier VARCHAR(50),
  visible BOOLEAN NOT NULL DEFAULT TRUE,
  sort_num INT NOT NULL DEFAULT 0,
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS pms_product (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_no VARCHAR(20) NOT NULL UNIQUE,
  title VARCHAR(200) NOT NULL,
  subtitle VARCHAR(200),
  thumb VARCHAR(500),
  original_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
  discount_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
  status VARCHAR(10) NOT NULL DEFAULT 'off',
  audit_status VARCHAR(20) NOT NULL DEFAULT 'pending',
  remark VARCHAR(500),
  sku VARCHAR(50),
  sort_num INT NOT NULL DEFAULT 0,
  stock INT NOT NULL DEFAULT 0,
  month_sales INT NOT NULL DEFAULT 0,
  total_sales INT NOT NULL DEFAULT 0,
  supplier VARCHAR(50),
  category_code VARCHAR(50),
  brand_code VARCHAR(50),
  intro CLOB,
  shipping_template VARCHAR(20),
  stock_warning INT DEFAULT 0,
  unit VARCHAR(20),
  weight VARCHAR(20),
  pre_sale BOOLEAN DEFAULT FALSE,
  recommend_tags VARCHAR(200),
  service_tags VARCHAR(200),
  product_tags VARCHAR(200),
  main_images CLOB,
  white_image VARCHAR(500),
  video_url VARCHAR(500),
  detail_content CLOB,
  delivery_regions CLOB,
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS delivery_regions CLOB;

ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS intro CLOB;
ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS shipping_template VARCHAR(20);
ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS stock_warning INT DEFAULT 0;
ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS unit VARCHAR(20);
ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS weight VARCHAR(20);
ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS pre_sale BOOLEAN DEFAULT FALSE;
ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS recommend_tags VARCHAR(200);
ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS service_tags VARCHAR(200);
ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS product_tags VARCHAR(200);
ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS main_images CLOB;
ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS white_image VARCHAR(500);
ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS video_url VARCHAR(500);
ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS detail_content CLOB;

CREATE TABLE IF NOT EXISTS pms_product_comment (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_no VARCHAR(20) NOT NULL,
  rating VARCHAR(10) NOT NULL,
  content VARCHAR(1000),
  featured BOOLEAN NOT NULL DEFAULT FALSE,
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS oms_order (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(20) NOT NULL UNIQUE,
  product_name VARCHAR(200) NOT NULL,
  thumb VARCHAR(500),
  spec VARCHAR(100),
  quantity INT NOT NULL DEFAULT 1,
  actual_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
  discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
  freight DECIMAL(10, 2) NOT NULL DEFAULT 0,
  freight_free BOOLEAN NOT NULL DEFAULT FALSE,
  order_status VARCHAR(30) NOT NULL,
  ship_status VARCHAR(30) NOT NULL,
  logistics VARCHAR(200),
  pay_time TIMESTAMP,
  auto_confirm_time TIMESTAMP,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  receiver_name VARCHAR(50),
  receiver_phone VARCHAR(20),
  receiver_address VARCHAR(300),
  supplier VARCHAR(50),
  supplier_phone VARCHAR(50),
  user_account VARCHAR(100),
  pay_method VARCHAR(50),
  pay_method_detail VARCHAR(100),
  order_source VARCHAR(20),
  order_type VARCHAR(30),
  delivery_method VARCHAR(30),
  logistics_no VARCHAR(80),
  delivery_serial VARCHAR(50),
  auto_confirm_days_label VARCHAR(50),
  user_remark VARCHAR(500),
  platform_remark VARCHAR(500),
  invoice_type VARCHAR(30),
  invoice_status VARCHAR(30),
  invoice_attr VARCHAR(30),
  invoice_title VARCHAR(100),
  invoice_tax_no VARCHAR(50),
  invoice_content VARCHAR(100),
  invoice_email VARCHAR(100),
  product_sku VARCHAR(50),
  unit_price DECIMAL(10, 2),
  product_total DECIMAL(10, 2),
  coupon_amount DECIMAL(10, 2),
  payable_subtotal DECIMAL(10, 2),
  after_sales_status VARCHAR(30) NOT NULL DEFAULT 'none',
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS oms_after_sale (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  after_sale_no VARCHAR(20) NOT NULL UNIQUE,
  order_no VARCHAR(20) NOT NULL,
  product_name VARCHAR(200),
  thumb VARCHAR(500),
  order_status VARCHAR(30),
  ship_status VARCHAR(30),
  after_sale_status VARCHAR(30) NOT NULL,
  after_sale_type VARCHAR(30) NOT NULL,
  refund_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
  apply_time TIMESTAMP,
  process_time TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS oms_return_reason (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  reason_code VARCHAR(20) NOT NULL UNIQUE,
  reason_text VARCHAR(200) NOT NULL,
  add_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  visible BOOLEAN NOT NULL DEFAULT TRUE,
  sort_num INT NOT NULL DEFAULT 0,
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE oms_order_address ADD COLUMN IF NOT EXISTS province_code VARCHAR(20);
ALTER TABLE oms_order_address ADD COLUMN IF NOT EXISTS city_code VARCHAR(20);
ALTER TABLE oms_order_address ADD COLUMN IF NOT EXISTS district_code VARCHAR(20);

CREATE TABLE IF NOT EXISTS oms_express_template (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  template_code VARCHAR(20) NOT NULL UNIQUE,
  template_name VARCHAR(100) NOT NULL,
  express_company VARCHAR(50) NOT NULL,
  template_spec VARCHAR(50),
  remark VARCHAR(200),
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
  visible BOOLEAN NOT NULL DEFAULT TRUE,
  sort_num INT NOT NULL DEFAULT 0,
  add_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS sys_region (
  code VARCHAR(20) NOT NULL PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  parent_code VARCHAR(20) DEFAULT '0',
  level_num TINYINT NOT NULL
);

CREATE TABLE IF NOT EXISTS oms_order_address (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  address_code VARCHAR(20) NOT NULL UNIQUE,
  address_type VARCHAR(10) NOT NULL,
  contact_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  province VARCHAR(30),
  city VARCHAR(30),
  district VARCHAR(30),
  province_code VARCHAR(20),
  city_code VARCHAR(20),
  district_code VARCHAR(20),
  detail_address VARCHAR(300) NOT NULL,
  zip_code VARCHAR(10),
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
  visible BOOLEAN NOT NULL DEFAULT TRUE,
  sort_num INT NOT NULL DEFAULT 0,
  add_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);
