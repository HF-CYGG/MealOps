<!--
  C端通用布局组件
  包含顶部导航栏和主要内容区域，提供给C端页面统一的页面骨架
-->
<template>
  <div class="client-layout">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="client-header">
        <div class="logo">MealOps 订餐</div>
        <el-menu
          :default-active="activeIndex"
          mode="horizontal"
          router
          class="nav-menu"
          :ellipsis="false"
        >
          <el-menu-item index="/client/menu">点餐</el-menu-item>
          <el-menu-item index="/client/order/history">历史订单</el-menu-item>
        </el-menu>
        <div class="user-info">
          <el-dropdown @command="handleCommand" v-if="userStore.token">
            <span class="el-dropdown-link">
              {{ userStore.userInfo?.name || userStore.userInfo?.phone || '我的' }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="address">收货地址</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <div v-else class="unlogin-wrapper">
            <span class="unlogin-text" @click="router.push('/client/login')">未登录，去登录</span>
          </div>
        </div>
      </el-header>

      <!-- 主要内容区域 -->
      <el-main class="client-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { useClientUserStore } from '@/store/clientUser'
import { userLogout } from '@/api/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useClientUserStore()

// 当前激活的菜单项
const activeIndex = computed(() => {
  if (route.path.includes('/client/order/history')) {
    return '/client/order/history'
  }
  return '/client/menu'
})

// 处理下拉菜单指令
const handleCommand = async (command) => {
  if (command === 'address') {
    router.push('/client/address')
    return
  }

  if (command === 'logout') {
    try {
      await userLogout()
      userStore.clearUserInfo()
      ElMessage.success('退出成功')
      router.push('/client/login')
    } catch (error) {
      console.error(error)
    }
  }
}
</script>

<style scoped>
.client-layout {
  min-height: 100vh;
  background-color: var(--bg-color);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Helvetica Neue', Arial, sans-serif;
  overflow-x: hidden;
}

.client-layout :deep(.el-container) {
  width: 100%;
  min-width: 0;
}

.client-header {
  box-sizing: border-box;
  background-color: rgba(255, 255, 255, 0.75);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.04);
  padding: 0 32px;
  margin: 16px 40px;
  border-radius: 24px;
  height: 64px;
  position: sticky;
  top: 16px;
  z-index: 100;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.client-header:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.06);
}

.logo {
  font-size: 24px;
  font-weight: 800;
  color: var(--primary-color);
  width: 200px;
  letter-spacing: -0.5px;
  cursor: pointer;
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.logo:hover {
  transform: scale(1.02);
}

.nav-menu {
  flex: 1;
  border-bottom: none;
  justify-content: center;
  background: transparent;
}

:deep(.el-menu--horizontal .el-menu-item) {
  font-size: 16px;
  font-weight: 500;
  border-bottom: none !important;
  color: var(--text-color-secondary);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  border-radius: 20px;
  margin: 0 12px;
  height: 40px;
  line-height: 40px;
  top: 12px;
  padding: 0 24px;
}

:deep(.el-menu--horizontal .el-menu-item:not(.is-disabled):focus),
:deep(.el-menu--horizontal .el-menu-item:not(.is-disabled):hover) {
  background-color: rgba(0, 0, 0, 0.04);
  color: var(--primary-color);
}

:deep(.el-menu--horizontal .el-menu-item.is-active) {
  border-bottom: none;
  color: var(--text-color) !important;
  background-color: #f0f0f2;
  font-weight: 700;
  box-shadow: none;
}

.user-info {
  width: 200px;
  text-align: right;
  cursor: pointer;
}

.el-dropdown-link {
  display: inline-flex;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color);
  padding: 10px 20px;
  border-radius: 20px;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

.el-dropdown-link:hover {
  background-color: var(--primary-color);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.unlogin-wrapper {
  display: inline-flex;
  align-items: center;
}

.unlogin-text {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color-secondary);
  padding: 10px 20px;
  border-radius: 20px;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  background-color: transparent;
}

.unlogin-text:hover {
  background-color: #fff;
  color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.client-main {
  padding: 24px 40px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  overflow-x: hidden;
}

/* 优化页面切换的全局过渡动画为丝滑的淡入淡出 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: scale(0.98);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: scale(1.02);
}

@media (max-width: 1024px) {
  .client-header {
    margin: 12px 20px;
    padding: 0 20px;
  }

  .logo,
  .user-info {
    width: auto;
    min-width: 150px;
  }

  .client-main {
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .client-header {
    flex-wrap: wrap;
    height: auto;
    min-height: 92px;
    margin: 10px 12px;
    padding: 12px;
    width: calc(100% - 24px);
    border-radius: 22px;
    row-gap: 10px;
  }

  .logo {
    width: auto;
    min-width: 0;
    flex: 1 1 auto;
    font-size: 20px;
  }

  .user-info {
    width: auto;
    min-width: 0;
    flex: 0 0 auto;
  }

  .nav-menu {
    order: 3;
    flex: 1 0 100%;
    justify-content: stretch;
  }

  :deep(.el-menu--horizontal .el-menu-item) {
    flex: 1;
    justify-content: center;
    margin: 0 4px;
    top: 0;
    padding: 0 12px;
    height: 40px;
    line-height: 40px;
  }

  .el-dropdown-link,
  .unlogin-text {
    padding: 8px 12px;
    max-width: 132px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .client-main {
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .client-header {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    align-items: center;
    margin: 8px;
    width: calc(100% - 16px);
  }

  .logo {
    font-size: 18px;
    width: auto;
    min-width: 0;
  }

  .user-info {
    justify-self: end;
    max-width: 96px;
  }

  .nav-menu {
    grid-column: 1 / -1;
    width: 100%;
    min-width: 0;
  }

  :deep(.el-menu--horizontal .el-menu-item) {
    min-width: 0;
    font-size: 14px;
    padding: 0 8px;
  }

  .el-dropdown-link,
  .unlogin-text {
    max-width: 96px;
    padding: 8px;
    font-size: 13px;
  }
}
</style>
