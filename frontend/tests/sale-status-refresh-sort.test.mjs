import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import test from 'node:test'

const dish = readFileSync(resolve(process.cwd(), 'frontend/src/views/dish/Index.vue'), 'utf8')
const setmeal = readFileSync(resolve(process.cwd(), 'frontend/src/views/setmeal/Index.vue'), 'utf8')

test('dish and setmeal status changes refresh their lists after success', () => {
  assert.match(dish, /await getList\(\)/, 'dish status success must await list refresh')
  assert.match(setmeal, /await getList\(\)/, 'setmeal status success must await list refresh')
})
