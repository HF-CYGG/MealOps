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
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
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
}

.sidebar {
  background-color: #304156;
  transition: width 0.28s;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  background: #2b3643;
  overflow: hidden;
}

.el-menu-vertical {
  border-right: none;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #606266;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}

/* 路由切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
