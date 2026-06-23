<!--
  C端用户登录页
  提供用户输入手机号进行登录的功能
-->
<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>欢迎登录 MealOps</span>
        </div>
      </template>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="0"
        size="large"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="loginForm.phone"
            placeholder="请输入手机号"
            prefix-icon="Iphone"
            clearable
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone } from '@element-plus/icons-vue'
import { userLogin } from '@/api/user'
import { useClientUserStore } from '@/store/clientUser'

const router = useRouter()
const route = useRoute()
const userStore = useClientUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  phone: ''
})

const loginRules = reactive({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await userLogin({ phone: loginForm.phone })
        if (res.code === 1) {
          ElMessage.success('登录成功')
          userStore.setToken(res.data.token)
          userStore.setUserInfo({ phone: loginForm.phone, ...res.data })
          
          const redirect = route.query.redirect || '/client/menu'
          router.push(redirect)
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
  background-color: #f0f2f5;
  background-image: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-card {
  width: 400px;
  border-radius: 8px;
}

.card-header {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.login-btn {
  width: 100%;
  font-size: 16px;
  letter-spacing: 2px;
}
</style>
