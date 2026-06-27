<!-- 
  员工管理页面
  包含员工列表展示、新增、编辑及状态切换功能
-->
<template>
  <div class="page-container">
    <!-- 顶部操作栏 -->
    <div class="header-action">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.name" placeholder="请输入员工姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="handleAdd">+ 新增员工</el-button>
    </div>

    <!-- 表格区域 -->
    <el-table :data="tableData" v-loading="loading" border style="width: 100%">
      <el-table-column prop="name" label="员工姓名" align="center" />
      <el-table-column prop="username" label="账号" align="center" />
      <el-table-column prop="phone" label="手机号" align="center" />
      <el-table-column prop="status" label="账号状态" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="最后操作时间" align="center" />
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button 
            :type="scope.row.status === 1 ? 'danger' : 'success'" 
            link 
            @click="handleStatusChange(scope.row)"
          >
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增员工' : '编辑员工'" 
      v-model="dialogVisible" 
      width="500px"
      @close="resetForm"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
      >
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入登录密码" show-password />
        </el-form-item>
        <el-form-item label="员工姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入员工姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="form.sex">
            <el-radio label="1">男</el-radio>
            <el-radio label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="form.idNumber" placeholder="请输入身份证号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getEmployeeList, 
  addEmployee, 
  updateEmployee, 
  changeEmployeeStatus 
} from '@/api/employee'

// 查询参数
const searchForm = reactive({
  name: ''
})

// 表格数据与分页
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 弹窗相关
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' or 'edit'
const submitLoading = ref(false)
const formRef = ref(null)
const form = reactive({
  id: '',
  username: '',
  password: '',
  name: '',
  phone: '',
  sex: '1',
  idNumber: ''
})

// 验证规则
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  idNumber: [
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ]
}

// 获取员工列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getEmployeeList({
      page: page.value,
      pageSize: pageSize.value,
      name: searchForm.name || undefined
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

// 搜索和重置
const handleSearch = () => {
  page.value = 1
  getList()
}
const resetSearch = () => {
  searchForm.name = ''
  handleSearch()
}

// 分页改变
const handleSizeChange = (val) => {
  pageSize.value = val
  getList()
}
const handleCurrentChange = (val) => {
  page.value = val
  getList()
}

// 新增员工
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
}

// 编辑员工
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '',
    name: row.name,
    phone: row.phone,
    sex: row.sex,
    idNumber: row.idNumber
  })
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const payload = {
          ...form,
          phone: form.phone || undefined,
          idNumber: form.idNumber || undefined
        }
        if (dialogType.value === 'edit') {
          delete payload.password
        }
        let res
        if (dialogType.value === 'add') {
          res = await addEmployee(payload)
        } else {
          res = await updateEmployee(payload)
        }
        if (res.code === 1) {
          ElMessage.success(dialogType.value === 'add' ? '新增成功' : '编辑成功')
          dialogVisible.value = false
          getList()
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: '',
    username: '',
    password: '',
    name: '',
    phone: '',
    sex: '1',
    idNumber: ''
  })
}

// 状态修改
const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}该员工账号吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await changeEmployeeStatus(newStatus, row.id)
    if (res.code === 1) {
      ElMessage.success(`${actionText}成功`)
      getList()
    } else {
      ElMessage.error(res.msg || `${actionText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
/* 页面特有样式（通用样式已在全局 style.css 中定义） */
</style>
