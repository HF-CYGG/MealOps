<!-- 
  管理端登录页面
  包含用户名、密码输入框及登录按钮
-->
<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="title">MealOps 管理系统</h2>
      <el-form 
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        label-width="0"
        size="large"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入账号" 
            prefix-icon="User"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            class="login-btn" 
            :loading="loading" 
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/employee'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '账号不能为空', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 处理登录逻辑
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        if (res.code === 1) {
          ElMessage.success('登录成功')
          // 保存 Token 和用户信息
          userStore.setToken(res.data.token)
          userStore.setUserInfo(res.data)
          // 跳转到首页
          router.push('/')
        } else {
          ElMessage.error(res.msg || '登录失败')
        }
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  /* 浅色高斯模糊渐变背景 */
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
}

/* 增加一些背景装饰图案以提升空间感 */
.login-container::before {
  content: '';
  position: absolute;
  top: -10%;
  left: -10%;
  width: 50%;
  height: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.8) 0%, rgba(255,255,255,0) 70%);
  filter: blur(40px);
  z-index: 0;
}

.login-box {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 50px 40px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.title {
  text-align: center;
  margin-bottom: 40px;
  color: #1c1c1e;
  font-size: 28px;
  font-weight: 600;
  letter-spacing: 1px;
}

/* 优化输入框样式 */
:deep(.el-input__wrapper) {
  background-color: #f5f5f7;
  border-radius: 12px;
  box-shadow: none !important;
  border: 1px solid transparent;
  transition: all 0.3s ease;
  padding: 8px 15px;
}

:deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: #d4a373;
  box-shadow: 0 0 0 2px rgba(212, 163, 115, 0.2) !important;
}

:deep(.el-input__inner) {
  height: 40px;
  color: #2c2c2e;
}

/* 美化表单验证错误提示 */
:deep(.el-form-item__error) {
  color: #ff3b30;
  font-size: 12px;
  padding-top: 4px;
  opacity: 0.8;
  position: absolute;
  top: 100%;
  left: 10px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 12px;
  margin-top: 10px;
  background-color: #1c1c1e;
  border-color: #1c1c1e;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.login-btn:hover, .login-btn:focus {
  background-color: #2c2c2e;
  border-color: #2c2c2e;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.login-btn:active {
  transform: translateY(0);
}

@media (max-width: 768px) {
  .login-container {
    min-height: 100vh;
    height: auto;
    padding: 24px 16px;
  }

  .login-box {
    width: 100%;
    max-width: 420px;
    padding: 38px 24px;
    border-radius: 22px;
  }

  .title {
    font-size: 24px;
    margin-bottom: 32px;
  }

  .login-btn {
    min-height: 48px;
  }
}
</style>
