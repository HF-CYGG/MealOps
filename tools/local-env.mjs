/**
 * 本文件用于读取并解析项目根目录下的本地环境变量文件。
 * 该模块只服务于本地开发场景，会优先加载被 Git 忽略的 `.env.local`，
 * 让开发者可以把数据库账号、密码等敏感配置保留在本机，而不是提交到仓库。
 */

import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const currentDir = path.dirname(fileURLToPath(import.meta.url))
const projectRoot = path.resolve(currentDir, '..')

/**
 * 解析一行环境变量，支持常见的 `KEY=value` 格式。
 * @param {string} line 单行文本
 * @returns {[string, string] | null}
 */
const parseEnvLine = (line) => {
  const trimmedLine = line.trim()

  // 空行和注释行直接跳过，避免把说明文本误当成配置项。
  if (!trimmedLine || trimmedLine.startsWith('#')) {
    return null
  }

  const separatorIndex = trimmedLine.indexOf('=')
  if (separatorIndex <= 0) {
    return null
  }

  const key = trimmedLine.slice(0, separatorIndex).trim()
  const rawValue = trimmedLine.slice(separatorIndex + 1).trim()
  const value = rawValue.replace(/^(['"])(.*)\1$/, '$2')

  return [key, value]
}

/**
 * 读取并解析指定环境变量文件。
 * @param {string} filePath 文件绝对路径
 * @returns {Record<string, string>}
 */
export const parseEnvFile = (filePath) => {
  if (!fs.existsSync(filePath)) {
    return {}
  }

  const content = fs.readFileSync(filePath, 'utf8')
  const envEntries = content
    .split(/\r?\n/)
    .map(parseEnvLine)
    .filter(Boolean)

  return Object.fromEntries(envEntries)
}

/**
 * 加载项目根目录下的 `.env.local`，并与当前进程环境变量合并。
 * 如果外部终端已经显式传入同名变量，则保留外部值，避免覆盖用户手动指定配置。
 * @returns {{ env: NodeJS.ProcessEnv, envFilePath: string, loadedKeys: string[] }}
 */
export const loadLocalEnv = () => {
  const envFilePath = path.join(projectRoot, '.env.local')
  const localEnv = parseEnvFile(envFilePath)
  const mergedEnv = { ...localEnv, ...process.env }

  return {
    env: mergedEnv,
    envFilePath,
    loadedKeys: Object.keys(localEnv)
  }
}
