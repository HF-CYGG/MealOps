SET NAMES utf8mb4;

USE reggie;

INSERT INTO employee
  (id, username, password, name, phone, sex, id_number, status, create_time, update_time, create_user, update_user)
VALUES
  (1, 'admin', '849199be4c873a9ab895576681da12f3', '系统管理员', '13800000000', '男', '110101199001010011', 1, NOW(), NOW(), 1, 1);

INSERT INTO user
  (id, openid, name, phone, sex, id_number, avatar, status, create_time, update_time)
VALUES
  (101, 'mock-openid-13900000000', '测试用户', '13900000000', '女', NULL, NULL, 1, NOW(), NOW());

INSERT INTO category
  (id, type, name, sort, status, create_time, update_time, create_user, update_user)
VALUES
  (1001, 1, '热销家常菜', 10, 1, NOW(), NOW(), 1, 1),
  (1002, 1, '主食小吃', 20, 1, NOW(), NOW(), 1, 1),
  (1003, 1, '饮品甜点', 30, 1, NOW(), NOW(), 1, 1),
  (2001, 2, '单人套餐', 10, 1, NOW(), NOW(), 1, 1),
  (2002, 2, '双人套餐', 20, 1, NOW(), NOW(), 1, 1);

INSERT INTO dish
  (id, name, category_id, price, code, image, description, status, stock, version, sort, create_time, update_time, create_user, update_user)
VALUES
  (3001, '宫保鸡丁', 1001, 28.00, 'DISH-3001', '/images/dish-gongbao.jpg', '鸡肉鲜嫩，酸甜微辣，经典热销。', 1, 100, 0, 10, NOW(), NOW(), 1, 1),
  (3002, '鱼香肉丝', 1001, 26.00, 'DISH-3002', '/images/dish-yuxiang.jpg', '咸甜酸辣均衡，适合下饭。', 1, 100, 0, 20, NOW(), NOW(), 1, 1),
  (3003, '番茄炒蛋', 1001, 18.00, 'DISH-3003', '/images/dish-tomato-egg.jpg', '家常口味，酸甜开胃。', 1, 100, 0, 30, NOW(), NOW(), 1, 1),
  (3004, '扬州炒饭', 1002, 22.00, 'DISH-3004', '/images/dish-fried-rice.jpg', '粒粒分明，配料丰富。', 1, 100, 0, 10, NOW(), NOW(), 1, 1),
  (3005, '酸梅汤', 1003, 8.00, 'DISH-3005', '/images/dish-plum-drink.jpg', '冰镇酸甜，解腻爽口。', 1, 100, 0, 10, NOW(), NOW(), 1, 1);

INSERT INTO dish_flavor
  (id, dish_id, name, value, create_time, update_time, create_user, update_user)
VALUES
  (4001, 3001, '辣度', '["不辣","微辣","中辣"]', NOW(), NOW(), 1, 1),
  (4002, 3002, '辣度', '["微辣","中辣"]', NOW(), NOW(), 1, 1),
  (4003, 3003, '甜度', '["少糖","标准"]', NOW(), NOW(), 1, 1),
  (4004, 3005, '温度', '["常温","少冰","多冰"]', NOW(), NOW(), 1, 1);

INSERT INTO setmeal
  (id, category_id, name, price, status, code, description, image, create_time, update_time, create_user, update_user)
VALUES
  (5001, 2001, '经典单人餐', 38.00, 1, 'SET-5001', '宫保鸡丁、米饭、酸梅汤组合。', '/images/setmeal-single.jpg', NOW(), NOW(), 1, 1),
  (5002, 2002, '家常双人餐', 68.00, 1, 'SET-5002', '两道热菜搭配主食和饮品。', '/images/setmeal-double.jpg', NOW(), NOW(), 1, 1);

INSERT INTO setmeal_dish
  (id, setmeal_id, dish_id, name, price, copies, sort, create_time, update_time, create_user, update_user)
VALUES
  (6001, 5001, 3001, '宫保鸡丁', 28.00, 1, 10, NOW(), NOW(), 1, 1),
  (6002, 5001, 3005, '酸梅汤', 8.00, 1, 20, NOW(), NOW(), 1, 1),
  (6003, 5002, 3002, '鱼香肉丝', 26.00, 1, 10, NOW(), NOW(), 1, 1),
  (6004, 5002, 3003, '番茄炒蛋', 18.00, 1, 20, NOW(), NOW(), 1, 1),
  (6005, 5002, 3004, '扬州炒饭', 22.00, 1, 30, NOW(), NOW(), 1, 1);

INSERT INTO address_book
  (id, user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default, create_time, update_time)
VALUES
  (7001, 101, '测试用户', '女', '13900000000', '110000', '北京市', '110100', '北京市', '110101', '东城区', '演示路 1 号 MealOps 实训楼 808', '学校', 1, NOW(), NOW());

INSERT INTO dining_table
  (id, table_no, table_name, capacity, status, current_session_id, create_time, update_time)
VALUES
  (8101, 'A01', 'A01 四人桌', 4, 'OCCUPIED', 8201, NOW(), NOW()),
  (8102, 'A02', 'A02 四人桌', 4, 'AVAILABLE', NULL, NOW(), NOW()),
  (8103, 'B01', 'B01 六人桌', 6, 'AVAILABLE', NULL, NOW(), NOW());

INSERT INTO dining_session
  (id, table_id, table_name, creator_user_id, party_size, status, opened_at, closed_at, create_time, update_time)
VALUES
  (8201, 8101, 'A01 四人桌', 101, 2, 'ACTIVE', NOW(), NULL, NOW(), NOW());

INSERT INTO dining_session_member
  (id, session_id, user_id, nickname, status, joined_at, left_at)
VALUES
  (8301, 8201, 101, '测试用户', 'ACTIVE', NOW(), NULL);

INSERT INTO dining_cart_item
  (id, session_id, creator_user_id, dish_id, setmeal_id, dish_flavor, name, image, number, amount, create_time, update_time)
VALUES
  (8401, 8201, 101, 3003, NULL, '标准', '番茄炒蛋', '/images/dish-tomato-egg.jpg', 1, 18.00, NOW(), NOW());

INSERT INTO shopping_cart
  (id, user_id, dish_id, setmeal_id, dish_flavor, name, image, number, amount, create_time)
VALUES
  (8001, 101, 3001, NULL, '微辣', '宫保鸡丁', '/images/dish-gongbao.jpg', 1, 28.00, NOW()),
  (8002, 101, NULL, 5001, NULL, '经典单人餐', '/images/setmeal-single.jpg', 1, 38.00, NOW());

INSERT INTO orders
  (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, remark, phone, address, user_name, consignee, estimated_delivery_time, delivery_status, pack_amount, tableware_number, tableware_status, create_time, update_time)
VALUES
  (9001, 'MO202606230001', 5, 101, 7001, NOW(), NOW(), 1, 1, 38.00, '示例已完成订单', '13900000000', '北京市北京市东城区演示路 1 号 MealOps 实训楼 808', '测试用户', '测试用户', DATE_ADD(NOW(), INTERVAL 30 MINUTE), 1, 0, 1, 1, NOW(), NOW());

INSERT INTO order_detail
  (id, name, image, order_id, dish_id, setmeal_id, dish_flavor, number, amount)
VALUES
  (9101, '经典单人餐', '/images/setmeal-single.jpg', 9001, NULL, 5001, NULL, 1, 38.00);

INSERT INTO payment_order
  (id, order_id, payment_no, amount, pay_method, status, paid_at, create_time, update_time)
VALUES
  (9201, 9001, 'PAY202606230001', 38.00, 1, 1, NOW(), NOW(), NOW());

INSERT INTO operation_log
  (id, operation_user, operation_type, operation_method, operation_params, operation_result, operation_time, duration, ip)
VALUES
  (10001, 1, 'INIT', '/sql/data.sql', '{"target":"employee","username":"admin"}', '初始化管理员账号 admin/admin1330', NOW(), 0, '127.0.0.1'),
  (10002, 1, 'INIT', '/sql/data.sql', '{"target":"dish","id":3001}', '初始化示例菜品宫保鸡丁', NOW(), 0, '127.0.0.1'),
  (10003, 1, 'INIT', '/sql/data.sql', '{"target":"setmeal","id":5001}', '初始化示例套餐经典单人餐', NOW(), 0, '127.0.0.1');
