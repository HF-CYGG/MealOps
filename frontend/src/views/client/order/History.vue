<!--
  C端历史订单页
  展示用户的历史订单列表及详情，支持再来一单功能
-->
<template>
  <div class="history-order-container fade-slide-enter-active">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>历史订单</span>
        </div>
      </template>

      <!-- 订单列表 -->
      <el-skeleton :loading="loading" animated :count="3">
        <template #template>
          <div style="padding: 20px; margin-bottom: 20px; border: 1px solid #eee; border-radius: 8px;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 16px;">
              <el-skeleton-item variant="text" style="width: 30%" />
              <el-skeleton-item variant="text" style="width: 20%" />
            </div>
            <div style="display: flex; gap: 16px; margin-bottom: 16px;">
              <el-skeleton-item variant="image" style="width: 60px; height: 60px; border-radius: 4px;" />
              <div style="flex: 1;">
                <el-skeleton-item variant="p" style="width: 80%" />
                <el-skeleton-item variant="p" style="width: 60%" />
              </div>
            </div>
            <div style="display: flex; justify-content: flex-end;">
              <el-skeleton-item variant="button" style="width: 80px;" />
            </div>
          </div>
        </template>
        <template #default>
          <div class="order-list">
            <el-card v-for="order in orderList" :key="order.id" class="order-item" shadow="hover">
          <div class="order-header">
            <span class="order-time">下单时间: {{ order.orderTime }}</span>
            <span class="order-status" :class="statusClass(order.status)">
              {{ statusText(order.status) }}
            </span>
          </div>
          
          <div class="order-content">
            <div class="order-info">
              <p>订单号: {{ order.number }}</p>
              <p>收货人: {{ order.consignee }} ({{ order.phone }})</p>
              <p>收货地址: {{ order.address }}</p>
            </div>
            <div class="order-amount">
              <span>实付金额:</span>
              <span class="price">￥{{ order.amount }}</span>
            </div>
          </div>

          <div class="order-footer">
            <el-button @click="handleDetail(order.id)">查看详情</el-button>
            <el-button
              v-if="canPayOrder(order)"
              type="success"
              plain
              :loading="payingOrderId === order.id"
              @click="handlePayOrder(order)"
            >
              去支付 {{ remainingPaymentTime(order) }}
            </el-button>
            <span v-else-if="isExpiredPendingOrder(order)" class="payment-expired">支付超时</span>
            <el-button type="primary" plain @click="handleRepetition(order.id)">再来一单</el-button>
          </div>
        </el-card>

        <el-empty v-if="orderList.length === 0 && !loading" description="暂无历史订单" />

        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @update:current-page="handlePageChange"
          />
        </div>
      </div>
      </template>
      </el-skeleton>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="dialogVisible" title="订单详情" width="600px">
      <div v-loading="detailLoading" v-if="orderDetail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <p>订单号：{{ orderDetail.number }}</p>
          <p>下单时间：{{ orderDetail.orderTime }}</p>
          <p>状态：{{ statusText(orderDetail.status) }}</p>
        </div>
        <el-divider />
        <div class="detail-section">
          <h4>收货信息</h4>
          <p>收货人：{{ orderDetail.consignee }}</p>
          <p>手机号：{{ orderDetail.phone }}</p>
          <p>收货地址：{{ orderDetail.address }}</p>
          <p>备注：{{ orderDetail.remark || '无' }}</p>
        </div>
        <el-divider />
        <div class="detail-section">
          <h4>商品明细</h4>
          <el-table :data="orderDetail.orderDetailList" style="width: 100%">
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="number" label="数量" width="80" align="center" />
            <el-table-column label="单价" width="100" align="right">
              <template #default="scope">
                ￥{{ scope.row.amount }}
              </template>
            </el-table-column>
          </el-table>
          <div class="detail-total">
            实付：<span class="price">￥{{ orderDetail.amount }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  confirmPayment,
  getHistoryOrders,
  getOrderDetail,
  getPaymentByOrder,
  prepayOrder,
  repetitionOrder
} from '@/api/clientOrder'

const router = useRouter()

const loading = ref(false)
const orderList = ref([])
const page = ref(1)
const pageSize = ref(5)
const total = ref(0)

const dialogVisible = ref(false)
const detailLoading = ref(false)
const orderDetail = ref(null)
const payingOrderId = ref(null)
const nowTick = ref(Date.now())
let countdownTimer = null

onMounted(() => {
  fetchOrders()
  countdownTimer = window.setInterval(() => {
    nowTick.value = Date.now()
  }, 1000)
})

onUnmounted(() => {
  if (countdownTimer) {
    window.clearInterval(countdownTimer)
  }
})

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getHistoryOrders({
      page: page.value,
      pageSize: pageSize.value
    })
    if (res.code === 1) {
      orderList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取订单列表失败', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (val) => {
  page.value = val
  fetchOrders()
}

// 订单状态文本
const statusText = (status) => {
  const map = {
    1: '待付款',
    2: '待接单',
    3: '已接单',
    4: '派送中',
    5: '已完成',
    6: '已取消'
  }
  return map[status] || '未知状态'
}

// 订单状态颜色类
const statusClass = (status) => {
  const map = {
    1: 'text-warning',
    2: 'text-warning',
    3: 'text-primary',
    4: 'text-primary',
    5: 'text-success',
    6: 'text-info'
  }
  return map[status] || ''
}

const paymentDeadline = (order) => {
  if (!order?.orderTime) {
    return 0
  }
  const orderTime = new Date(String(order.orderTime).replace(' ', 'T')).getTime()
  if (Number.isNaN(orderTime)) {
    return 0
  }
  return orderTime + 15 * 60 * 1000
}

const paymentRemainingMs = (order) => {
  return Math.max(0, paymentDeadline(order) - nowTick.value)
}

const remainingPaymentTime = (order) => {
  const remaining = paymentRemainingMs(order)
  const minutes = Math.floor(remaining / 60000)
  const seconds = Math.floor((remaining % 60000) / 1000)
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

const canPayOrder = (order) => {
  return order?.status === 1 && order?.payStatus !== 1 && paymentRemainingMs(order) > 0
}

const isExpiredPendingOrder = (order) => {
  return order?.status === 1 && order?.payStatus !== 1 && paymentRemainingMs(order) === 0
}

// 查看详情
const handleDetail = async (id) => {
  dialogVisible.value = true
  detailLoading.value = true
  try {
    const res = await getOrderDetail(id)
    if (res.code === 1) {
      orderDetail.value = res.data
    }
  } catch (error) {
    console.error('获取订单详情失败', error)
  } finally {
    detailLoading.value = false
  }
}

const handlePayOrder = async (order) => {
  if (!canPayOrder(order)) {
    ElMessage.warning('订单已超过支付保留时间')
    await fetchOrders()
    return
  }
  payingOrderId.value = order.id
  try {
    let payment
    try {
      const paymentRes = await getPaymentByOrder(order.id)
      if (paymentRes.code === 1) {
        payment = paymentRes.data
      }
    } catch (error) {
      const prepayRes = await prepayOrder(order.id)
      if (prepayRes.code === 1) {
        payment = prepayRes.data
      }
    }
    const paymentId = payment?.paymentId || payment?.id
    if (!paymentId) {
      ElMessage.error('未获取到支付单')
      return
    }
    const res = await confirmPayment(paymentId)
    if (res.code === 1) {
      ElMessage.success('支付成功')
      await fetchOrders()
    } else {
      ElMessage.error(res.msg || '支付失败')
    }
  } catch (error) {
    console.error('支付失败', error)
    await fetchOrders()
  } finally {
    payingOrderId.value = null
  }
}

// 再来一单
const handleRepetition = async (id) => {
  try {
    const res = await repetitionOrder(id)
    if (res.code === 1) {
      ElMessage.success('商品已加入购物车')
      router.push('/client/menu')
    }
  } catch (error) {
    console.error('再来一单失败', error)
  }
}
</script>

<style scoped>
.history-order-container {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.order-item {
  margin-bottom: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 15px;
}

.order-time {
  color: #666;
}

.order-status {
  font-weight: bold;
}

.text-warning { color: #E6A23C; }
.text-primary { color: var(--primary-color); }
.text-success { color: #67C23A; }
.text-info { color: #909399; }

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.order-info p {
  margin: 5px 0;
  color: #333;
}

.order-amount {
  font-size: 16px;
}

.order-amount .price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
  margin-left: 10px;
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
}

.payment-expired {
  color: #909399;
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  color: #333;
  border-left: 3px solid var(--primary-color);
  padding-left: 10px;
}

.detail-section p {
  margin: 5px 0;
  color: #666;
}

.detail-total {
  text-align: right;
  margin-top: 15px;
  font-size: 16px;
}

.detail-total .price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .history-order-container {
    max-width: none;
  }

  .order-header,
  .order-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .order-amount {
    width: 100%;
    display: flex;
    justify-content: space-between;
  }

  .order-footer {
    flex-direction: column;
  }

  .order-footer .el-button {
    width: 100%;
    min-height: 44px;
    margin-left: 0;
  }

  .detail-section {
    overflow-x: auto;
  }

  .detail-section .el-table {
    min-width: 420px;
  }
}
</style>
