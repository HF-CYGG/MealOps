// 本文件用于配置 Vite 开发服务器、构建行为，以及在启动时输出更友好的项目访问地址与联调提示。
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// 该插件会在开发服务器真正监听端口后输出管理端、用户端入口与后端依赖说明，减少本地联调时的启动歧义。
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

        // 使用清晰的中文提示把常用入口、依赖后端地址与备用联调入口直接打印到终端中。
        console.log('\nMealOps 前端开发服务已启动：')
        console.log(`- 管理端地址: ${baseUrl}/login`)
        console.log(`- 用户端地址: ${baseUrl}/client/home`)
        console.log('- 本地后端地址: http://localhost:8080')
        console.log('- Swagger 地址: http://localhost:8080/swagger-ui.html')
        console.log('- 若只联调后端静态页，可访问: http://localhost:8080/static/admin.html')
        console.log('- 若页面能打开但接口失败，请先确认后端已启动\n')
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
      // 配置 API 代理，将以 /api 开头的请求转发到本地后端 8080 端口，避免开发阶段跨域。
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})