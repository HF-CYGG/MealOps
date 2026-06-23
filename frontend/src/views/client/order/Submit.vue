<!--
  C端订单结算页
  展示购物车商品列表、填写收货地址或备注，并提交订单
-->
<template>
  <div class="submit-order-container">
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
        <el-form :model="orderForm" label-width="80px" size="large">
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
        <el-table :data="cartList" style="width: 100%">
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getShoppingCartList } from '@/api/shoppingCart'
import { submitOrder } from '@/api/clientOrder'

const router = useRouter()

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

onMounted(() => {
  fetchCartList()
})

const fetchCartList = async () => {
  try {
    const res = await getShoppingCartList()
    if (res.code === 1) {
      cartList.value = res.data || []
      if (cartList.value.length === 0) {
        ElMessage.warning('购物车为空，请先选购商品')
        router.push('/client/menu')
      }
    }
  } catch (error) {
    console.error('获取购物车失败', error)
  }
}

const handleSubmit = async () => {
  if (!orderForm.consignee || !orderForm.phone || !orderForm.address) {
    ElMessage.warning('请填写完整的收货信息')
    return
  }

  submitting.value = true
  try {
    const data = {
      ...orderForm,
      amount: totalPrice.value,
      payMethod: 1 // 1: 微信支付
    }
    const res = await submitOrder(data)
    if (res.code === 1) {
      ElMessage.success('下单成功')
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
  margin-bottom: 30px;
}

.section h3 {
  margin-bottom: 20px;
  padding-left: 10px;
  border-left: 4px solid #409EFF;
}

.order-total {
  text-align: right;
  margin-top: 20px;
  font-size: 16px;
}

.total-price {
  color: #f56c6c;
  font-size: 22px;
  font-weight: bold;
  margin-left: 10px;
}

.bottom-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.total-info {
  margin-right: 20px;
  font-size: 16px;
}

.total-info .price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}

.submit-btn {
  width: 150px;
}
</style>
