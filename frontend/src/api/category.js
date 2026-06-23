/**
 * 分类管理 API 接口
 */
import request from '@/utils/request'

// 获取分类列表 (分页)
export const getCategoryPage = (params) => {
  return request({
    url: '/category/page',
    method: 'get',
    params
  })
}

// 获取分类列表 (不分页，用于下拉框等)
export const getCategoryList = (params) => {
  return request({
    url: '/category/list',
    method: 'get',
    params
  })
}

// 新增分类
export const addCategory = (data) => {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}

// 编辑分类
export const updateCategory = (data) => {
  return request({
    url: '/category',
    method: 'put',
    data
  })
}

// 删除分类
export const deleteCategory = (id) => {
  return request({
    url: '/category',
    method: 'delete',
    params: { id }
  })
}

// 启用禁用分类
export const changeCategoryStatus = (status, id) => {
  return request({
    url: `/category/status/${status}`,
    method: 'post',
    params: { id }
  })
}
