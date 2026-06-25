/**
 * 本文件用于在执行 npm 脚本前自动加载项目根目录下的 `.env.local`。
 * 这样本地开发时可以把数据库账号、密码等敏感配置只保存在本机，
 * 然后通过根目录 `npm run dev`、`npm run dev:backend:local` 等脚本自动继承。
 */

import { spawn } from 'node:child_process'
import { loadLocalEnv } from './local-env.mjs'

const scriptName = process.argv[2]

if (!scriptName) {
  console.error('缺少要执行的 npm 脚本名称')
  process.exit(1)
}

const { env, loadedKeys, envFilePath } = loadLocalEnv()

// 仅输出是否读取到本地配置文件及关键字段数量，避免把敏感值打印到终端日志。
if (loadedKeys.length > 0) {
  console.log(`已加载本地环境文件：${envFilePath}`)
  console.log(`已注入 ${loadedKeys.length} 个本地环境变量`)
} else {
  console.log(`未发现本地环境文件：${envFilePath}，将按当前终端环境继续启动`)
}

const child = spawn('npm', ['run', scriptName], {
  cwd: process.cwd(),
  env,
  stdio: 'inherit',
  shell: true
})

child.on('exit', (code) => {
  process.exit(code ?? 0)
})
