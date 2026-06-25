/**
 * 本文件用于统一管理 C 端购物车状态。
 * 已登录用户优先使用服务端购物车，游客则使用本地购物车；
 * 当检测到失效 token 导致的鉴权失败时，会自动回退到本地购物车，避免界面持续报错。
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getShoppingCartList, addShoppingCart, subShoppingCart, cleanShoppingCart } from '@/api/shoppingCart'
import { useClientUserStore } from './clientUser'
import { isAuthFailureResponse } from '@/utils/auth'

export const useCartStore = defineStore('cart', () => {
  const localCart = ref(JSON.parse(localStorage.getItem('localCart') || '[]'))
  const userStore = useClientUserStore()

  // 将游客购物车持久化到本地，保证刷新页面后仍能恢复。
  const saveLocalCart = () => {
    localStorage.setItem('localCart', JSON.stringify(localCart.value))
  }

  // 统一判断当前错误是否属于登录失效，便于服务端购物车自动降级到本地购物车。
  const isAuthFailureError = (error) => {
    return isAuthFailureResponse(error?.response?.status, error?.response?.data)
  }

  // 游客加入购物车时直接在本地累加数量，避免依赖后端登录态。
  const addLocalCart = (dish) => {
    const existing = localCart.value.find(item => item.dishId === dish.dishId && item.setmealId === dish.setmealId)
    if (existing) {
      existing.number += 1
    } else {
      localCart.value.push({ ...dish, number: 1 })
    }
    saveLocalCart()
    return { code: 1, msg: 'success' }
  }

  // 游客减少购物车时只操作本地缓存，数量归零后直接移除。
  const subLocalCart = (data) => {
    const index = localCart.value.findIndex(item => item.dishId === data.dishId && item.setmealId === data.setmealId)
    if (index !== -1) {
      localCart.value[index].number -= 1
      if (localCart.value[index].number <= 0) {
        localCart.value.splice(index, 1)
      }
      saveLocalCart()
    }
    return { code: 1, msg: 'success' }
  }

  // 游客清空购物车时直接清空本地缓存。
  const cleanLocalCart = () => {
    localCart.value = []
    saveLocalCart()
    return { code: 1, msg: 'success' }
  }

  // 获取购物车列表
  const fetchCartList = async () => {
    if (userStore.token) {
      try {
        const res = await getShoppingCartList()
        if (res.code === 1) {
          return res.data || []
        }
      } catch (error) {
        // 如果只是登录态失效，则直接回退到游客购物车，避免菜单页反复报错。
        if (isAuthFailureError(error)) {
          return localCart.value
        }
        console.error('获取购物车失败', error)
      }
      return []
    } else {
      return localCart.value
    }
  }

  // 加入购物车
  const addCart = async (dish) => {
    if (userStore.token) {
      try {
        return await addShoppingCart(dish)
      } catch (error) {
        if (isAuthFailureError(error)) {
          return addLocalCart(dish)
        }
        throw error
      }
    } else {
      return addLocalCart(dish)
    }
  }

  // 减少购物车
  const subCart = async (data) => {
    if (userStore.token) {
      try {
        return await subShoppingCart(data)
      } catch (error) {
        if (isAuthFailureError(error)) {
          return subLocalCart(data)
        }
        throw error
      }
    } else {
      return subLocalCart(data)
    }
  }

  // 清空购物车
  const cleanCart = async () => {
    if (userStore.token) {
      try {
        return await cleanShoppingCart()
      } catch (error) {
        if (isAuthFailureError(error)) {
          return cleanLocalCart()
        }
        throw error
      }
    } else {
      return cleanLocalCart()
    }
  }

  // 登录后同步本地购物车到服务端
  const syncCartToServer = async () => {
    if (localCart.value.length > 0 && userStore.token) {
      for (const item of localCart.value) {
        // 由于服务端接口可能只支持每次加1，需要循环添加
        for (let i = 0; i < item.number; i++) {
          await addShoppingCart({
            dishId: item.dishId,
            setmealId: item.setmealId,
            name: item.name,
            amount: item.amount,
            image: item.image
          })
        }
      }
      localCart.value = []
      saveLocalCart()
    }
  }

  return {
    localCart,
    fetchCartList,
    addCart,
    subCart,
    cleanCart,
    syncCartToServer
  }
})
