/**
 * axios 请求封装文件
 * 用于统一配置请求基础URL、超时时间、请求和响应拦截器
 * 统一处理网络错误、业务错误提示以及登录失效后的状态清理
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useClientUserStore } from '@/store/clientUser'
import { useUserStore } from '@/store/user'
import { isAuthFailureResponse, shouldRedirectAfterClientAuthFailure } from '@/utils/auth'

// 创建axios实例
const request = axios.create({
  baseURL: '/api', // 基础路径，配合vite的proxy代理
  timeout: 10000 // 请求超时时间10秒
})

// 统一清理 C 端登录状态，确保 Pinia 与 localStorage 同步失效。
const clearClientAuthState = () => {
  const clientUserStore = useClientUserStore()
  clientUserStore.clearUserInfo()
}

// 统一清理 B 端登录状态，避免界面仍然误判为已登录。
const clearAdminAuthState = () => {
  const userStore = useUserStore()
  userStore.clearUserInfo()
}

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 根据当前路径判断是C端还是B端
    const isClient = window.location.pathname.startsWith('/client')
    const tokenKey = isClient ? 'clientToken' : 'token'
    
    // 在发送请求之前做些什么，例如添加Token
    const token = localStorage.getItem(tokenKey)
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    // 对请求错误做些什么
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 2xx 范围内的状态码都会触发该函数。
    // 对响应数据做点什么
    const res = response.data
    
    // 如果返回的 code 不是 1（假设1为成功状态码，可以根据后端实际情况调整）
    if (res.code === 0) {
      ElMessage.error(res.msg || '请求错误')
      return Promise.reject(new Error(res.msg || '请求错误'))
    }

    return res
  },
  (error) => {
    // 超出 2xx 范围的状态码都会触发该函数。
    // 对响应错误做点什么
    let msg = '网络请求错误'
    if (error.response) {
      const currentPath = window.location.pathname
      const isClient = currentPath.startsWith('/client')
      const responseData = error.response.data

      // 后端当前会把部分登录失效场景返回为 400，因此这里需要把鉴权语义单独识别出来。
      if (isAuthFailureResponse(error.response.status, responseData)) {
        msg = '登录状态已失效，请重新登录'

        if (isClient) {
          clearClientAuthState()

          if (shouldRedirectAfterClientAuthFailure(currentPath) && currentPath !== '/client/login') {
            window.location.href = `/client/login?redirect=${encodeURIComponent(currentPath)}`
          }
        } else {
          clearAdminAuthState()

          if (currentPath !== '/login') {
            window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`
          }
        }

        ElMessage.error(msg)
        return Promise.reject(error)
      }

      switch (error.response.status) {
        case 401:
          msg = '未授权，请重新登录'
          // 这里保留 401 的兜底处理，兼容后端后续改成更标准的未授权状态码。
          if (isClient) {
            clearClientAuthState()
            if (currentPath !== '/client/login') {
              window.location.href = `/client/login?redirect=${encodeURIComponent(currentPath)}`
            }
          } else {
            clearAdminAuthState()
            if (currentPath !== '/login') {
              window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`
            }
          }
          break
        case 403:
          msg = '拒绝访问'
          break
        case 404:
          msg = '请求地址错误'
          break
        case 500:
          msg = '服务器内部错误'
          break
        default:
          msg = `请求错误 (${error.response.status})`
      }
    } else if (error.message.includes('timeout')) {
      msg = '请求超时'
    } else if (error.message.includes('Network Error')) {
      msg = '网络连接异常'
    }
    
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
