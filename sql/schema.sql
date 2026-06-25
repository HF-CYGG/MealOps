CREATE DATABASE IF NOT EXISTS reggie
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

SET NAMES utf8mb4;

USE reggie;

DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS payment_order;
DROP TABLE IF EXISTS order_detail;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS dining_cart_item;
DROP TABLE IF EXISTS dining_session_member;
DROP TABLE IF EXISTS dining_session;
DROP TABLE IF EXISTS dining_table;
DROP TABLE IF EXISTS address_book;
DROP TABLE IF EXISTS shopping_cart;
DROP TABLE IF EXISTS setmeal_dish;
DROP TABLE IF EXISTS setmeal;
DROP TABLE IF EXISTS dish_flavor;
DROP TABLE IF EXISTS dish;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS employee;

CREATE TABLE employee (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(32) NOT NULL COMMENT '登录账号',
  password VARCHAR(64) NOT NULL COMMENT '密码MD5',
  name VARCHAR(32) NOT NULL COMMENT '员工姓名',
  phone VARCHAR(16) DEFAULT NULL COMMENT '手机号',
  sex VARCHAR(2) DEFAULT NULL COMMENT '性别',
  id_number VARCHAR(32) DEFAULT NULL COMMENT '身份证号',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_user BIGINT DEFAULT NULL COMMENT '创建人',
  update_user BIGINT DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (id),
  UNIQUE KEY uk_employee_username (username),
  KEY idx_employee_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工';

CREATE TABLE user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  openid VARCHAR(64) DEFAULT NULL COMMENT '微信openid',
  name VARCHAR(50) DEFAULT NULL COMMENT '姓名',
  phone VARCHAR(16) NOT NULL COMMENT '手机号',
  sex VARCHAR(2) DEFAULT NULL COMMENT '性别',
  id_number VARCHAR(32) DEFAULT NULL COMMENT '身份证号',
  avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_phone (phone),
  KEY idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';

CREATE TABLE category (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  type TINYINT NOT NULL COMMENT '类型：1菜品分类，2套餐分类',
  name VARCHAR(64) NOT NULL COMMENT '分类名称',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_user BIGINT DEFAULT NULL COMMENT '创建人',
  update_user BIGINT DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (id),
  UNIQUE KEY uk_category_type_name (type, name),
  KEY idx_category_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类';

CREATE TABLE dish (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(64) NOT NULL COMMENT '菜品名称',
  category_id BIGINT NOT NULL COMMENT '分类ID',
  price DECIMAL(10,2) NOT NULL COMMENT '价格',
  code VARCHAR(64) DEFAULT NULL COMMENT '商品码',
  image VARCHAR(255) DEFAULT NULL COMMENT '图片',
  description VARCHAR(255) DEFAULT NULL COMMENT '描述',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1起售，0停售',
  stock INT NOT NULL DEFAULT 0 COMMENT '库存',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_user BIGINT DEFAULT NULL COMMENT '创建人',
  update_user BIGINT DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dish_name (name),
  KEY idx_dish_category (category_id),
  KEY idx_dish_status (status),
  KEY idx_dish_stock (stock),
  CONSTRAINT fk_dish_category FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品';

CREATE TABLE dish_flavor (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  dish_id BIGINT NOT NULL COMMENT '菜品ID',
  name VARCHAR(64) NOT NULL COMMENT '口味名称',
  value VARCHAR(255) NOT NULL COMMENT '口味值JSON',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_user BIGINT DEFAULT NULL COMMENT '创建人',
  update_user BIGINT DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (id),
  KEY idx_flavor_dish (dish_id),
  CONSTRAINT fk_flavor_dish FOREIGN KEY (dish_id) REFERENCES dish (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品口味';

CREATE TABLE setmeal (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  category_id BIGINT NOT NULL COMMENT '分类ID',
  name VARCHAR(64) NOT NULL COMMENT '套餐名称',
  price DECIMAL(10,2) NOT NULL COMMENT '价格',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1起售，0停售',
  code VARCHAR(64) DEFAULT NULL COMMENT '商品码',
  description VARCHAR(255) DEFAULT NULL COMMENT '描述',
  image VARCHAR(255) DEFAULT NULL COMMENT '图片',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_user BIGINT DEFAULT NULL COMMENT '创建人',
  update_user BIGINT DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (id),
  UNIQUE KEY uk_setmeal_name (name),
  KEY idx_setmeal_category (category_id),
  KEY idx_setmeal_status (status),
  CONSTRAINT fk_setmeal_category FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐';

CREATE TABLE setmeal_dish (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  setmeal_id BIGINT NOT NULL COMMENT '套餐ID',
  dish_id BIGINT NOT NULL COMMENT '菜品ID',
  name VARCHAR(64) NOT NULL COMMENT '菜品名称快照',
  price DECIMAL(10,2) NOT NULL COMMENT '菜品价格快照',
  copies INT NOT NULL DEFAULT 1 COMMENT '份数',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_user BIGINT DEFAULT NULL COMMENT '创建人',
  update_user BIGINT DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (id),
  KEY idx_setmeal_dish_setmeal (setmeal_id),
  KEY idx_setmeal_dish_dish (dish_id),
  CONSTRAINT fk_setmeal_dish_setmeal FOREIGN KEY (setmeal_id) REFERENCES setmeal (id) ON DELETE CASCADE,
  CONSTRAINT fk_setmeal_dish_dish FOREIGN KEY (dish_id) REFERENCES dish (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐菜品关系';

CREATE TABLE shopping_cart (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  dish_id BIGINT DEFAULT NULL COMMENT '菜品ID',
  setmeal_id BIGINT DEFAULT NULL COMMENT '套餐ID',
  dish_flavor VARCHAR(255) DEFAULT NULL COMMENT '菜品口味',
  name VARCHAR(64) NOT NULL COMMENT '商品名称快照',
  image VARCHAR(255) DEFAULT NULL COMMENT '图片快照',
  number INT NOT NULL DEFAULT 1 COMMENT '数量',
  amount DECIMAL(10,2) NOT NULL COMMENT '金额',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_cart_user (user_id),
  KEY idx_cart_dish (dish_id),
  KEY idx_cart_setmeal (setmeal_id),
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES user (id),
  CONSTRAINT fk_cart_dish FOREIGN KEY (dish_id) REFERENCES dish (id),
  CONSTRAINT fk_cart_setmeal FOREIGN KEY (setmeal_id) REFERENCES setmeal (id),
  CONSTRAINT chk_cart_item CHECK (
    (dish_id IS NOT NULL AND setmeal_id IS NULL) OR
    (dish_id IS NULL AND setmeal_id IS NOT NULL)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车';

CREATE TABLE address_book (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  consignee VARCHAR(50) NOT NULL COMMENT '收货人',
  sex VARCHAR(2) DEFAULT NULL COMMENT '性别',
  phone VARCHAR(16) NOT NULL COMMENT '手机号',
  province_code VARCHAR(16) DEFAULT NULL COMMENT '省级区划编号',
  province_name VARCHAR(32) DEFAULT NULL COMMENT '省级名称',
  city_code VARCHAR(16) DEFAULT NULL COMMENT '市级区划编号',
  city_name VARCHAR(32) DEFAULT NULL COMMENT '市级名称',
  district_code VARCHAR(16) DEFAULT NULL COMMENT '区级区划编号',
  district_name VARCHAR(32) DEFAULT NULL COMMENT '区级名称',
  detail VARCHAR(255) NOT NULL COMMENT '详细地址',
  label VARCHAR(32) DEFAULT NULL COMMENT '标签',
  is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认：1是，0否',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_address_user (user_id),
  KEY idx_address_default (user_id, is_default),
  CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地址簿';

CREATE TABLE dining_table (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  table_no VARCHAR(32) NOT NULL COMMENT '桌号',
  table_name VARCHAR(64) NOT NULL COMMENT '桌台名称',
  capacity INT NOT NULL DEFAULT 1 COMMENT '容纳人数',
  status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE/OCCUPIED/RESERVED/DISABLED',
  current_session_id BIGINT DEFAULT NULL COMMENT '当前堂食会话ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dining_table_no (table_no),
  KEY idx_dining_table_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='堂食桌台';

CREATE TABLE dining_session (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  table_id BIGINT NOT NULL COMMENT '桌台ID',
  table_name VARCHAR(64) NOT NULL COMMENT '桌台名称快照',
  creator_user_id BIGINT NOT NULL COMMENT '开台用户ID',
  party_size INT NOT NULL DEFAULT 1 COMMENT '就餐人数',
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/CLOSED',
  opened_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开台时间',
  closed_at DATETIME DEFAULT NULL COMMENT '关台时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_dining_session_table (table_id),
  KEY idx_dining_session_status (status, opened_at),
  KEY idx_dining_session_creator (creator_user_id),
  CONSTRAINT fk_dining_session_table FOREIGN KEY (table_id) REFERENCES dining_table (id),
  CONSTRAINT fk_dining_session_creator FOREIGN KEY (creator_user_id) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='堂食会话';

CREATE TABLE dining_session_member (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  session_id BIGINT NOT NULL COMMENT '会话ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  nickname VARCHAR(64) DEFAULT NULL COMMENT '成员昵称',
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/LEFT',
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  left_at DATETIME DEFAULT NULL COMMENT '离开时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_session_member_user (session_id, user_id),
  KEY idx_session_member_status (session_id, status),
  CONSTRAINT fk_session_member_session FOREIGN KEY (session_id) REFERENCES dining_session (id) ON DELETE CASCADE,
  CONSTRAINT fk_session_member_user FOREIGN KEY (user_id) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='堂食会话成员';

CREATE TABLE dining_cart_item (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  session_id BIGINT NOT NULL COMMENT '会话ID',
  creator_user_id BIGINT NOT NULL COMMENT '创建用户ID',
  dish_id BIGINT DEFAULT NULL COMMENT '菜品ID',
  setmeal_id BIGINT DEFAULT NULL COMMENT '套餐ID',
  dish_flavor VARCHAR(255) DEFAULT NULL COMMENT '口味',
  name VARCHAR(64) NOT NULL COMMENT '商品名称快照',
  image VARCHAR(255) DEFAULT NULL COMMENT '图片快照',
  number INT NOT NULL DEFAULT 1 COMMENT '数量',
  amount DECIMAL(10,2) NOT NULL COMMENT '单价快照',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_dining_cart_session (session_id),
  KEY idx_dining_cart_creator (creator_user_id),
  KEY idx_dining_cart_dish (dish_id),
  KEY idx_dining_cart_setmeal (setmeal_id),
  CONSTRAINT fk_dining_cart_session FOREIGN KEY (session_id) REFERENCES dining_session (id) ON DELETE CASCADE,
  CONSTRAINT fk_dining_cart_creator FOREIGN KEY (creator_user_id) REFERENCES user (id),
  CONSTRAINT fk_dining_cart_dish FOREIGN KEY (dish_id) REFERENCES dish (id),
  CONSTRAINT fk_dining_cart_setmeal FOREIGN KEY (setmeal_id) REFERENCES setmeal (id),
  CONSTRAINT chk_dining_cart_item CHECK (
    (dish_id IS NOT NULL AND setmeal_id IS NULL) OR
    (dish_id IS NULL AND setmeal_id IS NOT NULL)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='堂食购物车';

CREATE TABLE orders (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  number VARCHAR(64) NOT NULL COMMENT '订单号',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态：1待付款，2待确认，3已确认，4派送中，5已完成，6已取消',
  order_type VARCHAR(32) NOT NULL DEFAULT 'TAKEOUT' COMMENT 'TAKEOUT/DINE_IN',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  dining_session_id BIGINT DEFAULT NULL COMMENT '堂食会话ID',
  table_id BIGINT DEFAULT NULL COMMENT '堂食桌台ID',
  table_name VARCHAR(64) DEFAULT NULL COMMENT '堂食桌台快照',
  party_size INT DEFAULT NULL COMMENT '堂食人数',
  address_book_id BIGINT DEFAULT NULL COMMENT '地址ID',
  order_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  checkout_time DATETIME DEFAULT NULL COMMENT '结账时间',
  pay_method TINYINT NOT NULL DEFAULT 1 COMMENT '支付方式：1微信，2支付宝',
  pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0未支付，1已支付',
  amount DECIMAL(10,2) NOT NULL COMMENT '实收金额',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  phone VARCHAR(16) NOT NULL COMMENT '手机号快照',
  address VARCHAR(255) NOT NULL COMMENT '地址快照',
  user_name VARCHAR(50) DEFAULT NULL COMMENT '用户名快照',
  consignee VARCHAR(50) NOT NULL COMMENT '收货人快照',
  cancel_reason VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  rejection_reason VARCHAR(255) DEFAULT NULL COMMENT '拒单原因',
  cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
  estimated_delivery_time DATETIME DEFAULT NULL COMMENT '预计送达时间',
  delivery_status TINYINT NOT NULL DEFAULT 1 COMMENT '配送状态：1立即送出，0选择具体时间',
  delivery_time DATETIME DEFAULT NULL COMMENT '送达时间',
  pack_amount INT NOT NULL DEFAULT 0 COMMENT '打包费',
  tableware_number INT NOT NULL DEFAULT 0 COMMENT '餐具数量',
  tableware_status TINYINT NOT NULL DEFAULT 1 COMMENT '餐具状态：1按餐量提供，0无需餐具',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_orders_number (number),
  KEY idx_orders_user (user_id),
  KEY idx_orders_dining_session (dining_session_id),
  KEY idx_orders_table (table_id),
  KEY idx_orders_status_time (status, order_time),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES user (id),
  CONSTRAINT fk_orders_dining_session FOREIGN KEY (dining_session_id) REFERENCES dining_session (id),
  CONSTRAINT fk_orders_table FOREIGN KEY (table_id) REFERENCES dining_table (id),
  CONSTRAINT fk_orders_address FOREIGN KEY (address_book_id) REFERENCES address_book (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单';

CREATE TABLE order_detail (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(64) NOT NULL COMMENT '商品名称快照',
  image VARCHAR(255) DEFAULT NULL COMMENT '图片快照',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  creator_user_id BIGINT DEFAULT NULL COMMENT '堂食加购用户ID',
  dining_cart_item_id BIGINT DEFAULT NULL COMMENT '堂食购物车明细ID',
  dish_id BIGINT DEFAULT NULL COMMENT '菜品ID',
  setmeal_id BIGINT DEFAULT NULL COMMENT '套餐ID',
  dish_flavor VARCHAR(255) DEFAULT NULL COMMENT '菜品口味',
  number INT NOT NULL DEFAULT 1 COMMENT '数量',
  amount DECIMAL(10,2) NOT NULL COMMENT '金额',
  PRIMARY KEY (id),
  KEY idx_order_detail_order (order_id),
  KEY idx_order_detail_creator (creator_user_id),
  KEY idx_order_detail_dining_cart (dining_cart_item_id),
  KEY idx_order_detail_dish (dish_id),
  KEY idx_order_detail_setmeal (setmeal_id),
  CONSTRAINT fk_order_detail_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
  CONSTRAINT fk_order_detail_creator FOREIGN KEY (creator_user_id) REFERENCES user (id),
  CONSTRAINT fk_order_detail_dish FOREIGN KEY (dish_id) REFERENCES dish (id),
  CONSTRAINT fk_order_detail_setmeal FOREIGN KEY (setmeal_id) REFERENCES setmeal (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细';

CREATE TABLE payment_order (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  payment_no VARCHAR(64) NOT NULL COMMENT '支付单号',
  amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  pay_method TINYINT NOT NULL DEFAULT 1 COMMENT '支付方式',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付，1已支付，2已取消',
  paid_at DATETIME DEFAULT NULL COMMENT '支付时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_payment_order_no (payment_no),
  UNIQUE KEY uk_payment_order_order (order_id),
  KEY idx_payment_order_status (status, create_time),
  CONSTRAINT fk_payment_order_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付单';

CREATE TABLE operation_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  operation_user BIGINT DEFAULT NULL COMMENT '操作人ID',
  operation_type VARCHAR(64) DEFAULT NULL COMMENT '操作类型',
  operation_method VARCHAR(255) DEFAULT NULL COMMENT '操作方法或请求路径',
  operation_params VARCHAR(2000) DEFAULT NULL COMMENT '请求参数',
  operation_result VARCHAR(1000) DEFAULT NULL COMMENT '操作结果',
  operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  duration BIGINT DEFAULT NULL COMMENT '耗时毫秒',
  ip VARCHAR(64) DEFAULT NULL COMMENT '客户端IP',
  PRIMARY KEY (id),
  KEY idx_operation_log_user (operation_user),
  KEY idx_operation_log_type (operation_type),
  KEY idx_operation_log_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志';
