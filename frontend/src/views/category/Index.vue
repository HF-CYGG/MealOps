<!-- 
  分类管理页面
  包含分类列表展示、新增、编辑、删除及状态修改功能
-->
<template>
  <div class="page-container">
    <div class="header-action">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="分类名称">
          <el-input v-model="searchForm.name" placeholder="请输入分类名称" clearable />
        </el-form-item>
        <el-form-item label="分类类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 120px">
            <el-option label="菜品分类" :value="1" />
            <el-option label="套餐分类" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="handleAdd">+ 新增分类</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border style="width: 100%">
      <el-table-column prop="name" label="分类名称" align="center" />
      <el-table-column prop="type" label="分类类型" align="center">
        <template #default="scope">
          {{ scope.row.type === 1 ? '菜品分类' : '套餐分类' }}
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" align="center" />
      <el-table-column prop="status" label="状态" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="最后操作时间" align="center" />
      <el-table-column label="操作" align="center" width="250">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button 
            :type="scope.row.status === 1 ? 'danger' : 'success'" 
            link 
            @click="handleStatusChange(scope.row)"
          >
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增分类' : '编辑分类'" 
      v-model="dialogVisible" 
      width="400px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类类型" prop="type">
          <el-radio-group v-model="form.type" :disabled="dialogType === 'edit'">
            <el-radio :label="1">菜品分类</el-radio>
            <el-radio :label="2">套餐分类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="1" :max="999" />
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
  getCategoryPage, 
  addCategory, 
  updateCategory, 
  deleteCategory,
  changeCategoryStatus 
} from '@/api/category'

// 查询参数
const searchForm = reactive({
  name: '',
  type: ''
})

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogType = ref('add')
const submitLoading = ref(false)
const formRef = ref(null)
const form = reactive({
  id: '',
  name: '',
  type: 1,
  sort: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择分类类型', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getCategoryPage({
      page: page.value,
      pageSize: pageSize.value,
      name: searchForm.name || undefined,
      type: searchForm.type || undefined
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
  searchForm.name = ''
  searchForm.type = ''
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

const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(form, {
    id: row.id,
    name: row.name,
    type: row.type,
    sort: row.sort
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let res
        if (dialogType.value === 'add') {
          res = await addCategory(form)
        } else {
          res = await updateCategory(form)
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

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: '',
    name: '',
    type: 1,
    sort: 1
  })
}

const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}该分类吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await changeCategoryStatus(newStatus, row.id)
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

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('此操作将永久删除该分类，是否继续？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteCategory(row.id)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      getList()
    } else {
      ElMessage.error(res.msg || '删除失败')
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
