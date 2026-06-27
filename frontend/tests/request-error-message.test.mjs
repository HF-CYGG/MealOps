import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const root = resolve(__dirname, '..')

const loadRequestModule = async () => {
  const requestPath = resolve(root, 'src/utils/request.js')
  const authUrl = pathToFileURL(resolve(root, 'src/utils/auth.js')).href
  const source = readFileSync(requestPath, 'utf8')

  const moduleSource = source
    .replace(
      "import axios from 'axios'",
      "const axios = { create: () => ({ interceptors: { request: { use() {} }, response: { use() {} } } }) }"
    )
    .replace(
      "import { ElMessage } from 'element-plus'",
      "const ElMessage = { error() {} }"
    )
    .replace(
      "import { useClientUserStore } from '@/store/clientUser'",
      "const useClientUserStore = () => ({ clearUserInfo() {} })"
    )
    .replace(
      "import { useUserStore } from '@/store/user'",
      "const useUserStore = () => ({ clearUserInfo() {} })"
    )
    .replace(
      "import { isAuthFailureResponse, shouldRedirectAfterClientAuthFailure } from '@/utils/auth'",
      `import { isAuthFailureResponse, shouldRedirectAfterClientAuthFailure } from ${JSON.stringify(authUrl)}`
    )

  return import(`data:text/javascript;charset=utf-8,${encodeURIComponent(moduleSource)}`)
}

const { resolveHttpErrorHandling } = await loadRequestModule()

assert.deepEqual(
  resolveHttpErrorHandling(400, { msg: '具体业务错误' }),
  { kind: 'httpError', message: '具体业务错误' }
)

const unauthorizedResult = resolveHttpErrorHandling(401, { msg: '后端未授权详情' })
assert.equal(unauthorizedResult.kind, 'authFailure')
assert.notEqual(unauthorizedResult.message, '后端未授权详情')

assert.deepEqual(
  resolveHttpErrorHandling(400, { msg: '登录已过期' }),
  { kind: 'authFailure', message: '登录状态已失效，请重新登录' }
)
