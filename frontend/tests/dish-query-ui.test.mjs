import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const root = resolve(__dirname, '..')
const source = readFileSync(resolve(root, 'src/views/dish/Index.vue'), 'utf8')

assert.match(source, /prop="categoryName"/, 'dish table must render the category name returned by backend')
assert.match(source, /categoryId:\s*searchForm\.categoryId/, 'dish query must send categoryId')
assert.match(source, /name:\s*searchForm\.name/, 'dish query must send dish name')
assert.match(source, /status:\s*searchForm\.status/, 'dish query must send sale status')
assert.match(source, /@click="handleSearch"/, 'query button must trigger handleSearch')
