import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const root = resolve(__dirname, '..')
const source = readFileSync(resolve(root, 'src/views/client/order/Submit.vue'), 'utf8')

assert.match(source, /prepayOrder/, 'checkout must request a prepay record after submitting an order')
assert.match(source, /confirmPayment/, 'checkout must let the user explicitly confirm payment')
assert.match(source, /paymentPanelVisible/, 'checkout must keep a visible payment-confirmation state')
assert.match(source, /currentPayment/, 'checkout must store the current payment record before confirmation')
assert.match(source, /handleConfirmPayment/, 'checkout must expose a separate payment confirmation handler')
assert.match(source, /payment-card/, 'checkout must render a dedicated payment confirmation section')
