<!--
  C端订单结算页
  展示购物车商品列表、填写收货地址或备注，并提交订单
-->
<template>
  <div class="submit-order-container fade-slide-enter-active">
    <el-card class="order-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-button icon="ArrowLeft" link @click="$router.back()">返回</el-button>
          <span>确认订单</span>
        </div>
      </template>

      <!-- 收货信息 -->
      <div class="section">
        <h3>收货信息</h3>
        <el-form :model="orderForm" label-width="80px" size="large" class="custom-form">
          <el-form-item label="收货人" required>
            <el-input v-model="orderForm.consignee" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="手机号" required>
            <el-input v-model="orderForm.phone" placeholder="请输入收货人手机号" />
          </el-form-item>
          <el-form-item label="收货地址" required>
            <el-input v-model="orderForm.address" placeholder="请输入收货地址" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="orderForm.remark" type="textarea" placeholder="有特殊要求请留言" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 订单详情 -->
      <div class="section">
        <h3>订单详情</h3>
        <el-table :data="cartList" style="width: 100%" class="custom-table">
          <el-table-column prop="name" label="商品名称" />
          <el-table-column prop="number" label="数量" width="100" align="center" />
          <el-table-column label="单价" width="120" align="right">
            <template #default="scope">
              ￥{{ scope.row.amount }}
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120" align="right">
            <template #default="scope">
              ￥{{ (scope.row.amount * scope.row.number).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
        <div class="order-total">
          总计: <span class="total-price">￥{{ totalPrice.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 底部提交栏 -->
      <div class="bottom-bar">
        <div class="total-info">
          应付金额: <span class="price">￥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <el-button type="primary" size="large" class="submit-btn" :loading="submitting" @click="handleSubmit">
          提交订单
        </el-button>
      </div>
    </el-card>

    <!-- 登录拦截弹窗 -->
    <el-dialog
      v-model="loginDialogVisible"
      title="请先登录"
      width="400px"
      class="login-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="loginForm" label-width="80px" size="large">
        <el-form-item label="手机号">
          <el-input v-model="loginForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="loginDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="loginLoading" @click="handleLogin">登录并继续</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/store/cart'
import { submitOrder } from '@/api/clientOrder'
import { useClientUserStore } from '@/store/clientUser'
import { userLogin } from '@/api/user'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useClientUserStore()

const cartList = ref([])
const submitting = ref(false)

const orderForm = reactive({
  consignee: '',
  phone: '',
  address: '',
  remark: ''
})

const totalPrice = computed(() => {
  return cartList.value.reduce((sum, item) => sum + (item.amount * item.number), 0)
})

// 登录相关
const loginDialogVisible = ref(false)
const loginLoading = ref(false)
const loginForm = reactive({
  phone: '',
  password: ''
})

onMounted(() => {
  fetchCartList()
  
  // 恢复之前保存的订单上下文
  const savedContext = localStorage.getItem('orderContext')
  if (savedContext) {
    try {
      const parsed = JSON.parse(savedContext)
      Object.assign(orderForm, parsed)
    } catch (e) {}
  }
})

const fetchCartList = async () => {
  try {
    cartList.value = await cartStore.fetchCartList()
    if (cartList.value.length === 0) {
      ElMessage.warning('购物车为空，请先选购商品')
      router.push('/client/menu')
    }
  } catch (error) {
    console.error('获取购物车失败', error)
  }
}

const handleLogin = async () => {
  if (!loginForm.phone || !loginForm.password) {
    ElMessage.warning('请输入手机号和密码')
    return
  }
  
  loginLoading.value = true
  try {
    const res = await userLogin(loginForm)
    if (res.code === 1) {
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data)
      ElMessage.success('登录成功')
      loginDialogVisible.value = false
      
      // 同步本地购物车到服务端
      await cartStore.syncCartToServer()
      
      // 继续提交订单
      doSubmitOrder()
    }
  } catch (error) {
    console.error('登录失败', error)
  } finally {
    loginLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!orderForm.consignee || !orderForm.phone || !orderForm.address) {
    ElMessage.warning('请填写完整的收货信息')
    return
  }

  // 检查是否登录
  if (!userStore.token) {
    // 保存上下文
    localStorage.setItem('orderContext', JSON.stringify(orderForm))
    loginDialogVisible.value = true
    return
  }

  doSubmitOrder()
}

const doSubmitOrder = async () => {
  submitting.value = true
  try {
    const data = {
      ...orderForm,
      amount: totalPrice.value,
      payMethod: 1 // 1: 微信支付
    }
    // 提交订单时可能需要附带人数
    if (userStore.guestCount) {
      data.remark = (data.remark ? data.remark + ' ' : '') + `(就餐人数：${userStore.guestCount}人)`
    }
    
    const res = await submitOrder(data)
    if (res.code === 1) {
      ElMessage.success('下单成功')
      localStorage.removeItem('orderContext')
      // 清空本地和远端购物车
      await cartStore.cleanCart()
      router.push('/client/order/history')
    }
  } catch (error) {
    console.error('提交订单失败', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.submit-order-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}

.order-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.card-header span {
  margin-left: 10px;
}

.section {
  margin-bottom: 40px;
}

.section h3 {
  margin-bottom: 24px;
  padding-left: 12px;
  border-left: 4px solid var(--el-color-primary, #FF6B00);
  font-size: 18px;
  color: #333;
}

.custom-form :deep(.el-input__wrapper) {
  box-shadow: none;
  background-color: #F7F8FA;
  border-radius: 8px;
}

.custom-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary, #FF6B00) inset;
}

.custom-form :deep(.el-textarea__inner) {
  box-shadow: none;
  background-color: #F7F8FA;
  border-radius: 8px;
}

.custom-form :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary, #FF6B00) inset;
}

.custom-table {
  border-radius: 8px;
  overflow: hidden;
}

.order-total {
  text-align: right;
  margin-top: 24px;
  font-size: 16px;
  color: #666;
}

.total-price {
  color: #F53F3F;
  font-size: 24px;
  font-weight: bold;
  margin-left: 10px;
  font-family: sans-serif;
}

.bottom-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #F0F2F5;
}

.total-info {
  margin-right: 24px;
  font-size: 16px;
  color: #666;
}

.total-info .price {
  color: #F53F3F;
  font-size: 28px;
  font-weight: bold;
  font-family: sans-serif;
}

.submit-btn {
  width: 160px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: bold;
  background: var(--el-color-primary, #FF6B00);
  border: none;
  transition: transform 0.2s;
}

.submit-btn:active {
  transform: scale(0.96);
}

.login-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

.login-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  border-bottom: 1px solid #f0f2f5;
  padding-bottom: 20px;
}

.login-dialog :deep(.el-input__wrapper) {
  background-color: #F7F8FA;
  box-shadow: none;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .submit-order-container {
    max-width: none;
    padding: 0;
  }

  .order-card {
    border-radius: 20px;
  }

  .card-header {
    font-size: 17px;
  }

  .section {
    margin-bottom: 28px;
  }

  .custom-form :deep(.el-form-item) {
    display: block;
  }

  .custom-form :deep(.el-form-item__label) {
    justify-content: flex-start;
    width: auto !important;
    margin-bottom: 8px;
  }

  .custom-form :deep(.el-form-item__content) {
    margin-left: 0 !important;
  }

  .custom-table {
    min-width: 520px;
  }

  .section {
    overflow-x: auto;
  }

  .bottom-bar {
    position: sticky;
    bottom: 0;
    z-index: 20;
    flex-direction: column;
    align-items: stretch;
    gap: 14px;
    margin: 28px -8px -8px;
    padding: 16px;
    border-radius: 22px;
    background: rgba(255, 255, 255, 0.92);
    backdrop-filter: blur(16px);
  }

  .total-info {
    margin-right: 0;
    display: flex;
    justify-content: space-between;
    align-items: baseline;
  }

  .submit-btn {
    width: 100%;
    min-height: 48px;
  }
}
</style>
