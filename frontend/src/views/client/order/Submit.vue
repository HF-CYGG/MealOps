<!--
  C端订单结算页
  展示购物车商品列表、填写收货地址或备注，并提交订单
-->
<template>
  <div class="submit-order-container fade-slide-enter-active">
    <el-card class="order-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-button class="order-back-btn" @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span>确认订单</span>
        </div>
      </template>

      <!-- 收货信息 -->
      <div class="section">
        <div class="section-title-row">
          <h3>收货信息</h3>
          <div v-if="userStore.token" class="address-title-actions">
            <el-button link type="primary" @click="router.push('/client/address')">管理地址</el-button>
            <el-button link type="primary" @click="router.push('/client/address')">新增地址</el-button>
          </div>
        </div>
        <div v-if="userStore.token" class="saved-addresses">
          <div
            v-for="address in addressList"
            :key="address.id"
            :class="['saved-address-card', selectedAddressId === address.id ? 'is-selected' : '']"
            tabindex="0"
            @click="selectAddress(address)"
            @keydown.enter="selectAddress(address)"
            @keydown.space.prevent="selectAddress(address)"
          >
            <div class="saved-address-main">
              <strong>{{ address.consignee }}</strong>
              <span>{{ address.phone }}</span>
              <em v-if="address.isDefault === 1">默认</em>
            </div>
            <p>{{ formatAddressBookDetail(address) }}</p>
          </div>
          <button
            v-if="addressList.length === 0"
            class="saved-address-empty"
            type="button"
            @click="router.push('/client/address')"
          >
            还没有收货地址，去新增
          </button>
        </div>
        <el-form
          ref="orderFormRef"
          :model="orderForm"
          :rules="orderRules"
          label-width="80px"
          size="large"
          class="custom-form"
        >
          <el-form-item label="收货人" prop="consignee" required>
            <el-input v-model.trim="orderForm.consignee" placeholder="请输入收货人姓名" @input="selectedAddressId = null" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone" required>
            <el-input
              v-model.trim="orderForm.phone"
              placeholder="请输入收货人手机号"
              maxlength="11"
              inputmode="tel"
              @input="selectedAddressId = null"
            />
          </el-form-item>
          <el-form-item label="收货地址" prop="address" required>
            <el-input v-model.trim="orderForm.address" placeholder="请输入收货地址" @input="selectedAddressId = null" />
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
import { ArrowLeft } from '@element-plus/icons-vue'
import { useCartStore } from '@/store/cart'
import { submitOrder } from '@/api/clientOrder'
import {
  createAddressBook,
  getAddressBookList,
  getDefaultAddressBook,
  setDefaultAddressBook
} from '@/api/addressBook'
import { useClientUserStore } from '@/store/clientUser'
import { userLogin } from '@/api/user'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useClientUserStore()

const cartList = ref([])
const addressList = ref([])
const selectedAddressId = ref(null)
const submitting = ref(false)
const orderFormRef = ref(null)

const PHONE_PATTERN = /^1[3-9]\d{9}$/
const ORDER_CONTEXT_KEY = 'orderContext'
const DELIVERY_INFO_KEY_PREFIX = 'clientDeliveryInfo'

const orderForm = reactive({
  consignee: '',
  phone: '',
  address: '',
  remark: ''
})

const orderRules = {
  consignee: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入收货人手机号', trigger: 'blur' },
    { pattern: PHONE_PATTERN, message: '请输入 11 位有效手机号', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ]
}

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

onMounted(async () => {
  fetchCartList()
  if (userStore.token) {
    await fetchAddressList()
  }
  await restoreDeliveryInfo()
})

const getDeliveryInfoStorageKey = () => {
  const userId = userStore.userInfo?.id || userStore.userInfo?.phone || 'guest'
  return `${DELIVERY_INFO_KEY_PREFIX}:${userId}`
}

const pickDeliveryInfo = (source = {}) => ({
  consignee: source.consignee || '',
  phone: source.phone || '',
  address: source.address || source.detail || '',
  remark: source.remark || ''
})

const applyDeliveryInfo = (source) => {
  if (!source) return false
  const info = pickDeliveryInfo(source)
  Object.assign(orderForm, info)
  if (!source.id) {
    selectedAddressId.value = null
  }
  return Boolean(info.consignee || info.phone || info.address || info.remark)
}

const readSavedDeliveryInfo = (key) => {
  const saved = localStorage.getItem(key)
  if (!saved) return null
  try {
    return JSON.parse(saved)
  } catch (error) {
    localStorage.removeItem(key)
    return null
  }
}

const formatAddressBookDetail = (addressBook) => {
  return [
    addressBook.provinceName,
    addressBook.cityName,
    addressBook.districtName,
    addressBook.detail
  ].filter(Boolean).join('')
}

const fetchAddressList = async () => {
  try {
    const res = await getAddressBookList()
    if (res.code === 1) {
      addressList.value = res.data || []
    }
  } catch (error) {
    console.error('获取收货地址失败', error)
  }
}

const selectAddress = (address, shouldSave = true) => {
  selectedAddressId.value = address.id
  Object.assign(orderForm, {
    consignee: address.consignee || '',
    phone: address.phone || '',
    address: formatAddressBookDetail(address),
    remark: orderForm.remark || ''
  })
  if (shouldSave) {
    saveDeliveryInfo()
  }
}

const getSelectedAddress = () => {
  return addressList.value.find(item => item.id === selectedAddressId.value)
}

const isSelectedAddressUnchanged = () => {
  const selected = getSelectedAddress()
  if (!selected) return false
  return selected.consignee === orderForm.consignee.trim()
    && selected.phone === orderForm.phone.trim()
    && formatAddressBookDetail(selected) === orderForm.address.trim()
}

const restoreDeliveryInfo = async () => {
  const savedContext = readSavedDeliveryInfo(ORDER_CONTEXT_KEY)
  if (applyDeliveryInfo(savedContext)) {
    return
  }

  const hasSavedDelivery = applyDeliveryInfo(readSavedDeliveryInfo(getDeliveryInfoStorageKey()))
  if (hasSavedDelivery) {
    return
  }

  if (!userStore.token) {
    return
  }

  const localDefault = addressList.value.find(item => item.isDefault === 1)
  if (localDefault && !orderForm.consignee && !orderForm.phone && !orderForm.address) {
    selectAddress(localDefault, false)
    return
  }

  try {
    const res = await getDefaultAddressBook()
    if (res.code === 1 && res.data) {
      selectAddress(res.data, false)
    }
  } catch (error) {
    console.error('恢复默认收货信息失败', error)
  }
}

const saveDeliveryInfo = () => {
  const info = pickDeliveryInfo(orderForm)
  localStorage.setItem(getDeliveryInfoStorageKey(), JSON.stringify(info))
}

const redirectToClientLogin = () => {
  saveDeliveryInfo()
  localStorage.setItem(ORDER_CONTEXT_KEY, JSON.stringify(pickDeliveryInfo(orderForm)))
  router.push({
    path: '/client/login',
    query: { redirect: router.currentRoute.value.fullPath || '/client/order/submit' }
  })
}

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
      await doSubmitOrder()
    }
  } catch (error) {
    console.error('登录失败', error)
  } finally {
    loginLoading.value = false
  }
}

const handleSubmit = async () => {
  const valid = await orderFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  // 检查是否登录
  if (!userStore.token) {
    ElMessage.warning('请先登录后再下单')
    redirectToClientLogin()
    return
  }

  await doSubmitOrder()
}

const buildAddressBookPayload = () => ({
  consignee: orderForm.consignee.trim(),
  phone: orderForm.phone.trim(),
  detail: orderForm.address.trim(),
  label: '下单地址'
})

const buildOrderRemark = () => {
  const remark = orderForm.remark.trim()
  const guestText = userStore.guestCount ? `（就餐人数：${userStore.guestCount}人）` : ''
  return [remark, guestText].filter(Boolean).join(' ')
}

const doSubmitOrder = async () => {
  submitting.value = true
  try {
    const addressBookId = await resolveAddressBookId()
    if (!addressBookId) {
      ElMessage.error('收货地址保存失败，请重试')
      return
    }
    saveDeliveryInfo()
    try {
      await setDefaultAddressBook(addressBookId)
    } catch (error) {
      console.error('设置默认收货地址失败', error)
    }

    const data = {
      addressBookId,
      payMethod: 1, // 1: 微信支付
      remark: buildOrderRemark()
    }
    
    const res = await submitOrder(data)
    if (res.code === 1) {
      ElMessage.success('下单成功')
      localStorage.removeItem(ORDER_CONTEXT_KEY)
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

const resolveAddressBookId = async () => {
  if (selectedAddressId.value && isSelectedAddressUnchanged()) {
    return selectedAddressId.value
  }
  const addressRes = await createAddressBook(buildAddressBookPayload())
  return addressRes.data?.id
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
  gap: 12px;
  font-size: 18px;
  font-weight: bold;
}

.order-back-btn {
  min-width: 76px;
  min-height: 40px;
  padding: 0 14px;
  border: 1px solid rgba(31, 41, 55, 0.1);
  border-radius: 999px;
  background: #f7f8fa;
  color: #1f2937;
  font-weight: 800;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.order-back-btn:hover,
.order-back-btn:focus-visible {
  border-color: rgba(64, 158, 255, 0.45);
  background: #fff;
  color: var(--el-color-primary, #409eff);
  box-shadow: 0 10px 24px rgba(64, 158, 255, 0.14);
}

.order-back-btn:active {
  transform: scale(0.96);
}

.order-back-btn :deep(.el-icon) {
  margin-right: 4px;
}

.card-header span {
  margin-left: 0;
}

.section {
  margin-bottom: 40px;
}

.section-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.section h3 {
  margin-bottom: 24px;
  padding-left: 12px;
  border-left: 4px solid var(--el-color-primary, #FF6B00);
  font-size: 18px;
  color: #333;
}

.address-title-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 44px;
}

.address-title-actions .el-button {
  min-height: 36px;
  margin-left: 0;
  font-weight: 700;
}

.saved-addresses {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
  margin: -8px 0 22px;
}

.saved-address-card,
.saved-address-empty {
  min-height: 96px;
  border: 1px solid rgba(28, 28, 30, 0.08);
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 8px 22px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.saved-address-card {
  padding: 14px 16px;
}

.saved-address-card:hover,
.saved-address-card:focus-visible,
.saved-address-empty:hover,
.saved-address-empty:focus-visible {
  transform: translateY(-2px);
  border-color: rgba(64, 158, 255, 0.55);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
  outline: none;
}

.saved-address-card.is-selected {
  border-color: var(--el-color-primary, #409eff);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.12), 0 12px 28px rgba(0, 0, 0, 0.08);
}

.saved-address-main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.saved-address-main strong {
  color: #1f2937;
  font-size: 15px;
}

.saved-address-main span {
  color: #6b7280;
  font-size: 13px;
  font-weight: 700;
}

.saved-address-main em {
  margin-left: auto;
  padding: 2px 8px;
  border-radius: 999px;
  background: #ecfdf3;
  color: #16833d;
  font-size: 12px;
  font-style: normal;
  font-weight: 800;
}

.saved-address-card p {
  margin: 10px 0 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
}

.saved-address-empty {
  width: 100%;
  color: var(--el-color-primary, #409eff);
  font-weight: 800;
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

  .section-title-row {
    flex-direction: column;
    gap: 4px;
  }

  .address-title-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .saved-addresses {
    grid-template-columns: 1fr;
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

@media (prefers-reduced-motion: reduce) {
  .order-back-btn,
  .saved-address-card,
  .saved-address-empty {
    transition: none;
  }

  .order-back-btn:active,
  .saved-address-card:hover,
  .saved-address-card:focus-visible,
  .saved-address-empty:hover,
  .saved-address-empty:focus-visible {
    transform: none;
  }
}
</style>
