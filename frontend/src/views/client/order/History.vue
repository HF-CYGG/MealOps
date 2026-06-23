<!--
  C端历史订单页
  展示用户的历史订单列表及详情，支持再来一单功能
-->
<template>
  <div class="history-order-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>历史订单</span>
        </div>
      </template>

      <div class="order-list" v-loading="loading">
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
            @current-change="fetchOrders"
          />
        </div>
      </div>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHistoryOrders, getOrderDetail, repetitionOrder } from '@/api/clientOrder'

const router = useRouter()

const loading = ref(false)
const orderList = ref([])
const page = ref(1)
const pageSize = ref(5)
const total = ref(0)

const dialogVisible = ref(false)
const detailLoading = ref(false)
const orderDetail = ref(null)

onMounted(() => {
  fetchOrders()
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
.text-primary { color: #409EFF; }
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
  gap: 10px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  color: #333;
  border-left: 3px solid #409EFF;
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
</style>
