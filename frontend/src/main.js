/**
 * Vue 应用入口文件
 * 用于创建 Vue 实例并挂载全局插件（如路由、状态管理、UI组件库等）
 */

import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

// 引入路由
import router from './router'
// 引入Pinia状态管理
import pinia from './store'
// 引入Element Plus及样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)

// 注册插件
app.use(router)
app.use(pinia)
app.use(ElementPlus)

// 挂载应用
app.mount('#app')
