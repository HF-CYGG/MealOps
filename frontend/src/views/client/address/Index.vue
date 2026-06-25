<template>
  <div class="address-page fade-slide-enter-active">
    <div class="address-shell">
      <div class="address-header">
        <div class="header-title">
          <el-button link class="back-btn" @click="router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <div>
            <h1>收货地址</h1>
            <p>保存常用地址，结算时可快速切换。</p>
          </div>
        </div>
        <el-button type="primary" class="add-btn" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          新增地址
        </el-button>
      </div>

      <el-skeleton :loading="loading" animated :count="3">
        <template #template>
          <div class="address-grid">
            <div v-for="item in 3" :key="item" class="address-card skeleton-card">
              <el-skeleton-item variant="p" style="width: 45%" />
              <el-skeleton-item variant="text" style="width: 70%; margin-top: 16px;" />
              <el-skeleton-item variant="text" style="width: 90%; margin-top: 12px;" />
            </div>
          </div>
        </template>
        <template #default>
          <div v-if="addressList.length > 0" class="address-grid">
            <div
              v-for="address in addressList"
              :key="address.id"
              :class="['address-card', address.isDefault === 1 ? 'is-default' : '']"
            >
              <div class="card-top">
                <div class="person">
                  <strong>{{ address.consignee }}</strong>
                  <span>{{ address.phone }}</span>
                </div>
                <span v-if="address.isDefault === 1" class="default-badge">默认</span>
              </div>
              <div class="address-line">
                <el-icon><Location /></el-icon>
                <span>{{ formatAddressDetail(address) }}</span>
              </div>
              <div class="card-actions">
                <el-button link type="primary" @click="openEditDialog(address)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button
                  link
                  type="primary"
                  :disabled="address.isDefault === 1"
                  @click="handleSetDefault(address)"
                >
                  <el-icon><Star /></el-icon>
                  设为默认
                </el-button>
                <el-button link type="danger" @click="handleDelete(address)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无收货地址">
            <el-button type="primary" class="empty-action" @click="openAddDialog">
              <el-icon><Plus /></el-icon>
              新增地址
            </el-button>
          </el-empty>
        </template>
      </el-skeleton>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑地址' : '新增地址'"
      width="520px"
      class="address-dialog"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="86px"
        size="large"
        class="address-form"
      >
        <el-form-item label="收货人" prop="consignee">
          <el-input v-model.trim="form.consignee" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model.trim="form.phone"
            placeholder="请输入收货人手机号"
            maxlength="11"
            inputmode="tel"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model.trim="form.detail" placeholder="请输入收货地址" />
        </el-form-item>
        <el-form-item label="地址标签">
          <el-input v-model.trim="form.label" placeholder="如：家、公司、学校" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="makeDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitAddress">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Delete, Edit, Location, Plus, Star } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAddressBook,
  deleteAddressBook,
  getAddressBookList,
  setDefaultAddressBook,
  updateAddressBook
} from '@/api/addressBook'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const makeDefault = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const addressList = ref([])

const PHONE_PATTERN = /^1[3-9]\d{9}$/

const form = reactive({
  consignee: '',
  phone: '',
  detail: '',
  label: ''
})

const rules = {
  consignee: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入收货人手机号', trigger: 'blur' },
    { pattern: PHONE_PATTERN, message: '请输入 11 位有效手机号', trigger: 'blur' }
  ],
  detail: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

onMounted(() => {
  fetchAddressList()
})

const formatAddressDetail = (address) => {
  return [
    address.provinceName,
    address.cityName,
    address.districtName,
    address.detail
  ].filter(Boolean).join('') || address.detail || ''
}

const resetForm = () => {
  editingId.value = null
  makeDefault.value = addressList.value.length === 0
  Object.assign(form, {
    consignee: '',
    phone: '',
    detail: '',
    label: ''
  })
  formRef.value?.clearValidate()
}

const fetchAddressList = async () => {
  loading.value = true
  try {
    const res = await getAddressBookList()
    if (res.code === 1) {
      addressList.value = res.data || []
    }
  } catch (error) {
    console.error('获取收货地址失败', error)
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (address) => {
  editingId.value = address.id
  makeDefault.value = address.isDefault === 1
  Object.assign(form, {
    consignee: address.consignee || '',
    phone: address.phone || '',
    detail: formatAddressDetail(address),
    label: address.label || ''
  })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

const buildPayload = () => ({
  id: editingId.value || undefined,
  consignee: form.consignee.trim(),
  phone: form.phone.trim(),
  detail: form.detail.trim(),
  label: form.label.trim() || '常用地址'
})

const submitAddress = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = buildPayload()
    const res = editingId.value
      ? await updateAddressBook(payload)
      : await createAddressBook(payload)
    const savedId = editingId.value || res.data?.id
    if (makeDefault.value && savedId) {
      await setDefaultAddressBook(savedId)
    }
    ElMessage.success('地址已保存')
    dialogVisible.value = false
    await fetchAddressList()
  } catch (error) {
    console.error('保存收货地址失败', error)
  } finally {
    saving.value = false
  }
}

const handleSetDefault = async (address) => {
  try {
    await setDefaultAddressBook(address.id)
    ElMessage.success('默认地址已更新')
    await fetchAddressList()
  } catch (error) {
    console.error('设置默认地址失败', error)
  }
}

const handleDelete = async (address) => {
  try {
    await ElMessageBox.confirm('确定删除这个收货地址吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAddressBook(address.id)
    ElMessage.success('地址已删除')
    await fetchAddressList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除收货地址失败', error)
    }
  }
}
</script>

<style scoped>
.address-page {
  width: 100%;
}

.address-shell {
  max-width: 980px;
  margin: 0 auto;
  padding: 8px 0 40px;
}

.address-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
  padding: 28px 32px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.04);
}

.header-title {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.back-btn {
  min-height: 44px;
  padding: 0 8px;
  font-weight: 700;
  color: var(--text-color);
}

.header-title h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 900;
  color: var(--text-color);
}

.header-title p {
  margin: 8px 0 0;
  color: var(--text-color-secondary);
}

.add-btn,
.empty-action {
  min-height: 44px;
  border-radius: 22px;
  padding: 0 22px;
  font-weight: 800;
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
}

.address-card {
  min-height: 172px;
  padding: 20px;
  border: 1px solid rgba(28, 28, 30, 0.08);
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.04);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.address-card:hover {
  transform: translateY(-2px);
  border-color: rgba(212, 163, 115, 0.45);
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.08);
}

.address-card.is-default {
  border-color: rgba(212, 163, 115, 0.7);
}

.skeleton-card {
  box-sizing: border-box;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.person {
  display: flex;
  align-items: baseline;
  gap: 10px;
  min-width: 0;
}

.person strong {
  color: var(--text-color);
  font-size: 18px;
}

.person span {
  color: var(--text-color-secondary);
  font-weight: 700;
}

.default-badge {
  flex: 0 0 auto;
  padding: 4px 10px;
  border-radius: 999px;
  color: #9a5b19;
  background: #fff4df;
  font-size: 12px;
  font-weight: 800;
}

.address-line {
  display: flex;
  gap: 8px;
  margin-top: 18px;
  color: var(--text-color-secondary);
  line-height: 1.7;
}

.address-line .el-icon {
  flex: 0 0 auto;
  margin-top: 4px;
}

.card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  margin-top: 18px;
}

.card-actions .el-button {
  min-height: 36px;
  margin-left: 0;
  font-weight: 700;
}

.address-form :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 12px;
  box-shadow: none;
  background: #f7f8fa;
}

.address-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary, #409eff) inset;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.address-dialog :deep(.el-dialog) {
  border-radius: 22px;
}

@media (max-width: 768px) {
  .address-shell {
    padding: 0;
  }

  .address-header {
    flex-direction: column;
    padding: 22px;
    border-radius: 20px;
  }

  .header-title h1 {
    font-size: 24px;
  }

  .add-btn {
    width: 100%;
  }

  .address-grid {
    grid-template-columns: 1fr;
  }

  .address-dialog :deep(.el-dialog) {
    width: calc(100vw - 24px) !important;
  }
}

@media (prefers-reduced-motion: reduce) {
  .address-card {
    transition: none;
  }

  .address-card:hover {
    transform: none;
  }
}
</style>
