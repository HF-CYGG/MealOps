# MealOps 接口验收清单

记录人：闫奕衡  
日期：2026-06-23  
环境：`http://localhost:8080`

## 基础

| 模块 | 方法 | 路径 | 验收点 | 结果 | 备注 |
| --- | --- | --- | --- | --- | --- |
| 健康检查 | GET | `/health` | 返回 `code=1`，`data.status=UP` | 通过 | `06-health.png` |
| OpenAPI | GET | `/v3/api-docs` | 返回 OpenAPI JSON | 通过 | Swagger 页面加载 OpenAPI |
| Swagger | GET | `/swagger-ui.html` | 页面可打开 | 通过 | `07-swagger-ui.png` |

## 管理端

| 模块 | 方法 | 路径 | 验收点 | 结果 | 备注 |
| --- | --- | --- | --- | --- | --- |
| 员工登录 | POST | `/employee/login` | `admin/123456` 登录成功并返回 token | 通过 | `08-admin-login.png`，token 已遮挡 |
| 员工退出 | POST | `/employee/logout` | 登录态退出成功 | 待复测 | 页面有按钮，未单独截图 |
| 分类分页 | GET | `/category/page?page=1&pageSize=10` | 返回分页数据 | 待复测 | 本轮截图覆盖分类列表 |
| 分类列表 | GET | `/category/list?type=1` | 返回菜品分类 | 通过 | `09-category-list.png` |
| 新增分类 | POST | `/category` | 新分类写入成功 | 通过 | `10-category-save.png` |
| 修改分类 | PUT | `/category` | 名称、排序或状态更新成功 | 待复测 | 建议 Swagger 补测 |
| 删除分类 | DELETE | `/category?id={id}` | 无关联菜品/套餐时可删除 | 部分通过 | 删除有关联分类被拦截见 `28-category-delete-guard.png` |
| 菜品分页 | GET | `/dish/page?page=1&pageSize=10` | 返回菜品分页 | 通过 | `11-dish-page.png` |
| 菜品列表 | GET | `/dish/list?categoryId=1001` | 返回指定分类菜品 | 通过 | `18-user-dish-list.png` |
| 新增菜品 | POST | `/dish` | 菜品和口味写入成功 | 通过 | `12-dish-save.png` |
| 修改菜品 | PUT | `/dish` | 菜品和口味更新成功 | 待复测 | 建议 Swagger 补测 |
| 菜品起售停售 | POST | `/dish/status/{status}?ids={ids}` | 状态批量更新成功 | 待复测 | 建议 Swagger 补测 |
| 删除菜品 | DELETE | `/dish?ids={ids}` | 未售卖或未关联时删除成功 | 待复测 | 建议 Swagger 补测 |
| 套餐分页 | GET | `/setmeal/page?page=1&pageSize=10` | 返回套餐分页 | 通过 | `13-setmeal-page.png` |
| 套餐列表 | GET | `/setmeal/list?categoryId=2001` | 返回指定分类套餐 | 通过 | `19-user-setmeal-list.png` |
| 新增套餐 | POST | `/setmeal` | 套餐和套餐菜品写入成功 | 通过 | `14-setmeal-save.png` |
| 修改套餐 | PUT | `/setmeal` | 套餐和明细更新成功 | 待复测 | 建议 Swagger 补测 |
| 套餐起售停售 | POST | `/setmeal/status/{status}?ids={ids}` | 状态批量更新成功 | 待复测 | 建议 Swagger 补测 |
| 删除套餐 | DELETE | `/setmeal?ids={ids}` | 未售卖时删除成功 | 待复测 | 建议 Swagger 补测 |
| 订单分页 | GET | `/order/page?page=1&pageSize=10` | 管理端可查看订单 | 通过 | `15-admin-order-page.png` |
| 订单派送 | POST | `/order/status` | 订单状态可更新 | 待复测 | 原清单 `PUT /order` 与实际接口不一致，已改为实际接口 |
| 操作日志 | GET | `/logs/page?page=1&pageSize=10` | 返回操作日志分页 | 通过 | `16-operation-log.png` |

## 用户端

| 模块 | 方法 | 路径 | 验收点 | 结果 | 备注 |
| --- | --- | --- | --- | --- | --- |
| 用户登录 | POST | `/user/login` | 示例手机号可登录并返回 token | 通过 | `17-user-login.png`，token 已遮挡 |
| 地址列表 | GET | `/addressBook/list` | 返回当前用户地址 | 通过 | `20-address-book.png` |
| 默认地址 | GET | `/addressBook/default` | 返回默认地址 | 待复测 | 页面有按钮，未单独截图 |
| 新增地址 | POST | `/addressBook` | 地址写入成功 | 待复测 | 本轮截图覆盖地址查询 |
| 修改地址 | PUT | `/addressBook` | 地址更新成功 | 待复测 | 建议 Swagger 补测 |
| 设置默认地址 | PUT | `/addressBook/default` | 默认地址切换成功 | 待复测 | 建议 Swagger 补测 |
| 加入购物车 | POST | `/shoppingCart/add` | 菜品或套餐加入购物车 | 通过 | `21-cart-add.png` |
| 查看购物车 | GET | `/shoppingCart/list` | 返回购物车明细和数量 | 通过 | `22-cart-list.png` |
| 减少购物车 | POST | `/shoppingCart/sub` | 商品数量减少 | 待复测 | 建议 Swagger 补测 |
| 清空购物车 | DELETE | `/shoppingCart/clean` | 当前用户购物车清空 | 通过 | 作为 `29-empty-cart-order.png` 前置操作 |
| 提交订单 | POST | `/order/submit` | 根据购物车和地址生成订单 | 通过 | `23-order-submit.png` |
| 用户订单分页 | GET | `/order/userPage?page=1&pageSize=10` | 返回当前用户订单 | 通过 | `24-user-order-page.png` |
| 再来一单 | POST | `/order/repetition/{id}` | 历史订单商品回填购物车 | 待复测 | 建议 Swagger 补测 |

## 异常与权限

| 场景 | 操作 | 预期结果 | 结果 | 备注 |
| --- | --- | --- | --- | --- |
| 未登录访问管理端接口 | 不带 token 调 `/dish/page` | 返回未登录或 401 | 通过 | `27-auth-required.png`，实际为 `code=0,msg=未登录` |
| 错误管理员密码 | `admin` + 错误密码 | 登录失败，错误次数记录 | 通过 | `25-login-fail.png` |
| 登录锁定 | 连续多次错误登录 | 账号短时间锁定 | 通过 | `26-login-lock.png` |
| 用户越权 | 用户 token 调管理端写接口 | 返回无权限 | 待复测 | 建议 Swagger 补测 |
| 分类删除保护 | 删除有关联菜品的分类 | 返回业务错误 | 通过 | `28-category-delete-guard.png` |
| 空购物车下单 | 清空购物车后提交订单 | 返回业务错误 | 通过 | `29-empty-cart-order.png` |
