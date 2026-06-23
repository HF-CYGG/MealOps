import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getShoppingCartList, addShoppingCart, subShoppingCart, cleanShoppingCart } from '@/api/shoppingCart'
import { useClientUserStore } from './clientUser'

export const useCartStore = defineStore('cart', () => {
  const localCart = ref(JSON.parse(localStorage.getItem('localCart') || '[]'))
  const userStore = useClientUserStore()

  const saveLocalCart = () => {
    localStorage.setItem('localCart', JSON.stringify(localCart.value))
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
      return await addShoppingCart(dish)
    } else {
      const existing = localCart.value.find(item => item.dishId === dish.dishId && item.setmealId === dish.setmealId)
      if (existing) {
        existing.number += 1
      } else {
        localCart.value.push({ ...dish, number: 1 })
      }
      saveLocalCart()
      return { code: 1, msg: 'success' }
    }
  }

  // 减少购物车
  const subCart = async (data) => {
    if (userStore.token) {
      return await subShoppingCart(data)
    } else {
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
  }

  // 清空购物车
  const cleanCart = async () => {
    if (userStore.token) {
      return await cleanShoppingCart()
    } else {
      localCart.value = []
      saveLocalCart()
      return { code: 1, msg: 'success' }
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
