<template>
  <div class="not-found-container">
    <div class="error-code">404</div>
    <div class="error-message">页面不存在</div>
    <p class="error-description">
      抱歉，您访问的页面不存在或已被删除。请检查URL是否正确或返回首页。
    </p>
    <div class="action-buttons">
      <el-button type="primary" @click="goToDashboard">返回仪表盘</el-button>
      <el-button @click="goBack">返回上一页</el-button>
    </div>
    <div class="error-image">
      <img src="/error-404.svg" alt="Page not found" class="svg-image"/>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const goToDashboard = () => {
  router.push('/dashboard');
};

const goBack = () => {
  router.go(-1);
};
</script>

<style scoped>
.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 60px);
  padding: 20px;
  text-align: center;
  background-color: #f5f7fa;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  background: linear-gradient(135deg, #409eff, #67c23a);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 20px;
}

.error-message {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.error-description {
  font-size: 16px;
  color: #606266;
  max-width: 500px;
  line-height: 1.5;
  margin-bottom: 30px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 40px;
}

.error-image {
  max-width: 400px;
  width: 100%;
}

.svg-image {
  width: 100%;
  height: auto;
}
</style>