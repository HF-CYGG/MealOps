<!-- 
  菜品管理页面
  包含菜品列表展示、新增、编辑、删除及起售/停售状态切换功能
-->
<template>
  <div class="page-container">
    <div class="header-action">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="菜品名称">
          <el-input v-model="searchForm.name" placeholder="请输入菜品名称" clearable />
        </el-form-item>
        <el-form-item label="菜品分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择" clearable>
            <el-option 
              v-for="item in categoryList" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="售卖状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="起售" :value="1" />
            <el-option label="停售" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="action-buttons">
        <el-button type="primary" @click="handleAdd">+ 新增菜品</el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>
    </div>

    <el-table 
      :data="tableData" 
      v-loading="loading" 
      border 
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="name" label="菜品名称" align="center" />
      <el-table-column prop="image" label="图片" align="center">
        <template #default="scope">
          <el-image 
            class="dish-thumb"
            :src="resolveImageUrl(scope.row.image)" 
            fit="cover"
            :preview-src-list="[resolveImageUrl(scope.row.image)]"
            preview-teleported
          >
            <template #error>
              <img class="dish-thumb-fallback" :src="FALLBACK_DISH_IMAGE" alt="菜品图片占位" />
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="菜品分类" align="center" />
      <el-table-column prop="price" label="售价(元)" align="center">
        <template #default="scope">
          ￥{{ scope.row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="售卖状态" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '起售' : '停售' }}
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
            {{ scope.row.status === 1 ? '停售' : '起售' }}
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

    <!-- 简化的新增/编辑对话框（实际项目中可能需要更复杂的表单，如口味配置） -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增菜品' : '编辑菜品'" 
      v-model="dialogVisible" 
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="菜品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择菜品分类">
            <el-option 
              v-for="item in categoryList" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="菜品价格" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" />
          <span style="margin-left: 10px;">元</span>
        </el-form-item>
        <el-form-item label="菜品图片" prop="image">
          <div class="image-editor">
            <el-upload
              class="image-uploader"
              action="#"
              :show-file-list="false"
              :http-request="handleImageUpload"
              :before-upload="beforeImageUpload"
              accept="image/*"
            >
              <div class="image-preview">
                <img :src="resolveImageUrl(form.image)" alt="菜品图片预览" @error="applyImageFallback" />
                <div class="image-preview-mask">
                  {{ uploadLoading ? '上传中...' : '点击上传' }}
                </div>
              </div>
            </el-upload>
            <el-input v-model="form.image" placeholder="上传后自动填入，也可手动编辑图片 URL" clearable />
          </div>
        </el-form-item>
        <el-form-item label="菜品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入菜品描述" />
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
  getDishPage, 
  addDish, 
  updateDish, 
  deleteDish,
  changeDishStatus,
  getDishById
} from '@/api/dish'
import { getCategoryList } from '@/api/category'
import { uploadFile } from '@/api/common'
import { applyImageFallback, FALLBACK_DISH_IMAGE, resolveImageUrl } from '@/utils/image'

const searchForm = reactive({
  name: '',
  categoryId: '',
  status: ''
})

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref([])
const categoryList = ref([])

const dialogVisible = ref(false)
const dialogType = ref('add')
const submitLoading = ref(false)
const uploadLoading = ref(false)
const formRef = ref(null)
const form = reactive({
  id: '',
  name: '',
  categoryId: '',
  price: 0,
  image: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择菜品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入菜品价格', trigger: 'blur' }],
  image: [{ required: true, message: '请上传或填写菜品图片', trigger: 'blur' }]
}

const beforeImageUpload = (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return false
  }
  return true
}

const handleImageUpload = async ({ file, onSuccess, onError }) => {
  uploadLoading.value = true
  try {
    const res = await uploadFile(file)
    if (res.code === 1 && res.data?.url) {
      form.image = res.data.url
      ElMessage.success('图片上传成功')
      onSuccess?.(res)
    }
  } catch (error) {
    console.error('图片上传失败', error)
    onError?.(error)
  } finally {
    uploadLoading.value = false
  }
}

// 获取分类下拉列表
const fetchCategoryList = async () => {
  try {
    const res = await getCategoryList({ type: 1 }) // 1 表示菜品分类
    if (res.code === 1) {
      categoryList.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getDishPage({
      page: page.value,
      pageSize: pageSize.value,
      name: searchForm.name || undefined,
      categoryId: searchForm.categoryId || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
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
  searchForm.categoryId = ''
  searchForm.status = ''
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

const handleSelectionChange = (val) => {
  selectedIds.value = val.map(item => item.id)
}

const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogType.value = 'edit'
  try {
    const res = await getDishById(row.id)
    if (res.code === 1) {
      Object.assign(form, {
        id: res.data.id,
        name: res.data.name,
        categoryId: res.data.categoryId,
        price: res.data.price,
        image: res.data.image,
        description: res.data.description,
        status: res.data.status
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let res
        if (dialogType.value === 'add') {
          res = await addDish(form)
        } else {
          res = await updateDish(form)
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
    categoryId: '',
    price: 0,
    image: '',
    description: '',
    status: 1
  })
}

const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '起售' : '停售'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}该菜品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await changeDishStatus(newStatus, row.id)
    if (res.code === 1) {
      ElMessage.success(`${actionText}成功`)
      await getList()
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
    await ElMessageBox.confirm('此操作将永久删除该菜品，是否继续？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteDish(row.id)
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

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm('此操作将永久删除选中的菜品，是否继续？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ids = selectedIds.value.join(',')
    const res = await deleteDish(ids)
    if (res.code === 1) {
      ElMessage.success('批量删除成功')
      getList()
    } else {
      ElMessage.error(res.msg || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  fetchCategoryList()
  getList()
})
</script>

<style scoped>
.dish-thumb,
.dish-thumb-fallback {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  object-fit: cover;
  display: block;
}

.image-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.image-uploader {
  width: 180px;
}

.image-preview {
  width: 180px;
  height: 118px;
  position: relative;
  overflow: hidden;
  border-radius: 14px;
  background: #f5f5f7;
  cursor: pointer;
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.05);
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-preview-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  background: rgba(28, 28, 30, 0.46);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.image-preview:hover .image-preview-mask {
  opacity: 1;
}

@media (max-width: 768px) {
  .header-action {
    flex-direction: column;
    gap: 16px;
  }

  .header-action :deep(.el-form) {
    width: 100%;
  }

  .header-action :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }

  .header-action :deep(.el-form-item__content),
  .header-action :deep(.el-input),
  .header-action :deep(.el-select) {
    width: 100%;
  }

  .action-buttons {
    width: 100%;
    flex-wrap: wrap;
  }

  .action-buttons .el-button {
    flex: 1 1 140px;
    min-height: 44px;
  }

  :deep(.el-table) {
    min-width: 820px;
  }

  :deep(.el-table__inner-wrapper) {
    overflow-x: auto;
  }

  .image-uploader,
  .image-preview {
    width: 100%;
    max-width: 260px;
  }

  .image-preview {
    height: auto;
    aspect-ratio: 16 / 10;
  }
}
</style>
