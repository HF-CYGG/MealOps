<!--
  C端用户登录页
  提供用户输入手机号进行登录的功能
-->
<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="card-topbar">
          <button class="back-button" type="button" aria-label="返回订餐首页" @click="handleBack">
            <el-icon :size="18">
              <ArrowLeft />
            </el-icon>
            <span>返回</span>
          </button>
        </div>
        <div class="card-header">
          <span>MealOps 订餐</span>
        </div>
        <p class="subtitle">请输入手机号以继续点餐</p>
      </template>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="0"
        size="large"
        class="custom-form"
      >
        <el-tabs v-model="activeTab" class="custom-tabs" stretch>
          <el-tab-pane label="密码登录" name="pwd">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="用户名/手机号"
                prefix-icon="User"
                clearable
                class="custom-input"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                placeholder="密码"
                prefix-icon="Lock"
                type="password"
                show-password
                clearable
                class="custom-input"
              />
            </el-form-item>
          </el-tab-pane>
          <el-tab-pane label="验证码登录" name="code">
            <el-form-item prop="phone">
              <el-input
                v-model="loginForm.phone"
                placeholder="手机号码"
                prefix-icon="Iphone"
                clearable
                maxlength="11"
                class="custom-input"
              />
            </el-form-item>
            
            <el-form-item prop="code">
              <div class="code-wrapper">
                <el-input
                  v-model="loginForm.code"
                  placeholder="验证码 (任意6位)"
                  prefix-icon="Key"
                  clearable
                  maxlength="6"
                  class="custom-input code-input"
                />
                <el-button 
                  class="send-code-btn" 
                  :disabled="countdown > 0 || !isPhoneValid" 
                  @click="sendCode"
                >
                  {{ countdown > 0 ? `${countdown}s 后重新获取` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
          </el-tab-pane>
          <el-tab-pane label="新用户注册" name="register">
            <el-form-item prop="regUsername">
              <el-input
                v-model="loginForm.regUsername"
                placeholder="设置用户名"
                prefix-icon="User"
                clearable
                class="custom-input"
              />
            </el-form-item>
            <el-form-item prop="regPhone">
              <el-input
                v-model="loginForm.regPhone"
                placeholder="手机号码"
                prefix-icon="Iphone"
                clearable
                maxlength="11"
                class="custom-input"
              />
            </el-form-item>
            <el-form-item prop="regPassword">
              <el-input
                v-model="loginForm.regPassword"
                placeholder="设置密码"
                prefix-icon="Lock"
                type="password"
                show-password
                clearable
                class="custom-input"
              />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ submitButtonText }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Iphone, Key, User, Lock } from '@element-plus/icons-vue'
import { userLogin } from '@/api/user'
import { useClientUserStore } from '@/store/clientUser'
import { useCartStore } from '@/store/cart'

const router = useRouter()
const route = useRoute()
const userStore = useClientUserStore()
const cartStore = useCartStore()

const loginFormRef = ref(null)
const loading = ref(false)
const countdown = ref(0)
let timer = null

const activeTab = ref('pwd') // pwd: 密码登录, code: 验证码登录, register: 注册

const loginForm = reactive({
  phone: '',
  code: '',
  username: '',
  password: '',
  regUsername: '',
  regPhone: '',
  regPassword: ''
})

const submitButtonText = computed(() => {
  return activeTab.value === 'register' ? '立即注册并点餐' : '开始点餐'
})

const isPhoneValid = computed(() => {
  return /^1[3-9]\d{9}$/.test(loginForm.phone)
})

const handleBack = () => {
  router.push('/client/home')
}

const loginRules = reactive({
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号码格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 6, max: 6, message: '验证码为6位', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名或手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  regUsername: [
    { required: true, message: '请设置用户名', trigger: 'blur' }
  ],
  regPhone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号码格式不正确', trigger: 'blur' }
  ],
  regPassword: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
})

const sendCode = () => {
  if (!isPhoneValid.value) return
  ElMessage.success('验证码已发送，测试阶段可输入任意6位数字')
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const handleSubmit = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid, fields) => {
    // 根据当前 Tab 判断需要校验的字段是否通过
    let isCurrentTabValid = true
    if (!valid && fields) {
      const errorFields = Object.keys(fields)
      if (activeTab.value === 'pwd') {
        isCurrentTabValid = !errorFields.includes('username') && !errorFields.includes('password')
      } else if (activeTab.value === 'code') {
        isCurrentTabValid = !errorFields.includes('phone') && !errorFields.includes('code')
      } else if (activeTab.value === 'register') {
        isCurrentTabValid = !errorFields.includes('regUsername') && !errorFields.includes('regPhone') && !errorFields.includes('regPassword')
      }
    }

    if (isCurrentTabValid) {
      loading.value = true
      try {
        let res
        if (activeTab.value === 'register') {
          // TODO: 替换为真实的注册接口
          ElMessage.success('注册功能即将开放，目前演示直接使用手机号模拟登录')
          res = await userLogin({ phone: loginForm.regPhone })
        } else if (activeTab.value === 'pwd') {
          // TODO: 替换为真实的密码登录接口
          ElMessage.success('密码登录即将开放，目前演示直接使用手机号模拟登录')
          // 暂时提取手机号部分模拟登录
          const simulatePhone = /^1[3-9]\d{9}$/.test(loginForm.username) ? loginForm.username : '13900000000'
          res = await userLogin({ phone: simulatePhone })
        } else {
          // 验证码登录
          res = await userLogin({ phone: loginForm.phone })
        }

        if (res && res.code === 1) {
          ElMessage.success('登录成功')
          userStore.setToken(res.data.token)
          userStore.setUserInfo({ 
            phone: activeTab.value === 'register' ? loginForm.regPhone : (activeTab.value === 'pwd' ? loginForm.username : loginForm.phone), 
            ...res.data 
          })
          await cartStore.syncCartToServer()
          
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
  background-color: #f7f9fc;
  background-image: radial-gradient(circle at 50% 0%, #e6f0ff 0%, #f7f9fc 60%);
  animation: fadeIn 0.6s ease-out;
}

.card-topbar {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 10px;
}

.back-button {
  min-width: 82px;
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 0 16px;
  border: 1px solid rgba(29, 29, 31, 0.08);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.82);
  color: #1d1d1f;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  transition: transform 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.12);
  transform: translateY(-1px);
}

.back-button:active {
  transform: scale(0.97);
}

.back-button:focus-visible {
  outline: 3px solid rgba(212, 163, 115, 0.36);
  outline-offset: 3px;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.login-card {
  width: 420px;
  border-radius: 24px;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.06);
  padding: 16px 8px;
  transition: transform 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.login-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 56px rgba(0, 0, 0, 0.08);
}

.subtitle {
  text-align: center;
  color: #8e8e93;
  font-size: 14px;
  margin: 0 0 16px 0;
  letter-spacing: 0.5px;
}

.custom-form {
  padding: 0 12px;
}

:deep(.el-card__header) {
  border-bottom: none;
  padding-bottom: 0;
}

:deep(.custom-tabs .el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #f0f0f2;
}

:deep(.custom-tabs .el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
  color: #8e8e93;
  transition: all 0.3s;
}

:deep(.custom-tabs .el-tabs__item.is-active) {
  font-weight: 700;
  color: #1d1d1f;
}

:deep(.custom-tabs .el-tabs__active-bar) {
  background-color: var(--primary-color);
  height: 3px;
  border-radius: 3px;
}

.card-header {
  text-align: center;
  font-size: 26px;
  font-weight: 800;
  color: #1d1d1f;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

:deep(.custom-input .el-input__wrapper) {
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03) !important;
  background-color: #f5f5f7;
  padding: 8px 16px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid transparent;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06) !important;
  border: 1px solid var(--primary-color);
}

:deep(.custom-input .el-input__inner) {
  font-size: 16px;
  font-weight: 500;
  color: #1d1d1f;
}

.code-wrapper {
  display: flex;
  width: 100%;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  width: 130px;
  border-radius: 14px;
  height: 100%;
  background: #f5f5f7;
  border: none;
  color: var(--primary-color);
  font-weight: 600;
  transition: all 0.3s;
}

.send-code-btn:not(:disabled):hover {
  background: #e5e5ea;
  color: var(--accent-color);
}

.login-btn {
  width: 100%;
  height: 52px;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
  border-radius: 16px;
  margin-top: 24px;
  background: var(--primary-color);
  border: none;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 8px 20px rgba(212, 163, 115, 0.25);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(212, 163, 115, 0.35);
  background: var(--accent-color);
}

.login-btn:active:not(:disabled) {
  transform: scale(0.97);
  box-shadow: 0 4px 12px rgba(212, 163, 115, 0.2);
}

@media (max-width: 768px) {
  .login-container {
    min-height: 100vh;
    height: auto;
    padding: 24px 16px;
  }

  .back-button {
    min-width: 80px;
    min-height: 44px;
    padding: 0 14px;
    font-size: 14px;
  }

  .login-card {
    width: 100%;
    max-width: 420px;
    padding: 8px 0;
  }

  .card-header {
    font-size: 23px;
  }

  :deep(.custom-tabs .el-tabs__item) {
    font-size: 14px;
    padding: 0 8px;
  }

  .code-wrapper {
    flex-direction: column;
    gap: 10px;
  }

  .send-code-btn {
    width: 100%;
    min-height: 44px;
  }
}
</style>
