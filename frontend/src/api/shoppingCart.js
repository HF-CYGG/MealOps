/**
 * C端购物车相关API接口
 * 用于处理购物车中商品的增删改查
 */
import request from '@/utils/request'

/**
 * 获取购物车列表
 * @returns {Promise}
 */
export const getShoppingCartList = () => {
  return request({
    url: '/shoppingCart/list',
    method: 'get'
  })
}

/**
 * 添加购物车
 * @param {Object} data 包含菜品或套餐ID及数量
 * @returns {Promise}
 */
export const addShoppingCart = (data) => {
  return request({
    url: '/shoppingCart/add',
    method: 'post',
    data
  })
}

/**
 * 减少购物车商品数量
 * @param {Object} data 包含菜品或套餐ID
 * @returns {Promise}
 */
export const subShoppingCart = (data) => {
  return request({
    url: '/shoppingCart/sub',
    method: 'post',
    data
  })
}

/**
 * 清空购物车
 * @returns {Promise}
 */
export const cleanShoppingCart = () => {
  return request({
    url: '/shoppingCart/clean',
    method: 'delete'
  })
}
