/**
 * C端用户状态管理
 * 存储用户 token 和个人信息
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useClientUserStore = defineStore('clientUser', () => {
  // 从 localStorage 获取初始 token
  const token = ref(localStorage.getItem('clientToken') || '')
  // 用户信息
  const userInfo = ref(JSON.parse(localStorage.getItem('clientUserInfo') || '{}'))
  // 用餐人数上下文
  const guestCount = ref(localStorage.getItem('guestCount') || '')

  // 设置用餐人数
  const setGuestCount = (count) => {
    guestCount.value = count
    localStorage.setItem('guestCount', count)
  }

  // 设置 token
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('clientToken', newToken)
  }

  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('clientUserInfo', JSON.stringify(info))
  }

  // 清除用户信息（退出登录用）
  const clearUserInfo = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('clientToken')
    localStorage.removeItem('clientUserInfo')
  }

  return {
    token,
    userInfo,
    guestCount,
    setToken,
    setUserInfo,
    setGuestCount,
    clearUserInfo
  }
})
