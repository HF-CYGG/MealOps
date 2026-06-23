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
            <el-option label="已接单" :value="3" />
            <el-option label="派送中" :value="4" />
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
        </el-form-item>
      </el-form>
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
            v-if="scope.row.status === 2" 
            type="primary" 
            link 
            @click="handleConfirm(scope.row)"
          >接单</el-button>
          <el-button 
            v-if="scope.row.status === 2" 
            type="danger" 
            link 
            @click="handleRejection(scope.row)"
          >拒单</el-button>
          <el-button 
            v-if="scope.row.status === 3" 
            type="primary" 
            link 
            @click="handleDelivery(scope.row)"
          >派送</el-button>
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
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getOrderPage, 
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

const rejectDialogVisible = ref(false)
const cancelDialogVisible = ref(false)
const submitLoading = ref(false)

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
    3: '已接单',
    4: '派送中',
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

const handleSearch = () => {
  page.value = 1
  getList()
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
  getList()
}

const handleCurrentChange = (val) => {
  page.value = val
  getList()
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
      getList()
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
          getList()
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

// 派送
const handleDelivery = async (row) => {
  try {
    await ElMessageBox.confirm('确认开始派送该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await deliveryOrder(row.id)
    if (res.code === 1) {
      ElMessage.success('派送成功')
      getList()
    } else {
      ElMessage.error(res.msg || '派送失败')
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
      getList()
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
          getList()
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
  getList()
})
</script>

<style scoped>
/* 页面特有样式（通用样式已在全局 style.css 中定义） */
</style>
