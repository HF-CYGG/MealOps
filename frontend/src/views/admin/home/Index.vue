<!-- 管理端首页视图组件 -->
<template>
  <div class="admin-home-container">
    <!-- 数据概览区 -->
    <div class="section-title">数据概览</div>
    <el-row v-loading="statsLoading" :gutter="24" class="stats-row">
      <el-col :xs="24" :sm="12" :md="12" :lg="6" v-for="stat in statsData" :key="stat.title" class="stat-col">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-card-content">
            <div class="stat-info">
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-value">{{ stat.value }}</div>
            </div>
            <div class="stat-icon-wrapper" :style="{ backgroundColor: stat.bgColor, color: stat.color }">
              <el-icon class="stat-icon"><component :is="stat.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作区 -->
    <div class="section-title" style="margin-top: 10px;">快捷操作</div>
    <el-card class="quick-action-card" shadow="hover">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="8" :md="6" :lg="4" v-for="action in quickActions" :key="action.name">
          <div class="quick-action-btn" @click="handleQuickAction(action.route)">
            <div class="action-icon-wrapper">
              <el-icon><component :is="action.icon" /></el-icon>
            </div>
            <div class="action-name">{{ action.name }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Money, ShoppingCart, User, Tickets, Plus, List, Grid, Setting } from '@element-plus/icons-vue'
import { getDashboardOverview } from '@/api/stats'

const router = useRouter()

// 统计数据
const statsLoading = ref(false)
const statsData = ref([
  { key: 'todayTurnover', title: '今日营业额', value: '¥0.00', icon: Money, color: '#409EFF', bgColor: '#ecf5ff' },
  { key: 'todayValidOrders', title: '今日有效订单', value: '0', icon: ShoppingCart, color: '#67C23A', bgColor: '#f0f9eb' },
  { key: 'newUsers', title: '新增用户', value: '0', icon: User, color: '#E6A23C', bgColor: '#fdf6ec' },
  { key: 'pendingOrders', title: '待处理订单', value: '0', icon: Tickets, color: '#F56C6C', bgColor: '#fef0f0' }
])

// 快捷操作
const quickActions = ref([
  { name: '添加菜品', icon: Plus, route: '/dish' },
  { name: '订单管理', icon: List, route: '/order' },
  { name: '分类管理', icon: Grid, route: '/category' },
  { name: '员工管理', icon: User, route: '/employee' },
  { name: '套餐管理', icon: Setting, route: '/setmeal' }
])

// 处理快捷操作点击跳转
const handleQuickAction = (route) => {
  router.push(route)
}

const formatCurrency = (value) => {
  return Number(value || 0).toLocaleString('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  })
}

const formatCount = (value) => {
  return Number(value || 0).toLocaleString('zh-CN')
}

const refreshStats = async () => {
  statsLoading.value = true
  try {
    const res = await getDashboardOverview()
    const overview = res.data || {}
    statsData.value = statsData.value.map((stat) => ({
      ...stat,
      value: stat.key === 'todayTurnover'
        ? formatCurrency(overview[stat.key])
        : formatCount(overview[stat.key])
    }))
  } catch (error) {
    console.error('获取首页数据概览失败', error)
    statsData.value = statsData.value.map((stat) => ({
      ...stat,
      value: stat.key === 'todayTurnover' ? formatCurrency(0) : formatCount(0)
    }))
  } finally {
    statsLoading.value = false
  }
}

onMounted(() => {
  refreshStats()
})
</script>

<style scoped>
.admin-home-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100%;
  box-sizing: border-box;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 10px;
}

.stat-col {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 16px;
  border: none;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08) !important;
}

.stat-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.stat-icon {
  font-size: 28px;
}

.quick-action-card {
  border-radius: 16px;
  border: none;
  padding: 10px;
}

.quick-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #f8f9fa;
  margin-bottom: 16px;
}

.quick-action-btn:hover {
  background-color: #ecf5ff;
  color: #409EFF;
  transform: translateY(-3px);
}

.quick-action-btn:hover .action-icon-wrapper {
  color: #409EFF;
}

.quick-action-btn:hover .action-name {
  color: #409EFF;
}

.action-icon-wrapper {
  font-size: 28px;
  color: #606266;
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.action-name {
  font-size: 14px;
  color: #606266;
  transition: all 0.3s ease;
}
</style>
