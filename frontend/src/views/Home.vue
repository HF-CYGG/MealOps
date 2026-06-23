<!-- 首页视图组件 -->
<template>
  <div class="home-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>欢迎使用 MealOps</span>
        </div>
      </template>
      <div class="content">
        <p>Vue3 + Element Plus + Pinia + Vue Router 项目初始化成功！</p>
        <el-button type="primary" @click="testAxios">测试 Axios 请求</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// 测试Axios请求的方法
const testAxios = async () => {
  try {
    // 这里发送一个测试请求到代理地址，后端如果没有对应接口可能会报404
    await request.get('/test')
    ElMessage.success('请求发送成功')
  } catch (error) {
    // 错误已经在 request.js 拦截器中处理，这里也可以做额外处理
    console.log('测试请求报错：', error)
  }
}
</script>

<style scoped>
.home-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: 20px;
}

.box-card {
  width: 480px;
}

.card-header {
  font-weight: bold;
  font-size: 18px;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-items: center;
}
</style>
