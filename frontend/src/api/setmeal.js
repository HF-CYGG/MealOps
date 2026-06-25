/**
 * 套餐管理 API 接口
 */
import request from '@/utils/request'

// 获取套餐列表 (分页)
export const getSetmealPage = (params) => {
  return request({
    url: '/setmeal/page',
    method: 'get',
    params
  })
}

export const getSetmealList = (params) => {
  return request({
    url: '/setmeal/list',
    method: 'get',
    params
  })
}

// 新增套餐
export const addSetmeal = (data) => {
  return request({
    url: '/setmeal',
    method: 'post',
    data
  })
}

// 修改套餐
export const updateSetmeal = (data) => {
  return request({
    url: '/setmeal',
    method: 'put',
    data
  })
}

// 根据 ID 获取套餐详情
export const getSetmealById = (id) => {
  return request({
    url: `/setmeal/${id}`,
    method: 'get'
  })
}

// 批量删除套餐
export const deleteSetmeal = (ids) => {
  return request({
    url: '/setmeal',
    method: 'delete',
    params: { ids }
  })
}

// 套餐起售停售
export const changeSetmealStatus = (status, id) => {
  return request({
    url: `/setmeal/status/${status}`,
    method: 'post',
    params: { id }
  })
}
