/**
 * 管理端统计 API
 */
import request from '@/utils/request'

export const getDashboardOverview = () => {
  return request({
    url: '/stats/overview',
    method: 'get'
  })
}
