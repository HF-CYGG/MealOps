/**
 * 菜品管理 API 接口
 */
import request from '@/utils/request'

// 获取菜品列表 (分页)
export const getDishPage = (params) => {
  return request({
    url: '/dish/page',
    method: 'get',
    params
  })
}

// 新增菜品
export const addDish = (data) => {
  return request({
    url: '/dish',
    method: 'post',
    data
  })
}

// 修改菜品
export const updateDish = (data) => {
  return request({
    url: '/dish',
    method: 'put',
    data
  })
}

// 根据 ID 获取菜品详情
export const getDishById = (id) => {
  return request({
    url: `/dish/${id}`,
    method: 'get'
  })
}

// 批量删除菜品
export const deleteDish = (ids) => {
  return request({
    url: '/dish',
    method: 'delete',
    params: { ids }
  })
}

// 根据分类ID获取菜品列表 (用于C端或下拉框)
export const getDishList = (params) => {
  return request({
    url: '/dish/list',
    method: 'get',
    params
  })
}

// 菜品起售停售
export const changeDishStatus = (status, id) => {
  return request({
    url: `/dish/status/${status}`,
    method: 'post',
    params: { id }
  })
}
