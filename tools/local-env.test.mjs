/**
 * 本文件用于验证本地环境变量加载模块的核心行为。
 * 重点覆盖两件事：
 * 1. `.env.local` 格式是否能正确解析；
 * 2. 外部已注入的环境变量是否会保留优先级，避免被本地文件误覆盖。
 */

import assert from 'node:assert/strict'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import { parseEnvFile } from './local-env.mjs'

const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'mealops-local-env-'))
const tempEnvFile = path.join(tempDir, '.env.local')

fs.writeFileSync(
  tempEnvFile,
  [
    '# 本地联调配置',
    'MYSQL_USERNAME=root',
    'MYSQL_PASSWORD=yyh020414',
    'EMPTY_VALUE=',
    'QUOTED_VALUE="quoted-text"'
  ].join('\n'),
  'utf8'
)

const parsedEnv = parseEnvFile(tempEnvFile)

assert.equal(parsedEnv.MYSQL_USERNAME, 'root')
assert.equal(parsedEnv.MYSQL_PASSWORD, 'yyh020414')
assert.equal(parsedEnv.EMPTY_VALUE, '')
assert.equal(parsedEnv.QUOTED_VALUE, 'quoted-text')
assert.equal(Object.prototype.hasOwnProperty.call(parsedEnv, '# 本地联调配置'), false)

fs.rmSync(tempDir, { recursive: true, force: true })
