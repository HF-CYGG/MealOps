/**
 * 本文件用于统一处理前端鉴权失败识别与后续跳转策略。
 * 这样可以让请求层、购物车等多个模块共用同一套“登录失效”判断逻辑，
 * 避免后端返回 400 时各处重复判断并出现行为不一致。
 */

const AUTH_MESSAGE_KEYWORDS = ['未登录', '登录已过期', 'token', 'jwt', 'bearer', '未授权']

/**
 * 判断当前响应是否属于登录失效、未授权或 token 无效的鉴权失败。
 * @param {number | undefined} status HTTP 状态码
 * @param {{ msg?: string } | undefined} data 响应体
 * @returns {boolean}
 */
export const isAuthFailureResponse = (status, data) => {
  if (status === 401) {
    return true
  }

  if (status !== 400) {
    return false
  }

  const message = String(data?.msg || '').toLowerCase()
  return AUTH_MESSAGE_KEYWORDS.some((keyword) => message.includes(keyword.toLowerCase()))
}

/**
 * 判断 C 端当前页面在登录失效后是否应该立即跳转登录页。
 * 菜单页允许游客继续浏览，因此只清理失效状态，不强制跳转。
 * @param {string} pathname 当前路径
 * @returns {boolean}
 */
export const shouldRedirectAfterClientAuthFailure = (pathname = '') => {
  return pathname.startsWith('/client/order')
}
