<!--
  用户端首页引导页
  用户进入网页后先选择用餐人数，再进入商品展示页面
  本文件主要实现了苹果风格(Apple Style)的人数选择界面，使用深色强调、大圆角和弥散阴影来提升质感。
-->
<template>
  <div class="guest-home-container fade-slide-enter-active">
    <div class="welcome-box">
      <h1 class="title">欢迎光临 MealOps</h1>
      <p class="subtitle">请选择用餐人数，开启美味之旅</p>
      
      <div class="guest-selection">
        <div class="quick-options">
          <el-button 
            v-for="num in 6" 
            :key="num" 
            class="guest-btn" 
            :class="{ active: selectedCount === num }"
            @click="selectCount(num)"
          >
            {{ num }}人
          </el-button>
        </div>
        
        <div class="custom-option">
          <span class="label">更多人数：</span>
          <el-input-number 
            v-model="customCount" 
            :min="7" 
            :max="50" 
            @change="handleCustomChange"
            class="custom-input"
          />
        </div>
      </div>

      <el-button 
        class="start-btn" 
        :disabled="!currentCount"
        @click="startOrdering"
      >
        开始点餐
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useClientUserStore } from '@/store/clientUser'

const router = useRouter()
const userStore = useClientUserStore()

// 选中的快捷人数，默认不选中
const selectedCount = ref(null)
// 自定义输入的人数，默认7人起
const customCount = ref(7)

// 计算当前实际选择的人数，优先使用选中的快捷人数，否则使用自定义人数
const currentCount = computed(() => {
  return selectedCount.value || customCount.value
})

// 处理快捷人数选择点击事件
const selectCount = (num) => {
  selectedCount.value = num
}

// 处理自定义人数变化事件，一旦变化就清空快捷人数的选中状态
const handleCustomChange = (val) => {
  selectedCount.value = null // 清除快速选项的选中状态
}

// 确认点餐人数并跳转至菜单页
const startOrdering = () => {
  if (currentCount.value) {
    userStore.setGuestCount(currentCount.value)
    router.push('/client/menu')
  }
}
</script>

<style scoped>
.guest-home-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 140px);
  background-color: transparent;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.welcome-box {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  padding: 56px 64px;
  border-radius: 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  text-align: center;
  max-width: 560px;
  width: 100%;
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.welcome-box:hover {
  transform: translateY(-4px);
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.12);
}

.title {
  color: #1d1d1f;
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.subtitle {
  color: #86868b;
  font-size: 17px;
  margin-bottom: 48px;
  font-weight: 500;
}

.guest-selection {
  margin-bottom: 48px;
}

.quick-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 36px;
}

.guest-btn {
  width: 100%;
  height: 72px;
  border-radius: 20px;
  font-size: 22px;
  font-weight: 600;
  border: none;
  background-color: #f5f5f7;
  color: #1d1d1f;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.guest-btn:hover {
  background-color: #e8e8ed;
  color: #1d1d1f;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}

.guest-btn.active {
  background-color: #1d1d1f;
  color: #ffffff;
  transform: scale(1.02);
  box-shadow: 0 12px 32px rgba(29, 29, 31, 0.3);
}

.custom-option {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: #f5f5f7;
  padding: 16px 24px;
  border-radius: 20px;
  transition: background-color 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.custom-option:hover {
  background: #e8e8ed;
}

.custom-option .label {
  color: #1d1d1f;
  font-size: 17px;
  font-weight: 600;
}

:deep(.el-input-number .el-input__wrapper) {
  border-radius: 14px;
  box-shadow: none !important;
  background-color: transparent;
}

:deep(.el-input-number .el-input__inner) {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
}

:deep(.el-input-number__decrease),
:deep(.el-input-number__increase) {
  background-color: transparent !important;
  border: none !important;
  color: #1d1d1f !important;
  font-size: 18px;
  transition: all 0.3s;
}

:deep(.el-input-number__decrease:hover),
:deep(.el-input-number__increase:hover) {
  color: var(--primary-color) !important;
  transform: scale(1.1);
}

.start-btn {
  width: 100%;
  height: 64px;
  font-size: 20px;
  font-weight: 600;
  border-radius: 20px;
  letter-spacing: 0.5px;
  background-color: #1d1d1f;
  color: #ffffff;
  border: none;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 10px 30px rgba(29, 29, 31, 0.25);
}

.start-btn:hover:not(:disabled) {
  background-color: #333336;
  color: #ffffff;
  transform: translateY(-2px);
  box-shadow: 0 14px 40px rgba(29, 29, 31, 0.35);
}

.start-btn:active:not(:disabled) {
  transform: scale(0.98);
  box-shadow: 0 6px 16px rgba(29, 29, 31, 0.2);
}

.start-btn:disabled {
  box-shadow: none;
  background-color: #e8e8ed;
  color: #86868b;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .guest-home-container {
    align-items: flex-start;
    min-height: calc(100vh - 132px);
    padding: 12px 0 24px;
  }

  .welcome-box {
    padding: 36px 24px;
    border-radius: 28px;
    max-width: none;
  }

  .title {
    font-size: 28px;
  }

  .subtitle {
    margin-bottom: 32px;
  }

  .quick-options {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
    margin-bottom: 24px;
  }

  .guest-btn {
    height: 60px;
    min-height: 44px;
    font-size: 18px;
  }

  .custom-option {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .start-btn {
    height: 56px;
    min-height: 44px;
  }
}

@media (max-width: 480px) {
  .welcome-box {
    padding: 28px 18px;
    border-radius: 24px;
  }

  .title {
    font-size: 24px;
  }
}
</style>
