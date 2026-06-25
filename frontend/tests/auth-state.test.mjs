import assert from 'node:assert/strict'
import {
  isAuthFailureResponse,
  shouldRedirectAfterClientAuthFailure
} from '../src/utils/auth.js'

assert.equal(
  isAuthFailureResponse(401, { msg: '未授权，请重新登录' }),
  true
)

assert.equal(
  isAuthFailureResponse(400, { msg: '登录已过期' }),
  true
)

assert.equal(
  isAuthFailureResponse(400, { msg: '未登录' }),
  true
)

assert.equal(
  isAuthFailureResponse(400, { msg: '参数错误' }),
  false
)

assert.equal(
  shouldRedirectAfterClientAuthFailure('/client/menu'),
  false
)

assert.equal(
  shouldRedirectAfterClientAuthFailure('/client/order/history'),
  true
)
