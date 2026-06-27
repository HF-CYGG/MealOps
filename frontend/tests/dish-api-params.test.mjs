import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const root = resolve(__dirname, '..')

const loadDishModule = async () => {
  const source = readFileSync(resolve(root, 'src/api/dish.js'), 'utf8')
  const moduleSource = source.replace(
    "import request from '@/utils/request'",
    `const request = (config) => {
      globalThis.__dishRequestConfig = config
      return config
    }`
  )

  return import(`data:text/javascript;charset=utf-8,${encodeURIComponent(moduleSource)}`)
}

delete globalThis.__dishRequestConfig

const { changeDishStatus } = await loadDishModule()

changeDishStatus(0, 3001)

const config = globalThis.__dishRequestConfig

assert.equal(config.url, '/dish/status/0')
assert.equal(config.method, 'post')
assert.equal(config.params.ids, 3001)
assert.ok(!Object.hasOwn(config.params, 'id'))
