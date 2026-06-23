# MealOps 实训报告截图清单

截图应包含浏览器地址栏或终端命令，便于报告追溯。涉及 token、数据库密码、Cookie 的截图需要遮挡敏感值。

## 环境与启动

| 序号 | 截图内容 | 文件名建议 | 是否完成 | 备注 |
| --- | --- | --- | --- | --- |
| 1 | MySQL `reggie` 数据库创建成功 | `01-mysql-database.png` | 已完成 | 密码已遮挡 |
| 2 | `schema.sql` 导入后表列表 | `02-schema-tables.png` | 已完成 | 12 张表 |
| 3 | `data.sql` 导入后管理员账号和示例分类 | `03-seed-data.png` | 已完成 | 遮挡无关敏感信息 |
| 4 | Redis 服务运行状态 | `04-redis-status.png` | 已完成 | TCP PING 返回 PONG |
| 5 | Maven 启动日志显示端口 8080 | `05-boot-start.png` | 已完成 | JDK 17 启动 |
| 6 | `/health` 返回 `UP` | `06-health.png` | 已完成 | 浏览器截图 |
| 7 | Swagger UI 页面 | `07-swagger-ui.png` | 已完成 | 浏览器截图 |

## 管理端功能

| 序号 | 截图内容 | 文件名建议 | 是否完成 | 备注 |
| --- | --- | --- | --- | --- |
| 8 | `admin.html` 管理员登录成功 | `08-admin-login.png` | 已完成 | token 已遮挡 |
| 9 | 分类列表或分页查询 | `09-category-list.png` | 已完成 |  |
| 10 | 新增或修改分类成功 | `10-category-save.png` | 已完成 | 新增截图分类 |
| 11 | 菜品分页查询 | `11-dish-page.png` | 已完成 |  |
| 12 | 新增菜品及口味成功 | `12-dish-save.png` | 已完成 | 新增截图菜品 |
| 13 | 套餐分页查询 | `13-setmeal-page.png` | 已完成 |  |
| 14 | 新增套餐及明细成功 | `14-setmeal-save.png` | 已完成 | 新增截图套餐 |
| 15 | 管理端订单分页 | `15-admin-order-page.png` | 已完成 | 自定义请求 |
| 16 | 操作日志分页 | `16-operation-log.png` | 已完成 |  |

## 用户端功能

| 序号 | 截图内容 | 文件名建议 | 是否完成 | 备注 |
| --- | --- | --- | --- | --- |
| 17 | `user.html` 用户登录成功 | `17-user-login.png` | 已完成 | token 已遮挡 |
| 18 | 菜品分类和菜品列表 | `18-user-dish-list.png` | 已完成 |  |
| 19 | 套餐列表 | `19-user-setmeal-list.png` | 已完成 |  |
| 20 | 新增或查询地址簿 | `20-address-book.png` | 已完成 | 查询地址簿 |
| 21 | 加入购物车 | `21-cart-add.png` | 已完成 |  |
| 22 | 购物车列表 | `22-cart-list.png` | 已完成 |  |
| 23 | 提交订单成功 | `23-order-submit.png` | 已完成 |  |
| 24 | 用户订单分页 | `24-user-order-page.png` | 已完成 |  |

## 异常与安全

| 序号 | 截图内容 | 文件名建议 | 是否完成 | 备注 |
| --- | --- | --- | --- | --- |
| 25 | 错误密码登录失败 | `25-login-fail.png` | 已完成 | 终端接口截图 |
| 26 | 连续错误触发登录限制 | `26-login-lock.png` | 已完成 | 终端接口截图 |
| 27 | 不带 token 访问受保护接口失败 | `27-auth-required.png` | 已完成 |  |
| 28 | 删除有关联分类被拦截 | `28-category-delete-guard.png` | 已完成 |  |
| 29 | 空购物车下单失败 | `29-empty-cart-order.png` | 已完成 | 终端接口截图 |

## 报告整理

| 序号 | 材料 | 是否完成 | 备注 |
| --- | --- | --- | --- |
| 30 | 测试记录表已填写 | 已完成 | `docs/test-record-template.md` |
| 31 | 接口验收清单已填写 | 已完成 | `docs/api-checklist.md` |
| 32 | 关键 SQL、配置、页面文件路径已写入报告 | 已完成 | README 与 docs 已记录 |
| 33 | 未完成点和复测项已单独说明 | 已完成 | 见测试记录“未完成或需复测项” |
