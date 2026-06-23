// 本文件用于配置 Vite 开发服务器、构建行为，以及在启动时输出更友好的项目访问地址。
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// 该插件在开发服务器启动后额外输出管理端与用户端的访问地址，方便本地联调。
function mealopsEntryHintPlugin() {
  return {
    name: 'mealops-entry-hint',
    configureServer(server) {
      server.httpServer?.once('listening', () => {
        const addressInfo = server.httpServer.address()
        const rawHost =
          typeof addressInfo === 'object' && addressInfo?.address ? addressInfo.address : 'localhost'
        const host =
          rawHost === '::' || rawHost === '::1' || rawHost === '0.0.0.0' ? 'localhost' : rawHost
        const port =
          typeof addressInfo === 'object' && addressInfo?.port ? addressInfo.port : 5173
        const baseUrl = `http://${host}:${port}`

        // 使用清晰的中文提示把常用入口直接打印在终端中。
        console.log('\nMealOps 本地入口地址：')
        console.log(`- 管理端地址: ${baseUrl}/login`)
        console.log(`- 用户端地址: ${baseUrl}/client/home\n`)
      })
    }
  }
}

export default defineConfig({
  plugins: [vue(), mealopsEntryHintPlugin()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      // 配置API代理，将以/api开头的请求转发到后端8080端口
      '/api': {
        target: 'http://localhost:8080', // 后端服务器地址
        changeOrigin: true, // 允许跨域
        rewrite: (path) => path.replace(/^\/api/, '') // 如果后端不需要/api前缀则可以重写，这里保留常规配置
      }
    }
  }
})
