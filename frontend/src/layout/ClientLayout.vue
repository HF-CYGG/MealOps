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
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              我的<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主要内容区域 -->
      <el-main class="client-main">
        <router-view />
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
  background-color: #f5f7fa;
}

.client-header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 0 40px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
  width: 200px;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
  justify-content: center;
}

.user-info {
  width: 200px;
  text-align: right;
  cursor: pointer;
}

.client-main {
  padding: 20px 40px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}
</style>
