<!--
  C端点餐菜单及购物车页
  左侧展示分类，右侧展示菜品列表（随左侧分类联动）
  底部悬浮购物车模块
-->
<template>
  <div class="menu-container fade-slide-enter-active">
    <!-- 顶部信息区 -->
    <div class="menu-header">
      <div class="guest-info">
        <span class="label">就餐人数：</span>
        <span class="value">{{ guestCount || '未选择' }}人</span>
        <el-button link type="primary" @click="$router.push('/client/home')">修改</el-button>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧分类列表 -->
      <div class="category-list">
        <el-scrollbar>
          <div
            v-for="item in categoryList"
            :key="item.key"
            :class="['category-item', activeCategoryKey === item.key ? 'active' : '']"
            @click="handleCategoryClick(item)"
          >
            {{ item.name }}
          </div>
        </el-scrollbar>
      </div>

      <!-- 右侧菜品列表 -->
      <div class="dish-list" ref="dishListRef">
        <el-scrollbar ref="scrollbarRef">
          <el-skeleton :loading="loading" animated :count="6">
            <template #template>
              <div class="dish-grid">
                <div class="dish-card skeleton-card" v-for="i in 6" :key="i">
                  <el-skeleton-item variant="image" style="width: 100%; height: 150px;" />
                  <div style="padding: 14px;">
                    <el-skeleton-item variant="p" style="width: 50%" />
                    <div style="display: flex; align-items: center; justify-content: space-between; margin-top: 16px;">
                      <el-skeleton-item variant="text" style="width: 30%" />
                      <el-skeleton-item variant="circle" style="width: 28px; height: 28px;" />
                    </div>
                  </div>
                </div>
              </div>
            </template>
            <template #default>
              <transition name="fade-slide-list" mode="out-in">
                <div :key="activeCategoryKey || 'empty'" class="dish-grid">
                  <el-card
                    v-for="(dish, index) in dishList"
                    :key="dish.cartKey"
                    class="dish-card"
                    :style="{ animationDelay: `${index * 0.04}s` }"
                    shadow="never"
                    tabindex="0"
                    @click="openDishDetail(dish)"
                    @keydown.enter="openDishDetail(dish)"
                    @keydown.space.prevent="openDishDetail(dish)"
                  >
                    <div class="dish-img-wrap">
                      <img
                        :src="resolveImageUrl(dish.image)"
                        class="dish-img"
                        :alt="dish.name"
                        loading="lazy"
                        @error="applyImageFallback"
                      >
                    </div>
                    <div class="dish-info">
                      <h3 class="dish-name">{{ dish.name }}</h3>
                      <p class="dish-desc">{{ dish.description }}</p>
                      <div class="dish-bottom">
                        <span class="dish-price">￥{{ dish.price }}</span>
                        <div class="dish-actions">
                          <div class="custom-stepper-container">
                            <!-- 减号 -->
                            <transition name="roll-out">
                              <div v-if="getProductCartNum(dish) > 0"
                                   class="stepper-btn minus"
                                   @click.stop="handleSubCart(dish)">
                                <el-icon><Minus /></el-icon>
                              </div>
                            </transition>
                            
                            <!-- 数量 -->
                            <transition name="fade-in">
                              <div v-if="getProductCartNum(dish) > 0" class="stepper-num">
                                {{ getProductCartNum(dish) }}
                              </div>
                            </transition>

                            <!-- 加号 -->
                            <div class="stepper-btn plus" 
                                 :class="{ 'is-big': getProductCartNum(dish) === 0 }"
                                 @click.stop="handleAddCart(dish)">
                              <el-icon><Plus /></el-icon>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </div>
              </transition>
              <el-empty v-if="dishList.length === 0" description="该分类下暂无商品" />
            </template>
          </el-skeleton>
        </el-scrollbar>
      </div>
    </div>

    <!-- 悬浮购物车 -->
    <div class="cart-bar" @click="cartVisible = true">
      <div class="cart-icon" :class="{ 'bounce': isBouncing }">
        <el-badge :value="totalCartNum" :hidden="totalCartNum === 0" class="cart-badge">
          <el-icon :size="30"><ShoppingCart /></el-icon>
        </el-badge>
      </div>
      <div class="cart-price">
        <span>合计: </span>
        <span class="price">￥{{ totalCartPrice.toFixed(2) }}</span>
      </div>
      <el-button type="primary" size="large" class="checkout-btn action-btn" @click.stop="goToCheckout" :disabled="totalCartNum === 0">
        去结算
      </el-button>
    </div>

    <!-- 购物车抽屉 -->
    <el-drawer
      v-model="cartVisible"
      title="购物车"
      direction="btt"
      size="50%"
      class="custom-drawer"
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

    <!-- 商品详情弹卡 -->
    <el-dialog
      v-model="dishDetailVisible"
      width="520px"
      class="dish-detail-dialog"
      :title="dishDetailTitle"
      :show-close="true"
      append-to-body
    >
      <div v-if="selectedDish" class="dish-detail">
        <img
          :src="resolveImageUrl(selectedDish.image)"
          class="dish-detail-img"
          :alt="selectedDish.name"
          @error="applyImageFallback"
        >
        <div class="dish-detail-body">
          <div class="dish-detail-meta">
            <span>{{ currentCategoryName }}</span>
            <span :class="['dish-status', selectedDish.status === 0 ? 'sold-out' : 'available']">
              {{ selectedDish.status === 0 ? '暂停售卖' : '正在售卖' }}
            </span>
          </div>
          <h2>{{ selectedDish.name }}</h2>
          <p>{{ selectedDish.description || '暂无商品介绍' }}</p>
          <div class="dish-detail-footer">
            <span class="dish-detail-price">￥{{ selectedDish.price }}</span>
            <el-button
              type="primary"
              class="detail-add-btn"
              :disabled="selectedDish.status === 0"
              @click="handleAddCartFromDetail"
            >
              <el-icon><Plus /></el-icon>
              加入购物车
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, ShoppingCart, Minus } from '@element-plus/icons-vue'
import { getCategoryList } from '@/api/category'
import { getDishList } from '@/api/dish'
import { getSetmealList } from '@/api/setmeal'
import { useCartStore } from '@/store/cart'
import { useClientUserStore } from '@/store/clientUser'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applyImageFallback, resolveImageUrl } from '@/utils/image'

const CATEGORY_TYPE_DISH = 1
const CATEGORY_TYPE_SETMEAL = 2
const PRODUCT_TYPE_DISH = 'dish'
const PRODUCT_TYPE_SETMEAL = 'setmeal'
const CHECKOUT_PATH = '/client/order/submit'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useClientUserStore()

// 人数信息
const guestCount = computed(() => userStore.guestCount)

// 分类和菜品数据
const categoryList = ref([])
const activeCategoryId = ref(null)
const activeCategoryType = ref(CATEGORY_TYPE_DISH)
const dishList = ref([])
const loading = ref(false)
const scrollbarRef = ref(null)

// 购物车数据
const cartList = ref([])
const cartVisible = ref(false)
const isBouncing = ref(false)
const dishDetailVisible = ref(false)
const selectedDish = ref(null)

const toCartNumber = (value) => {
  const number = Number(value)
  return Number.isFinite(number) ? number : 0
}

const normalizeCategory = (item, type) => ({
  ...item,
  type,
  key: `${type}-${item.id}`
})

const normalizeProduct = (item, itemType) => ({
  ...item,
  itemType,
  categoryType: itemType === PRODUCT_TYPE_SETMEAL ? CATEGORY_TYPE_SETMEAL : CATEGORY_TYPE_DISH,
  cartKey: `${itemType}-${item.id}`,
  price: item.price,
  description: item.description || (itemType === PRODUCT_TYPE_SETMEAL ? '精选套餐组合' : '')
})

const buildCartPayload = (item) => {
  const base = {
    name: item.name,
    amount: item.price,
    image: item.image
  }
  return item.itemType === PRODUCT_TYPE_SETMEAL
    ? { ...base, setmealId: item.id }
    : { ...base, dishId: item.id }
}

// 计算购物车总数和总价
const totalCartNum = computed(() => {
  return cartList.value.reduce((total, item) => total + toCartNumber(item.number), 0)
})
const totalCartPrice = computed(() => {
  return cartList.value.reduce((total, item) => total + (toCartNumber(item.amount) * toCartNumber(item.number)), 0)
})
const activeCategoryKey = computed(() => {
  return activeCategoryId.value ? `${activeCategoryType.value}-${activeCategoryId.value}` : ''
})
const currentCategoryName = computed(() => {
  const categoryId = selectedDish.value?.categoryId || activeCategoryId.value
  const categoryType = selectedDish.value?.categoryType || activeCategoryType.value
  const category = categoryList.value.find(item => item.id === categoryId && item.type === categoryType)
  return category?.name || '推荐商品'
})
const dishDetailTitle = computed(() => {
  return selectedDish.value?.itemType === PRODUCT_TYPE_SETMEAL ? '套餐详情' : '菜品详情'
})

onMounted(async () => {
  await fetchCategoryList()
  await fetchCartList()
})

// 获取分类列表
const fetchCategoryList = async () => {
  try {
    const [dishRes, setmealRes, featuredSetmealRes] = await Promise.all([
      getCategoryList({ type: CATEGORY_TYPE_DISH }),
      getCategoryList({ type: CATEGORY_TYPE_SETMEAL }),
      getSetmealList({ status: 1 })
    ])
    const dishCategories = dishRes.code === 1 ? (dishRes.data || []) : []
    const setmealCategories = setmealRes.code === 1 ? (setmealRes.data || []) : []
    const featuredSetmeals = featuredSetmealRes.code === 1 ? (featuredSetmealRes.data || []) : []
    const fallbackSetmealCategory = setmealCategories.length === 0 && featuredSetmeals.length > 0
      ? [{ id: 'setmeal-all', name: '精选套餐', type: CATEGORY_TYPE_SETMEAL, key: `${CATEGORY_TYPE_SETMEAL}-setmeal-all`, isFallback: true }]
      : []
    categoryList.value = [
      ...dishCategories.map(item => normalizeCategory(item, CATEGORY_TYPE_DISH)),
      ...setmealCategories.map(item => normalizeCategory(item, CATEGORY_TYPE_SETMEAL)),
      ...fallbackSetmealCategory
    ]

    if (categoryList.value.length > 0) {
      const firstCategory = categoryList.value[0]
      activeCategoryId.value = firstCategory.id
      activeCategoryType.value = firstCategory.type
      await fetchDishList()
      return
    }

    dishList.value = []
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

// 点击分类联动
const handleCategoryClick = (category) => {
  activeCategoryId.value = category.id
  activeCategoryType.value = category.type
  fetchDishList()
}

// 获取当前分类下的菜品或套餐列表
const fetchDishList = async () => {
  loading.value = true
  try {
    const isSetmealCategory = activeCategoryType.value === CATEGORY_TYPE_SETMEAL
    const params = { status: 1 }
    if (activeCategoryId.value && activeCategoryId.value !== 'setmeal-all') {
      params.categoryId = activeCategoryId.value
    }
    const res = isSetmealCategory ? await getSetmealList(params) : await getDishList(params)
    if (res.code === 1) {
      dishList.value = (res.data || []).map(item => normalizeProduct(
        item,
        isSetmealCategory ? PRODUCT_TYPE_SETMEAL : PRODUCT_TYPE_DISH
      ))
      nextTick(() => {
        if (scrollbarRef.value) {
          scrollbarRef.value.setScrollTop(0)
        }
      })
    }
  } catch (error) {
    console.error('获取菜品失败', error)
  } finally {
    loading.value = false
  }
}

// 获取购物车列表
const fetchCartList = async () => {
  cartList.value = await cartStore.fetchCartList()
}

// 获取某个商品在购物车中的数量
const getProductCartNum = (product) => {
  const item = cartList.value.find(cartItem => {
    return product.itemType === PRODUCT_TYPE_SETMEAL
      ? cartItem.setmealId === product.id
      : cartItem.dishId === product.id
  })
  return item ? toCartNumber(item.number) : 0
}

// 减少购物车商品
const handleSubCart = async (dish) => {
  try {
    await cartStore.subCart(buildCartPayload(dish))
    fetchCartList()
  } catch (error) {
    console.error('减少购物车商品失败', error)
  }
}

// 加入购物车
const handleAddCart = async (dish, showBounce = true) => {
  try {
    const res = await cartStore.addCart(buildCartPayload(dish))
    if (res.code === 1) {
      ElMessage.success('已加入购物车')
      fetchCartList()
      if (showBounce) {
        triggerBounce()
      }
    }
  } catch (error) {
    console.error('加入购物车失败', error)
  }
}

const openDishDetail = (dish) => {
  selectedDish.value = dish
  dishDetailVisible.value = true
}

const handleAddCartFromDetail = async () => {
  if (!selectedDish.value) return
  await handleAddCart(selectedDish.value)
  dishDetailVisible.value = false
}

const triggerBounce = () => {
  isBouncing.value = true
  setTimeout(() => {
    isBouncing.value = false
  }, 300)
}

// 修改购物车商品数量
const handleCartNumChange = async (val, row) => {
  try {
    if (val < row.number) {
      // 减少
      await cartStore.subCart({ dishId: row.dishId, setmealId: row.setmealId })
    } else {
      // 增加
      await cartStore.addCart({ dishId: row.dishId, setmealId: row.setmealId, name: row.name, amount: row.amount, image: row.image })
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
      const res = await cartStore.cleanCart()
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
  if (!userStore.token) {
    ElMessage.warning('请先登录后再下单')
    router.push({
      path: '/client/login',
      query: { redirect: CHECKOUT_PATH }
    })
    return
  }

  router.push(CHECKOUT_PATH)
}
</script>

<style scoped>
.menu-container {
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px);
  position: relative;
  background-color: transparent;
  animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.menu-header {
  padding: 16px 24px;
  background: transparent;
  margin-bottom: 8px;
}

.guest-info {
  display: flex;
  align-items: center;
  font-size: 15px;
}

.guest-info .label {
  color: var(--text-color-secondary);
}

.guest-info .value {
  color: var(--text-color);
  font-weight: 700;
  margin-right: 16px;
  font-size: 18px;
  letter-spacing: 0.5px;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
  background: transparent;
}

.category-list {
  width: 110px;
  background-color: transparent;
  border-right: none;
  padding: 12px 0;
}

.category-item {
  padding: 14px 16px;
  cursor: pointer;
  text-align: center;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  color: var(--text-color-secondary);
  font-size: 14px;
  font-weight: 500;
  margin: 8px 12px;
  border-radius: 16px;
  background-color: transparent;
}

.category-item:hover {
  color: var(--primary-color);
  background-color: rgba(0, 0, 0, 0.02);
}

.category-item.active {
  background-color: #fff;
  color: var(--primary-color);
  font-weight: 800;
  font-size: 15px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  transform: scale(1.05);
}

.dish-list {
  flex: 1;
  padding: 12px 24px 24px 12px;
  scroll-behavior: smooth;
  min-width: 0;
}

.dish-grid {
  display: grid;
  /* 采用不对称的大卡片布局 */
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 32px 24px;
  padding-bottom: 100px; /* 留出底部购物车空间 */
}

.dish-card {
  border-radius: 24px;
  overflow: visible;
  border: none;
  background: transparent;
  box-shadow: none !important;
  transition: opacity 0.24s ease, box-shadow 0.24s ease;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  outline: none;
  /* 列表项依次浮现的动画 */
  animation: itemFadeInUp 0.5s cubic-bezier(0.16, 1, 0.3, 1) backwards;
}

@keyframes itemFadeInUp {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.dish-card:focus-visible .dish-img-wrap {
  box-shadow: 0 0 0 3px rgba(212, 163, 115, 0.35), 0 16px 32px rgba(0, 0, 0, 0.12);
}

.dish-card:hover .dish-img-wrap {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.1);
}

.dish-card:hover .dish-img {
  transform: scale(1.035);
}

.dish-card :deep(.el-card__body) {
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.dish-img-wrap {
  width: 100%;
  height: 220px;
  overflow: hidden;
  border-radius: 24px;
  background-color: #e5e5ea;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  transition: transform 0.24s ease, box-shadow 0.24s ease;
}

.skeleton-card {
  background: transparent;
}

.dish-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.24s ease;
}

.dish-info {
  padding: 20px 8px 8px;
  position: relative;
  background: transparent;
  z-index: 1;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.dish-name {
  margin: 0 0 6px 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--text-color);
  letter-spacing: 0.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dish-desc {
  font-size: 13px;
  color: var(--text-color-secondary);
  margin: 0 0 16px 0;
  height: 36px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.dish-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: auto;
}

.dish-price {
  color: var(--accent-color);
  font-size: 22px;
  font-weight: 800;
  font-family: 'SF Pro Display', -apple-system, BlinkMacSystemFont, sans-serif;
  letter-spacing: -0.5px;
}

/* 加购按钮与步进器的平滑展开动效 */
.dish-actions {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-width: 90px;
  height: 44px;
}

.stepper-expand-enter-active,
.stepper-expand-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

/* 刚出来时：按钮稍微向右偏移并透明，宽度缩小 */
.stepper-expand-enter-from {
  opacity: 0;
  transform: translateX(10px);
}

/* 离开时：按钮同样偏移并透明 */
.stepper-expand-leave-to {
  opacity: 0;
  transform: translateX(10px);
}

.dish-actions .el-button {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  color: #fff;
  transition: transform 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 8px 16px rgba(28, 28, 30, 0.15);
  width: 44px;
  height: 44px;
  min-width: 44px;
  font-size: 18px;
}

.dish-actions .el-button :deep(.el-icon) {
  color: #fff;
  font-size: 19px;
  font-weight: 700;
}

.dish-actions .el-button:hover {
  transform: scale(1.06);
  background-color: var(--accent-color);
  border-color: var(--accent-color);
  box-shadow: 0 12px 24px rgba(28, 28, 30, 0.25);
}

.dish-actions .el-button:active {
  transform: scale(0.9);
}

/* 自定义丝滑分裂步进器 */
.custom-stepper-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 44px;
  min-width: 90px;
}

.stepper-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #fff;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); /* 带有明显弹性回弹效果的曲线 */
  position: absolute;
  z-index: 2;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stepper-btn:active {
  transform: scale(0.85) !important; /* 覆盖动画中的 transform */
}

.stepper-btn.plus {
  right: 0;
  background-color: var(--accent-color);
  width: 28px;
  height: 28px;
  font-size: 14px;
}

.stepper-btn.plus.is-big {
  background-color: var(--primary-color);
  width: 44px;
  height: 44px;
  font-size: 20px;
}

.stepper-btn.minus {
  right: 60px;
  background-color: var(--primary-color);
  width: 28px;
  height: 28px;
  font-size: 14px;
}

.stepper-num {
  position: absolute;
  right: 28px;
  width: 32px;
  text-align: center;
  font-weight: 800;
  font-size: 16px;
  color: var(--text-color);
  z-index: 1;
}

/* 减号从加号背后滚出的动画 */
.roll-out-enter-active,
.roll-out-leave-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.roll-out-enter-from,
.roll-out-leave-to {
  opacity: 0;
  transform: translateX(60px) rotate(180deg) scale(0.5);
}

/* 数字淡入并滑出的动画 */
.fade-in-enter-active,
.fade-in-leave-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.fade-in-enter-from,
.fade-in-leave-to {
  opacity: 0;
  transform: translateX(20px) scale(0.8);
}

/* 悬浮购物车 - 类似灵动岛风格 */
.cart-bar {
  box-sizing: border-box;
  position: absolute;
  bottom: 32px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 600px;
  height: 72px;
  background: rgba(28, 28, 30, 0.85);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-radius: 36px;
  display: flex;
  align-items: center;
  padding: 0 12px 0 32px;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  z-index: 100;
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.cart-bar:hover {
  transform: translateX(-50%) translateY(-6px) scale(1.02);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.3);
  background: rgba(28, 28, 30, 0.95);
}

.cart-icon {
  margin-right: 24px;
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.cart-icon.bounce {
  animation: elegantBounce 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.cart-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

@keyframes elegantBounce {
  0% { transform: scale(1); }
  40% { transform: scale(1.3) translateY(-4px); }
  100% { transform: scale(1); }
}

.cart-icon :deep(.el-badge__content) {
  background-color: var(--accent-color);
  color: #fff;
  border: none;
  font-weight: 700;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  line-height: 18px;
  font-size: 12px;
  box-shadow: 0 4px 12px rgba(212, 163, 115, 0.4);
}

.cart-price {
  flex: 1;
  font-size: 15px;
  opacity: 0.8;
  display: flex;
  align-items: baseline;
}

.cart-price .price {
  font-size: 24px;
  font-weight: 800;
  margin-left: 8px;
  color: #fff;
  opacity: 1;
  font-family: 'SF Pro Display', -apple-system, sans-serif;
}

.checkout-btn {
  border-radius: 28px;
  padding: 0 32px;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  background: var(--accent-color);
  color: #fff;
  border: none;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 8px 20px rgba(212, 163, 115, 0.3);
}

.checkout-btn:not(:disabled):hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 12px 28px rgba(212, 163, 115, 0.4);
  background: #dfb285;
}

.checkout-btn:not(:disabled):active {
  transform: scale(0.96);
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 16px;
}

.cart-header span {
  font-size: 20px;
  font-weight: 800;
  color: var(--text-color);
}

/* 整个商品网格区域在分类切换时的过渡动画 */
.fade-slide-list-enter-active,
.fade-slide-list-leave-active {
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1), transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-list-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-slide-list-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.dish-detail {
  overflow: hidden;
  border-radius: 24px;
  background: #fff;
}

.dish-detail-img {
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: cover;
  display: block;
  background: #e5e5ea;
}

.dish-detail-body {
  padding: 24px;
}

.dish-detail-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  color: var(--text-color-secondary);
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 10px;
}

.dish-status {
  padding: 4px 10px;
  border-radius: 999px;
  background: #f5f5f7;
}

.dish-status.available {
  color: #1c7c3f;
}

.dish-status.sold-out {
  color: #8e8e93;
}

.dish-detail h2 {
  margin: 0 0 10px;
  font-size: 28px;
  color: var(--text-color);
}

.dish-detail p {
  min-height: 48px;
  margin: 0 0 24px;
  color: var(--text-color-secondary);
  font-size: 15px;
  line-height: 1.7;
}

.dish-detail-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.dish-detail-price {
  color: var(--accent-color);
  font-size: 30px;
  font-weight: 900;
  letter-spacing: -0.8px;
}

.detail-add-btn {
  height: 48px;
  border-radius: 24px;
  padding: 0 24px;
  font-weight: 800;
  background: var(--primary-color);
  border: none;
}

.dish-detail-dialog :deep(.el-dialog) {
  border-radius: 28px;
  overflow: hidden;
  padding: 0;
}

.dish-detail-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.dish-detail-dialog :deep(.el-dialog__header) {
  position: absolute;
  top: 0;
  right: 0;
  z-index: 2;
  width: 64px;
  height: 64px;
  padding: 0;
  margin: 0;
}

.dish-detail-dialog :deep(.el-dialog__headerbtn) {
  z-index: 2;
  width: 42px;
  height: 42px;
  top: 14px;
  right: 14px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(12px);
}

@media (max-width: 1024px) {
  .menu-container {
    height: auto;
    min-height: calc(100vh - 128px);
  }

  .dish-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 28px 20px;
  }

  .dish-img-wrap {
    height: 190px;
  }
}

@media (max-width: 768px) {
  .menu-container {
    min-height: calc(100vh - 128px);
    padding-bottom: 92px;
  }

  .menu-header {
    padding: 8px 4px 12px;
    margin-bottom: 0;
  }

  .guest-info {
    flex-wrap: wrap;
    gap: 8px;
  }

  .main-content {
    flex-direction: column;
    overflow: visible;
  }

  .category-list {
    width: 100%;
    padding: 0 0 12px;
  }

  .category-list :deep(.el-scrollbar__view) {
    display: flex;
    gap: 10px;
    padding: 2px 2px 10px;
  }

  .category-item {
    flex: 0 0 auto;
    min-width: 92px;
    min-height: 52px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0;
    padding: 10px 14px;
    white-space: normal;
  }

  .category-item.active {
    transform: none;
  }

  .dish-list {
    padding: 0;
    overflow: visible;
  }

  .dish-grid {
    grid-template-columns: 1fr;
    gap: 24px;
    padding-bottom: 24px;
  }

  .dish-card {
    border-radius: 22px;
  }

  .dish-img-wrap {
    height: auto;
    aspect-ratio: 16 / 10;
    border-radius: 22px;
  }

  .dish-info {
    padding: 16px 4px 4px;
  }

  .dish-name {
    font-size: 17px;
  }

  .dish-price {
    font-size: 21px;
  }

  .cart-bar {
    position: fixed;
    bottom: max(16px, env(safe-area-inset-bottom));
    left: 12px;
    right: 12px;
    transform: none;
    width: auto;
    height: 64px;
    padding: 0 10px 0 18px;
    border-radius: 32px;
  }

  .cart-bar:hover {
    transform: none;
  }

  .cart-icon {
    margin-right: 14px;
    flex: 0 0 auto;
  }

  .cart-price {
    min-width: 0;
    white-space: nowrap;
  }

  .cart-price .price {
    font-size: 20px;
  }

  .checkout-btn {
    flex: 0 0 auto;
    min-width: 112px;
    height: 44px;
    padding: 0 20px;
  }

  .dish-detail-dialog :deep(.el-dialog) {
    width: calc(100vw - 24px) !important;
    max-width: 520px;
  }

  .dish-detail-body {
    padding: 20px;
  }

  .dish-detail h2 {
    font-size: 24px;
  }

  .dish-detail-footer {
    align-items: stretch;
    flex-direction: column;
    gap: 14px;
  }

  .detail-add-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .menu-container {
    min-height: calc(100vh - 120px);
  }

  .cart-bar {
    gap: 8px;
    padding: 0 8px 0 12px;
  }

  .category-item {
    min-width: 82px;
    font-size: 13px;
  }

  .dish-desc {
    height: auto;
    min-height: 36px;
  }

  .dish-bottom {
    gap: 12px;
  }

  .custom-stepper-container {
    min-width: 80px;
  }

  .stepper-btn.plus.is-big {
    width: 44px;
    height: 44px;
  }

  .cart-price {
    font-size: 13px;
    flex: 1 1 auto;
  }

  .cart-price .price {
    margin-left: 4px;
    font-size: 18px;
  }

  .checkout-btn {
    flex-basis: 82px;
    min-width: 82px;
    padding: 0 8px;
    font-size: 14px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .dish-card,
  .dish-img,
  .dish-img-wrap,
  .list-enter-active,
  .list-leave-active,
  .category-switch-enter-active,
  .category-switch-leave-active {
    transition: none;
  }

  .dish-card:hover .dish-img-wrap,
  .dish-card:hover .dish-img {
    transform: none;
  }
}
</style>
