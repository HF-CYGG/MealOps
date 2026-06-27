import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const root = resolve(__dirname, '..')

const loadClientOrderModule = async () => {
  const source = readFileSync(resolve(root, 'src/api/clientOrder.js'), 'utf8')
  const moduleSource = source.replace(
    "import request from '@/utils/request'",
    `const request = (config) => {
      globalThis.__clientOrderRequestConfig = config
      return config
    }`
  )

  return import(`data:text/javascript;charset=utf-8,${encodeURIComponent(moduleSource)}`)
}

delete globalThis.__clientOrderRequestConfig

const { prepayOrder, getPaymentByOrder, confirmPayment } = await loadClientOrderModule()

prepayOrder(9001)
assert.deepEqual(globalThis.__clientOrderRequestConfig, {
  url: '/orders/9001/payments/prepay',
  method: 'post'
})

getPaymentByOrder(9001)
assert.deepEqual(globalThis.__clientOrderRequestConfig, {
  url: '/orders/9001/payment',
  method: 'get'
})

confirmPayment(8001)
assert.deepEqual(globalThis.__clientOrderRequestConfig, {
  url: '/payments/8001/confirm',
  method: 'post'
})
