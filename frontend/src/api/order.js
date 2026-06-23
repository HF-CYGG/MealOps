/**
 * 订单管理 API 接口
 */
import request from '@/utils/request'

// 获取订单列表 (分页)
export const getOrderPage = (params) => {
  return request({
    url: '/order/page',
    method: 'get',
    params
  })
}

// 取消订单
export const cancelOrder = (data) => {
  return request({
    url: '/order/cancel',
    method: 'post',
    data
  })
}

// 派送订单
export const deliveryOrder = (id) => {
  return request({
    url: `/order/delivery/${id}`,
    method: 'put'
  })
}

// 完成订单
export const completeOrder = (id) => {
  return request({
    url: `/order/complete/${id}`,
    method: 'put'
  })
}

// 接单
export const confirmOrder = (data) => {
  return request({
    url: '/order/confirm',
    method: 'put',
    data
  })
}

// 拒单
export const rejectionOrder = (data) => {
  return request({
    url: '/order/rejection',
    method: 'put',
    data
  })
}
