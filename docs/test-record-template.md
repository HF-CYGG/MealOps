# MealOps 测试记录模板

项目名称：MealOps 外卖点餐实训系统  
测试人员：闫奕衡  
测试日期：2026-06-23  
测试环境：Windows 11，本地 MySQL80，本地 Redis Windows fork  
后端地址：`http://localhost:8080`  
数据库：`reggie`  
Redis：localhost:6379  

## 版本与配置

| 项目 | 内容 |
| --- | --- |
| Java 版本 | 17.0.12 |
| Maven 版本 | 3.9.11 |
| MySQL 版本 | 8.0.44 |
| Redis 版本 | redis-windows 8.8.0 |
| 启动命令 | `E:\JAVA17\bin\java.exe -jar target\mealops-0.0.1-SNAPSHOT.jar --server.port=8080` |
| 初始化脚本 | `sql/schema.sql`、`sql/data.sql` |
| 管理员账号 | `admin / 123456` |

## 测试范围

| 范围 | 是否测试 | 说明 |
| --- | --- | --- |
| 应用启动 | 是 | 见 `05-boot-start.png` |
| 健康检查 | 是 | 见 `06-health.png` |
| 管理端登录 | 是 | 见 `08-admin-login.png` |
| 分类管理 | 是 | 见 `09-category-list.png`、`10-category-save.png` |
| 菜品管理 | 是 | 见 `11-dish-page.png`、`12-dish-save.png` |
| 套餐管理 | 是 | 见 `13-setmeal-page.png`、`14-setmeal-save.png` |
| 用户登录 | 是 | 见 `17-user-login.png` |
| 地址簿 | 是 | 见 `20-address-book.png` |
| 购物车 | 是 | 见 `21-cart-add.png`、`22-cart-list.png` |
| 订单提交 | 是 | 见 `23-order-submit.png` |
| 订单管理 | 是 | 见 `15-admin-order-page.png`、`24-user-order-page.png` |
| 操作日志 | 是 | 见 `16-operation-log.png` |
| 权限与异常 | 是 | 见 `25-login-fail.png` 至 `29-empty-cart-order.png` |

## 测试用例记录

| 编号 | 模块 | 前置条件 | 操作步骤 | 预期结果 | 实际结果 | 结论 | 证据截图 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| TC-001 | 启动 | MySQL、Redis 已启动 | 执行启动命令 | 应用监听 8080 | Tomcat started on port 8080 | 通过 | `05-boot-start.png` |
| TC-002 | 健康检查 | 应用已启动 | 访问 `/health` | 返回 `UP` | `data.status=UP` | 通过 | `06-health.png` |
| TC-003 | 管理员登录 | 已导入初始化数据 | 用 `admin/123456` 登录 | 返回成功和 token | 返回 `code=1`，token 已遮挡 | 通过 | `08-admin-login.png` |
| TC-004 | 分类管理 | 管理员已登录 | 新增、查询分类 | 数据状态符合预期 | 分类列表和新增分类成功 | 通过 | `09-category-list.png`、`10-category-save.png` |
| TC-005 | 菜品管理 | 分类存在 | 新增菜品和口味，查询分页 | 菜品和口味保存成功 | 菜品分页和新增菜品成功 | 通过 | `11-dish-page.png`、`12-dish-save.png` |
| TC-006 | 套餐管理 | 菜品存在 | 新增套餐并关联菜品 | 套餐明细保存成功 | 套餐分页和新增套餐成功 | 通过 | `13-setmeal-page.png`、`14-setmeal-save.png` |
| TC-007 | 用户登录 | 示例手机号可用 | 用户端登录 | 返回成功和 token | 返回 `code=1`，token 已遮挡 | 通过 | `17-user-login.png` |
| TC-008 | 地址簿 | 用户已登录 | 查询地址列表 | 默认地址可查询 | 返回当前用户地址 | 通过 | `20-address-book.png` |
| TC-009 | 购物车 | 用户已登录且菜品起售 | 加入菜品、查询购物车 | 数量和金额正确 | 加购和购物车列表成功 | 通过 | `21-cart-add.png`、`22-cart-list.png` |
| TC-010 | 下单 | 购物车有商品且有默认地址 | 提交订单 | 订单与明细生成 | 返回订单号、金额和时间 | 通过 | `23-order-submit.png` |
| TC-011 | 操作日志 | 管理端发生写操作 | 查询操作日志 | 记录操作者、动作和时间 | 操作日志分页返回记录 | 通过 | `16-operation-log.png` |
| TC-012 | 异常权限 | 不带 token | 调用受保护接口 | 返回未登录或无权限 | 返回 `code=0` 的业务错误 | 通过 | `27-auth-required.png` |

## 问题记录

| 编号 | 问题描述 | 复现步骤 | 影响范围 | 处理状态 | 负责人 | 备注 |
| --- | --- | --- | --- | --- | --- | --- |
| BUG-001 | PowerShell 不支持 `<` 输入重定向 | 在 PowerShell 执行 `mysql -u root -p < sql\schema.sql` | 数据库初始化 | 已处理 | 闫奕衡 | 改用 `cmd /c` 或 cmd 窗口执行 |
| BUG-002 | Windows MySQL 客户端默认字符集导致中文种子数据导入失败 | 导入 `data.sql` 报 `Data too long for column 'name'` | 数据库初始化 | 已处理 | 闫奕衡 | 重新使用 `--default-character-set=utf8mb4` 导入 |
| BUG-003 | `redis-cli` 在当前 MSYS2 封装下执行时报 CreateFileMapping 权限错误 | 执行 `redis-cli ping` | Redis 截图证据 | 已规避 | 闫奕衡 | 使用 TCP Redis 协议验证 `PING -> PONG` |

## 测试结论

本轮测试结论：核心环境、启动、管理端、用户端、异常权限截图均已完成，应用构建和接口链路满足实训报告留痕要求。  

未完成或需复测项：接口清单中少量未单独截图的修改、删除、起售停售、再来一单等接口建议在最终答辩前用 Swagger 或 Postman 补充复测。  

签字：闫奕衡  
