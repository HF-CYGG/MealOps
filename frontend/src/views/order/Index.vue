<!-- 
  订单管理页面
  包含订单列表展示、状态筛选、接单、拒单、派送、完成、取消等功能
-->
<template>
  <div class="page-container">
    <div class="header-action">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.number" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 150px">
            <el-option label="待付款" :value="1" />
            <el-option label="待接单" :value="2" />
            <el-option label="备餐中" :value="3" />
            <el-option label="出餐中" :value="4" />
            <el-option label="已完成" :value="5" />
            <el-option label="已取消" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-switch
            v-model="autoRefresh"
            class="auto-refresh-switch"
            active-text="自动刷新"
            inactive-text="手动刷新"
            @change="handleAutoRefreshChange"
          />
        </el-form-item>
      </el-form>
    </div>

    <div class="summary-cards">
      <div
        v-for="item in summaryCards"
        :key="item.key"
        class="summary-card"
        :class="{ active: searchForm.status === item.status }"
        @click="handleSummaryClick(item.status)"
      >
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" border style="width: 100%">
      <el-table-column prop="number" label="订单号" align="center" width="180" />
      <el-table-column prop="status" label="订单状态" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payStatus" label="支付状态" align="center">
        <template #default="scope">
          <el-tag :type="getPayStatusType(scope.row.payStatus)">
            {{ getPayStatusText(scope.row.payStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="用户名" align="center" />
      <el-table-column prop="phone" label="手机号" align="center" width="120" />
      <el-table-column prop="address" label="地址" align="center" show-overflow-tooltip />
      <el-table-column prop="orderTime" label="下单时间" align="center" width="160" />
      <el-table-column prop="amount" label="实收金额" align="center">
        <template #default="scope">
          ￥{{ scope.row.amount }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220" fixed="right">
        <template #default="scope">
          <el-button
            type="primary"
            link
            @click="handleDetail(scope.row)"
          >详情</el-button>
          <el-button 
            v-if="scope.row.status === 2 && scope.row.payStatus === 1"
            type="primary" 
            link 
            @click="handleConfirm(scope.row)"
          >接单</el-button>
          <el-button 
            v-if="scope.row.status === 2 && scope.row.payStatus === 1"
            type="danger" 
            link 
            @click="handleRejection(scope.row)"
          >拒单</el-button>
          <el-button 
            v-if="scope.row.status === 3" 
            type="primary" 
            link 
            @click="handleDelivery(scope.row)"
          >开始出餐</el-button>
          <el-button 
            v-if="scope.row.status === 4" 
            type="success" 
            link 
            @click="handleComplete(scope.row)"
          >完成</el-button>
          <el-button 
            v-if="[1, 2, 3, 4, 5].includes(scope.row.status)" 
            type="danger" 
            link 
            @click="handleCancel(scope.row)"
          >取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @update:page-size="handleSizeChange"
        @update:current-page="handleCurrentChange"
      />
    </div>

    <!-- 拒单对话框 -->
    <el-dialog title="拒单原因" v-model="rejectDialogVisible" width="400px">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef">
        <el-form-item prop="rejectionReason">
          <el-input 
            v-model="rejectForm.rejectionReason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入拒单原因" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRejection" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 取消订单对话框 -->
    <el-dialog title="取消原因" v-model="cancelDialogVisible" width="400px">
      <el-form :model="cancelForm" :rules="cancelRules" ref="cancelFormRef">
        <el-form-item prop="cancelReason">
          <el-input 
            v-model="cancelForm.cancelReason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入取消原因" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCancel" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-drawer v-model="detailDrawerVisible" title="订单详情" size="520px">
      <div v-loading="detailLoading" v-if="orderDetail" class="order-detail">
        <div class="detail-line">
          <span>订单号</span>
          <strong>{{ orderDetail.number }}</strong>
        </div>
        <div class="detail-line">
          <span>订单状态</span>
          <el-tag :type="getStatusType(orderDetail.status)">
            {{ getStatusText(orderDetail.status) }}
          </el-tag>
        </div>
        <div class="detail-line">
          <span>支付状态</span>
          <el-tag :type="getPayStatusType(orderDetail.payStatus)">
            {{ getPayStatusText(orderDetail.payStatus) }}
          </el-tag>
        </div>
        <div class="detail-line">
          <span>联系人</span>
          <strong>{{ orderDetail.consignee || orderDetail.userName || '-' }}</strong>
        </div>
        <div class="detail-line">
          <span>手机号</span>
          <strong>{{ orderDetail.phone || '-' }}</strong>
        </div>
        <div class="detail-block">
          <span>地址</span>
          <p>{{ orderDetail.address || '-' }}</p>
        </div>
        <div class="detail-block">
          <span>备注</span>
          <p>{{ orderDetail.remark || '无' }}</p>
        </div>
        <el-table :data="orderDetail.orderDetailList || []" size="small" border>
          <el-table-column prop="name" label="商品" />
          <el-table-column prop="number" label="数量" width="70" align="center" />
          <el-table-column prop="amount" label="单价" width="90" align="right">
            <template #default="scope">￥{{ scope.row.amount }}</template>
          </el-table-column>
        </el-table>
        <div class="detail-total">合计：￥{{ orderDetail.amount }}</div>
      </div>
      <el-empty v-else-if="!detailLoading" description="暂无订单详情" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getOrderPage, 
  getOrderSummary,
  getOrderDetail,
  confirmOrder, 
  rejectionOrder, 
  deliveryOrder, 
  completeOrder, 
  cancelOrder 
} from '@/api/order'

const searchForm = reactive({
  number: '',
  phone: '',
  status: '',
  timeRange: []
})

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const summary = reactive({
  pendingPayment: 0,
  toBeConfirmed: 0,
  preparing: 0,
  serving: 0,
  completed: 0,
  cancelled: 0
})
const autoRefresh = ref(true)
let autoRefreshTimer = null

const rejectDialogVisible = ref(false)
const cancelDialogVisible = ref(false)
const submitLoading = ref(false)
const detailDrawerVisible = ref(false)
const detailLoading = ref(false)
const orderDetail = ref(null)

const rejectFormRef = ref(null)
const rejectForm = reactive({
  id: '',
  rejectionReason: ''
})
const rejectRules = {
  rejectionReason: [{ required: true, message: '请输入拒单原因', trigger: 'blur' }]
}

const cancelFormRef = ref(null)
const cancelForm = reactive({
  id: '',
  cancelReason: ''
})
const cancelRules = {
  cancelReason: [{ required: true, message: '请输入取消原因', trigger: 'blur' }]
}

// 状态映射
const getStatusText = (status) => {
  const map = {
    1: '待付款',
    2: '待接单',
    3: '备餐中',
    4: '出餐中',
    5: '已完成',
    6: '已取消'
  }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {
    1: 'warning',
    2: 'danger',
    3: 'primary',
    4: 'primary',
    5: 'success',
    6: 'info'
  }
  return map[status] || 'info'
}

const getPayStatusText = (payStatus) => {
  const map = {
    0: '未支付',
    1: '已支付'
  }
  return map[payStatus] || '未知'
}

const getPayStatusType = (payStatus) => {
  return payStatus === 1 ? 'success' : 'warning'
}

const summaryCards = computed(() => [
  { key: 'pendingPayment', label: '待付款', status: 1, value: summary.pendingPayment },
  { key: 'toBeConfirmed', label: '待接单', status: 2, value: summary.toBeConfirmed },
  { key: 'preparing', label: '备餐中', status: 3, value: summary.preparing },
  { key: 'serving', label: '出餐中', status: 4, value: summary.serving },
  { key: 'completed', label: '已完成', status: 5, value: summary.completed },
  { key: 'cancelled', label: '已取消', status: 6, value: summary.cancelled }
])

const getList = async () => {
  loading.value = true
  try {
    let beginTime, endTime
    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      beginTime = searchForm.timeRange[0]
      endTime = searchForm.timeRange[1]
    }
    
    const res = await getOrderPage({
      page: page.value,
      pageSize: pageSize.value,
      number: searchForm.number || undefined,
      phone: searchForm.phone || undefined,
      status: searchForm.status || undefined,
      beginTime,
      endTime
    })
    
    if (res.code === 1) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchSummary = async () => {
  try {
    const res = await getOrderSummary()
    if (res.code === 1 && res.data) {
      Object.assign(summary, res.data)
    }
  } catch (error) {
    console.error(error)
  }
}

const refreshOrders = async () => {
  await Promise.all([getList(), fetchSummary()])
}

const handleSearch = () => {
  page.value = 1
  refreshOrders()
}

const resetSearch = () => {
  searchForm.number = ''
  searchForm.phone = ''
  searchForm.status = ''
  searchForm.timeRange = []
  handleSearch()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  refreshOrders()
}

const handleCurrentChange = (val) => {
  page.value = val
  refreshOrders()
}

const handleSummaryClick = (status) => {
  searchForm.status = status
  handleSearch()
}

const startAutoRefresh = () => {
  stopAutoRefresh()
  autoRefreshTimer = window.setInterval(() => {
    refreshOrders()
  }, 30000)
}

const stopAutoRefresh = () => {
  if (autoRefreshTimer) {
    window.clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
}

const handleAutoRefreshChange = (enabled) => {
  if (enabled) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

const handleDetail = async (row) => {
  detailDrawerVisible.value = true
  detailLoading.value = true
  orderDetail.value = null
  try {
    const res = await getOrderDetail(row.id)
    if (res.code === 1) {
      orderDetail.value = res.data
    }
  } catch (error) {
    console.error(error)
  } finally {
    detailLoading.value = false
  }
}

// 接单
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确认接单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await confirmOrder({ id: row.id })
    if (res.code === 1) {
      ElMessage.success('接单成功')
      refreshOrders()
    } else {
      ElMessage.error(res.msg || '接单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// 拒单
const handleRejection = (row) => {
  rejectForm.id = row.id
  rejectForm.rejectionReason = ''
  rejectDialogVisible.value = true
  if (rejectFormRef.value) {
    rejectFormRef.value.clearValidate()
  }
}

const submitRejection = async () => {
  if (!rejectFormRef.value) return
  await rejectFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await rejectionOrder(rejectForm)
        if (res.code === 1) {
          ElMessage.success('拒单成功')
          rejectDialogVisible.value = false
          refreshOrders()
        } else {
          ElMessage.error(res.msg || '拒单失败')
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 出餐
const handleDelivery = async (row) => {
  try {
    await ElMessageBox.confirm('确认该订单已备餐完成并开始出餐吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await deliveryOrder(row.id)
    if (res.code === 1) {
      ElMessage.success('出餐成功')
      refreshOrders()
    } else {
      ElMessage.error(res.msg || '出餐失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// 完成订单
const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('确认该订单已完成吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    const res = await completeOrder(row.id)
    if (res.code === 1) {
      ElMessage.success('操作成功')
      refreshOrders()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// 取消订单
const handleCancel = (row) => {
  cancelForm.id = row.id
  cancelForm.cancelReason = ''
  cancelDialogVisible.value = true
  if (cancelFormRef.value) {
    cancelFormRef.value.clearValidate()
  }
}

const submitCancel = async () => {
  if (!cancelFormRef.value) return
  await cancelFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await cancelOrder(cancelForm)
        if (res.code === 1) {
          ElMessage.success('取消订单成功')
          cancelDialogVisible.value = false
          refreshOrders()
        } else {
          ElMessage.error(res.msg || '取消订单失败')
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  refreshOrders()
  if (autoRefresh.value) {
    startAutoRefresh()
  }
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
/* 页面特有样式（通用样式已在全局 style.css 中定义） */
.auto-refresh-switch {
  margin-left: 12px;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(6, minmax(110px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.summary-card {
  min-height: 72px;
  padding: 12px 14px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.summary-card.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.summary-card span {
  color: #606266;
  font-size: 13px;
}

.summary-card strong {
  color: #303133;
  font-size: 24px;
}

.order-detail {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-line,
.detail-block {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  color: #606266;
}

.detail-block {
  flex-direction: column;
  gap: 6px;
}

.detail-block p {
  margin: 0;
  color: #303133;
  line-height: 1.6;
}

.detail-total {
  text-align: right;
  color: #f56c6c;
  font-size: 18px;
  font-weight: 700;
}

@media (max-width: 768px) {
  .summary-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
