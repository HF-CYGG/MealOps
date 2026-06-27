import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const root = resolve(__dirname, '..')
const source = readFileSync(resolve(root, 'src/views/order/Index.vue'), 'utf8')

assert.match(source, /prop="payStatus"/, 'admin order table must show payment status')
assert.match(source, /getPayStatusText/, 'admin order table must map payment status text')
assert.match(source, /getPayStatusType/, 'admin order table must map payment status tag type')
assert.match(source, /payStatus === 1/, 'merchant fulfillment actions must be gated by paid orders')
assert.match(source, /备餐中/, 'confirmed orders should be presented as preparing')
assert.match(source, /出餐中/, 'delivery stage should be presented as serving/outbound')
