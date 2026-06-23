// vite的配置文件，用于配置项目打包、开发服务器、插件等
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
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
