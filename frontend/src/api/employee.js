/**
 * 员工管理 API 接口
 */
import request from '@/utils/request'

// 员工登录
export const login = (data) => {
  return request({
    url: '/employee/login',
    method: 'post',
    data
  })
}

// 员工退出登录
export const logout = () => {
  return request({
    url: '/employee/logout',
    method: 'post'
  })
}

// 获取员工列表 (分页)
export const getEmployeeList = (params) => {
  return request({
    url: '/employee/page',
    method: 'get',
    params
  })
}

// 新增员工
export const addEmployee = (data) => {
  return request({
    url: '/employee',
    method: 'post',
    data
  })
}

// 编辑员工
export const updateEmployee = (data) => {
  return request({
    url: '/employee',
    method: 'put',
    data
  })
}

// 获取单个员工信息
export const getEmployeeById = (id) => {
  return request({
    url: `/employee/${id}`,
    method: 'get'
  })
}

// 修改员工状态
export const changeEmployeeStatus = (status, id) => {
  return request({
    url: `/employee/status/${status}`,
    method: 'post',
    params: { id }
  })
}
