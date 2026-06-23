/**
 * C端用户相关API接口
 * 用于处理用户的登录、登出等操作
 */
import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} data 包含手机号等信息
 * @returns {Promise}
 */
export const userLogin = (data) => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns {Promise}
 */
export const userLogout = () => {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}
