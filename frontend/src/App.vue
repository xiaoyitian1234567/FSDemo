<template>
  <div id="app">
    <el-container class="app-container">
      <el-header class="app-header">
        <div class="logo">
          <el-icon><Grid /></el-icon>
          <div>
            <div class="logo-text">招聘演示后台</div>
            <div class="logo-subtitle">员工管理前端 Demo</div>
          </div>
        </div>
        <div class="user-info">
          <el-tag type="success" effect="light">本地 Mock 模式</el-tag>
        </div>
      </el-header>

      <el-container>
        <el-aside width="220px" class="app-aside">
          <el-menu
            :default-active="activePath"
            router
            class="el-menu-vertical-demo"
            background-color="#0f172a"
            text-color="#cbd5e1"
            active-text-color="#ffffff"
          >
            <el-menu-item index="/dashboard">
              <el-icon><House /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>

            <el-submenu index="employee">
              <template #title>
                <el-icon><User /></el-icon>
                <span>员工管理</span>
              </template>
              <el-menu-item index="/employees">
                <el-icon><UserFilled /></el-icon>
                员工列表
              </el-menu-item>
              <el-menu-item index="/employee/create">
                <el-icon><Plus /></el-icon>
                新增员工
              </el-menu-item>
            </el-submenu>

            <el-menu-item index="/dashboard">
              <el-icon><PieChart /></el-icon>
              <span>统计展示</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const activePath = computed(() => {
  if (route.path.startsWith('/employee/')) {
    return route.path.includes('/create') ? '/employee/create' : '/employees';
  }
  return route.path;
});
</script>

<style scoped>
.app-container {
  min-height: 100vh;
}

.app-header {
  background-color: #fff;
  padding: 0 24px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 72px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo .el-icon {
  font-size: 24px;
  color: #2563eb;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.logo-subtitle {
  font-size: 12px;
  color: #6b7280;
}

.user-info {
  display: flex;
  align-items: center;
}

.app-aside {
  background-color: #0f172a;
}

.app-main {
  padding: 24px;
  background-color: #f3f6fb;
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 220px;
  min-height: calc(100vh - 72px);
  border-right: none;
}

@media (max-width: 900px) {
  .app-aside {
    display: none;
  }

  .app-main {
    padding: 16px;
  }
}
</style>
