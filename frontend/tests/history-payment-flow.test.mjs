import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import test from 'node:test'

const source = readFileSync(resolve(process.cwd(), 'frontend/src/views/client/order/History.vue'), 'utf8')

test('history order page lets users pay pending orders within retention window', () => {
  assert.match(source, /confirmPayment/, 'history page must confirm pending payments')
  assert.match(source, /prepayOrder/, 'history page must create a payment when missing')
  assert.match(source, /getPaymentByOrder/, 'history page must fetch existing order payment')
  assert.match(source, /remainingPaymentTime/, 'history page must show remaining payment time')
  assert.match(source, /canPayOrder/, 'history page must guard pay button by status and retention window')
})
