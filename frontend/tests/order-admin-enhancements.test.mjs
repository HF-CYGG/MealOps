import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import test from 'node:test'

const view = readFileSync(resolve(process.cwd(), 'frontend/src/views/order/Index.vue'), 'utf8')
const api = readFileSync(resolve(process.cwd(), 'frontend/src/api/order.js'), 'utf8')

test('admin order page has summary cards, detail drawer, and auto refresh controls', () => {
  assert.match(api, /getOrderSummary/, 'order API must expose summary endpoint')
  assert.match(api, /getOrderDetail/, 'order API must expose detail endpoint')
  assert.match(view, /summaryCards/, 'order page must render summary cards')
  assert.match(view, /autoRefresh/, 'order page must expose auto refresh control')
  assert.match(view, /detailDrawerVisible/, 'order page must include detail drawer state')
  assert.match(view, /handleDetail/, 'order page must open order detail')
})
