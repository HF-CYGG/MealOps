import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const root = resolve(__dirname, '..')

const loadSetmealModule = async () => {
  const source = readFileSync(resolve(root, 'src/api/setmeal.js'), 'utf8')
  const moduleSource = source.replace(
    "import request from '@/utils/request'",
    `const request = (config) => {
      globalThis.__setmealRequestConfig = config
      return config
    }`
  )

  return import(`data:text/javascript;charset=utf-8,${encodeURIComponent(moduleSource)}`)
}

delete globalThis.__setmealRequestConfig

const { changeSetmealStatus } = await loadSetmealModule()

changeSetmealStatus(0, 5001)

const config = globalThis.__setmealRequestConfig

assert.equal(config.url, '/setmeal/status/0')
assert.equal(config.method, 'post')
assert.equal(config.params.ids, 5001)
assert.ok(!Object.hasOwn(config.params, 'id'))
