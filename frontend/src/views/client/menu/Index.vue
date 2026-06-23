<!--
  C端点餐菜单及购物车页
  左侧展示分类，右侧展示菜品列表（随左侧分类联动）
  底部悬浮购物车模块
-->
<template>
  <div class="menu-container">
    <div class="main-content">
      <!-- 左侧分类列表 -->
      <div class="category-list">
        <el-scrollbar>
          <div
            v-for="item in categoryList"
            :key="item.id"
            :class="['category-item', activeCategoryId === item.id ? 'active' : '']"
            @click="handleCategoryClick(item.id)"
          >
            {{ item.name }}
          </div>
        </el-scrollbar>
      </div>

      <!-- 右侧菜品列表 -->
      <div class="dish-list">
        <el-scrollbar>
          <div class="dish-grid">
            <el-card v-for="dish in dishList" :key="dish.id" class="dish-card" shadow="hover">
              <img :src="dish.image" class="dish-img" alt="dish">
              <div class="dish-info">
                <h3 class="dish-name">{{ dish.name }}</h3>
                <p class="dish-desc">{{ dish.description }}</p>
                <div class="dish-bottom">
                  <span class="dish-price">￥{{ dish.price }}</span>
                  <div class="dish-actions">
                    <el-button type="primary" size="small" circle icon="Plus" @click="handleAddCart(dish)"></el-button>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
          <el-empty v-if="dishList.length === 0" description="该分类下暂无菜品" />
        </el-scrollbar>
      </div>
    </div>

    <!-- 悬浮购物车 -->
    <div class="cart-bar" @click="cartVisible = true">
      <div class="cart-icon">
        <el-badge :value="totalCartNum" class="item">
          <el-icon :size="30"><ShoppingCart /></el-icon>
        </el-badge>
      </div>
      <div class="cart-price">
        <span>合计: </span>
        <span class="price">￥{{ totalCartPrice.toFixed(2) }}</span>
      </div>
      <el-button type="warning" size="large" class="checkout-btn" @click.stop="goToCheckout" :disabled="totalCartNum === 0">
        去结算
      </el-button>
    </div>

    <!-- 购物车抽屉/弹窗 -->
    <el-drawer
      v-model="cartVisible"
      title="购物车"
      direction="btt"
      size="50%"
    >
      <div class="cart-list">
        <div class="cart-header">
          <span>已选商品</span>
          <el-button link type="danger" @click="handleCleanCart">清空购物车</el-button>
        </div>
        <el-table :data="cartList" style="width: 100%">
          <el-table-column prop="name" label="名称" />
          <el-table-column label="价格" width="100">
            <template #default="scope">
              ￥{{ scope.row.amount }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.number"
                :min="0"
                size="small"
                @change="(val) => handleCartNumChange(val, scope.row)"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, ShoppingCart } from '@element-plus/icons-vue'
import { getCategoryList } from '@/api/category'
import { getDishList } from '@/api/dish'
import { getShoppingCartList, addShoppingCart, subShoppingCart, cleanShoppingCart } from '@/api/shoppingCart'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// 分类和菜品数据
const categoryList = ref([])
const activeCategoryId = ref(null)
const dishList = ref([])

// 购物车数据
const cartList = ref([])
const cartVisible = ref(false)

// 计算购物车总数和总价
const totalCartNum = computed(() => {
  return cartList.value.reduce((total, item) => total + item.number, 0)
})
const totalCartPrice = computed(() => {
  return cartList.value.reduce((total, item) => total + (item.amount * item.number), 0)
})

onMounted(async () => {
  await fetchCategoryList()
  await fetchCartList()
})

// 获取分类列表
const fetchCategoryList = async () => {
  try {
    const res = await getCategoryList({ type: 1 }) // 1表示菜品分类
    if (res.code === 1) {
      categoryList.value = res.data || []
      if (categoryList.value.length > 0) {
        activeCategoryId.value = categoryList.value[0].id
        fetchDishList()
      }
    }
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

// 点击分类联动
const handleCategoryClick = (id) => {
  activeCategoryId.value = id
  fetchDishList()
}

// 获取菜品列表
const fetchDishList = async () => {
  try {
    const res = await getDishList({ categoryId: activeCategoryId.value })
    if (res.code === 1) {
      dishList.value = res.data || []
    }
  } catch (error) {
    console.error('获取菜品失败', error)
  }
}

// 获取购物车列表
const fetchCartList = async () => {
  try {
    const res = await getShoppingCartList()
    if (res.code === 1) {
      cartList.value = res.data || []
    }
  } catch (error) {
    console.error('获取购物车失败', error)
  }
}

// 加入购物车
const handleAddCart = async (dish) => {
  try {
    const res = await addShoppingCart({
      dishId: dish.id,
      name: dish.name,
      amount: dish.price,
      image: dish.image
    })
    if (res.code === 1) {
      ElMessage.success('已加入购物车')
      fetchCartList()
    }
  } catch (error) {
    console.error('加入购物车失败', error)
  }
}

// 修改购物车商品数量
const handleCartNumChange = async (val, row) => {
  try {
    if (val < row.number) {
      // 减少
      await subShoppingCart({ dishId: row.dishId, setmealId: row.setmealId })
    } else {
      // 增加
      await addShoppingCart({ dishId: row.dishId, setmealId: row.setmealId })
    }
    fetchCartList()
  } catch (error) {
    console.error('修改购物车数量失败', error)
  }
}

// 清空购物车
const handleCleanCart = () => {
  ElMessageBox.confirm('确认清空购物车吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await cleanShoppingCart()
      if (res.code === 1) {
        ElMessage.success('购物车已清空')
        cartVisible.value = false
        fetchCartList()
      }
    } catch (error) {
      console.error(error)
    }
  })
}

// 去结算
const goToCheckout = () => {
  router.push('/client/order/submit')
}
</script>

<style scoped>
.menu-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px); /* 减去 header 和 padding */
  position: relative;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.category-list {
  width: 150px;
  background-color: #f8f8f8;
  border-right: 1px solid #ebeef5;
}

.category-item {
  padding: 15px 20px;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s;
}

.category-item:hover {
  background-color: #e6f1fc;
}

.category-item.active {
  background-color: #fff;
  color: #409EFF;
  font-weight: bold;
  border-left: 4px solid #409EFF;
}

.dish-list {
  flex: 1;
  padding: 20px;
}

.dish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.dish-card {
  border-radius: 8px;
  overflow: hidden;
}

.dish-img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  background-color: #f0f0f0;
}

.dish-info {
  padding: 10px;
}

.dish-name {
  margin: 0 0 5px 0;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dish-desc {
  font-size: 12px;
  color: #999;
  margin: 0 0 10px 0;
  height: 32px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.dish-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

/* 悬浮购物车 */
.cart-bar {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  height: 60px;
  background: #333;
  border-radius: 30px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  z-index: 100;
}

.cart-icon {
  margin-right: 20px;
}

.cart-icon :deep(.el-badge__content) {
  background-color: #f56c6c;
}

.cart-price {
  flex: 1;
  font-size: 18px;
}

.cart-price .price {
  font-size: 24px;
  font-weight: bold;
}

.checkout-btn {
  border-radius: 20px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 0 20px;
}
</style>
