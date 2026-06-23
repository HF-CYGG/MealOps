/**
 * 用户状态管理
 * 存储用户信息和 Token
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 从本地存储初始化 Token
  const token = ref(localStorage.getItem('token') || '')
  // 存储用户信息
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  /**
   * 设置 Token
   * @param {string} newToken 
   */
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /**
   * 设置用户信息
   * @param {Object} info 
   */
  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  /**
   * 清除用户信息（退出登录）
   */
  const clearUserInfo = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    clearUserInfo
  }
})
