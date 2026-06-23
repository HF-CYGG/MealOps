# Tasks
- [x] Task 1: 初始化前端项目
  - [x] SubTask 1.1: 在项目根目录创建 `frontend` 文件夹，使用 Vite 初始化 Vue3 项目。
  - [x] SubTask 1.2: 安装并配置 Element-Plus、Vue Router、Pinia、Axios。
  - [x] SubTask 1.3: 配置全局基础样式，确立“简约风格”的设计规范（如主色调、间距、字体）。
- [x] Task 2: 封装 API 请求与路由
  - [x] SubTask 2.1: 封装 Axios 实例，配置请求拦截器携带 Token，响应拦截器处理统一错误（对接后端的 `R.java`）。
  - [x] SubTask 2.2: 初始化 Vue Router，配置管理端(Admin)和用户端(User)的基础路由及路由守卫。
  - [x] SubTask 2.3: 在 Vite 中配置代理 (proxy)，将 `/employee`、`/user`、`/category` 等请求转发至后端的 `8080` 端口。
- [x] Task 3: 开发管理端 (Admin) 核心页面
  - [x] SubTask 3.1: 开发管理端登录页和后台 Layout（顶栏 + 侧边栏菜单）。
  - [x] SubTask 3.2: 开发员工管理页面（列表、新增、编辑）。
  - [x] SubTask 3.3: 开发分类管理与菜品/套餐管理页面。
  - [x] SubTask 3.4: 开发订单管理页面。
- [x] Task 4: 开发用户端 (User) 核心页面
  - [x] SubTask 4.1: 开发用户端登录页和基础 Layout。
  - [x] SubTask 4.2: 开发菜品与套餐浏览展示页（支持分类切换联动）。
  - [x] SubTask 4.3: 开发购物车展示及下单结算页面。
  - [x] SubTask 4.4: 开发个人历史订单查看页面。
- [x] Task 5: 联调与优化
  - [x] SubTask 5.1: 对接所有相关后端接口进行联调测试。
  - [x] SubTask 5.2: 整体 UI 细节打磨，补充必要的中文注释，确保代码结构清晰。

# Task Dependencies
- [Task 2] depends on [Task 1]
- [Task 3] depends on [Task 2]
- [Task 4] depends on [Task 2]
- [Task 5] depends on [Task 3, Task 4]