<!--
  管理端全局布局组件
  包含侧边栏 (Sidebar)、顶栏 (Header) 和主要内容区域 (Main)
-->
<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>MealOps</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        router
      >
        <el-menu-item index="/">
          <el-icon><DataBoard /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/employee">
          <el-icon><User /></el-icon>
          <span>员工管理</span>
        </el-menu-item>
        <el-menu-item index="/category">
          <el-icon><Files /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/dish">
          <el-icon><Food /></el-icon>
          <span>菜品管理</span>
        </el-menu-item>
        <el-menu-item index="/setmeal">
          <el-icon><Box /></el-icon>
          <span>套餐管理</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-left">
          <!-- 面包屑或其他导航信息 -->
          <span class="page-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ userInfo.name || '管理员' }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主要内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  DataBoard, User, Files, Food, Box, List, ArrowDown 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { logout } from '@/api/employee'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 获取当前用户信息
const userInfo = computed(() => userStore.userInfo)

// 当前激活的菜单
const activeMenu = computed(() => {
  // 如果是首页（精确匹配 /），或者带 /home 的路径
  if (route.path === '/' || route.path === '/home') return '/'
  
  // 对于其他路由（例如 /employee, /category 等）
  // 提取一级路径，比如 /employee/add 也高亮 /employee
  const pathParts = route.path.split('/')
  if (pathParts.length > 1 && pathParts[1]) {
    return '/' + pathParts[1]
  }
  
  return route.path
})

// 当前页面标题
const currentTitle = computed(() => {
  return route.meta.title || 'MealOps'
})

// 处理下拉菜单指令
const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      // 调用后端退出接口
      await logout()
      
      // 清除本地状态
      userStore.clearUserInfo()
      ElMessage.success('退出成功')
      router.push('/login')
    } catch (error) {
      if (error !== 'cancel') {
        console.error(error)
      }
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100%;
  overflow-x: hidden;
  background-color: var(--bg-color); /* 统一呼吸感背景 */
}

.sidebar {
  /* 无边框设计，悬浮高亮 */
  background-color: #fff;
  transition: width 0.28s, box-shadow 0.3s;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.02);
  z-index: 10;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: var(--primary-color);
  background: transparent;
  overflow: hidden;
  font-weight: bold;
  letter-spacing: 1px;
}

.el-menu-vertical {
  border-right: none;
  background-color: transparent;
}

:deep(.el-menu) {
  background-color: transparent !important;
}

:deep(.el-menu-item) {
  margin: 8px 12px;
  border-radius: 12px;
  height: 48px;
  line-height: 48px;
  color: var(--text-color-secondary) !important;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

:deep(.el-menu-item:hover) {
  background-color: rgba(0, 0, 0, 0.04) !important;
  color: var(--primary-color) !important;
  transform: translateX(4px);
}

:deep(.el-menu-item.is-active) {
  background-color: var(--primary-color) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(28, 28, 30, 0.2);
  font-weight: 600;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  background-color: transparent; /* 移除白底，融入背景 */
  padding: 0 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--primary-color);
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: var(--text-color);
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 20px;
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.02);
  transition: all 0.3s;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

.main-content {
  background-color: transparent;
  padding: 0 24px 24px 24px;
  overflow-x: hidden;
}

/* 路由切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

@media (max-width: 1024px) {
  .sidebar {
    width: 88px !important;
  }

  .logo h2 {
    font-size: 18px;
  }

  :deep(.el-menu-item) {
    justify-content: center;
    padding: 0 !important;
  }

  :deep(.el-menu-item span) {
    display: none;
  }
}

@media (max-width: 768px) {
  .layout-container {
    height: auto;
    min-height: 100vh;
    overflow-x: hidden;
  }

  .sidebar {
    width: 72px !important;
    flex: 0 0 72px;
  }

  .logo {
    height: 56px;
    line-height: 56px;
  }

  .logo h2 {
    font-size: 13px;
    letter-spacing: 0;
  }

  :deep(.el-menu-item) {
    width: 48px;
    height: 48px;
    margin: 8px auto;
  }

  .header {
    height: auto;
    min-height: 56px;
    padding: 10px 14px;
    gap: 12px;
  }

  .page-title {
    font-size: 18px;
  }

  .user-info {
    max-width: 132px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .main-content {
    padding: 0 12px 16px;
  }
}
</style>
