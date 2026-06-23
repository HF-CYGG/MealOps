/**
 * C端订单相关API接口
 * 用于处理用户提交订单、查看历史订单等
 */
import request from '@/utils/request'

/**
 * 用户提交订单
 * @param {Object} data 订单信息（地址、金额等）
 * @returns {Promise}
 */
export const submitOrder = (data) => {
  return request({
    url: '/order/submit',
    method: 'post',
    data
  })
}

/**
 * 用户历史订单分页查询
 * @param {Object} params 分页参数
 * @returns {Promise}
 */
export const getHistoryOrders = (params) => {
  return request({
    url: '/order/historyOrders',
    method: 'get',
    params
  })
}

/**
 * 查询订单详情
 * @param {String|Number} id 订单ID
 * @returns {Promise}
 */
export const getOrderDetail = (id) => {
  return request({
    url: `/order/orderDetail/${id}`,
    method: 'get'
  })
}

/**
 * 再来一单
 * @param {String|Number} id 订单ID
 * @returns {Promise}
 */
export const repetitionOrder = (id) => {
  return request({
    url: `/order/repetition/${id}`,
    method: 'post'
  })
}
