<template>
  <div id="app">
    <el-container class="app-container">
      <el-header class="app-header">
        <div class="logo">
          <i class="el-icon-company"></i>
          <span class="logo-text">员工管理系统</span>
        </div>
        <div class="user-info">
          <el-dropdown trigger="click">
            <span class="user-name">
              <i class="el-icon-user-circle"></i> 管理员
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item><i class="el-icon-setting"></i> 个人设置</el-dropdown-item>
              <el-dropdown-item><i class="el-icon-help"></i> 帮助中心</el-dropdown-item>
              <el-dropdown-item divided><i class="el-icon-switch-button"></i> 退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <el-aside width="200px" class="app-aside">
          <el-menu
            default-active="1"
            class="el-menu-vertical-demo"
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b"
          >
            <el-menu-item index="1" @click="$router.push('/dashboard')">
              <i class="el-icon-s-home"></i>
              <span slot="title">仪表盘</span>
            </el-menu-item>

            <el-submenu index="2">
              <template slot="title">
                <i class="el-icon-user"></i>
                <span>员工管理</span>
              </template>
              <el-menu-item index="2-1" @click="$router.push('/employees')">
                <i class="el-icon-user-solid"></i> 员工列表
              </el-menu-item>
              <el-menu-item index="2-2" @click="$router.push('/employee/create')">
                <i class="el-icon-user-plus"></i> 新增员工
              </el-menu-item>
            </el-submenu>

            <el-menu-item index="3">
              <i class="el-icon-building"></i>
              <span slot="title">部门管理</span>
            </el-menu-item>

            <el-menu-item index="4">
              <i class="el-icon-document"></i>
              <span slot="title">薪资管理</span>
            </el-menu-item>

            <el-menu-item index="5">
              <i class="el-icon-pie-chart"></i>
              <span slot="title">统计报表</span>
            </el-menu-item>

            <el-menu-item index="6">
              <i class="el-icon-setting"></i>
              <span slot="title">系统设置</span>
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
import { useRoute } from 'vue-router';

const route = useRoute();
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.app-header {
  background-color: #fff;
  padding: 0 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  height: 60px;
}

.logo i {
  font-size: 24px;
  color: #409eff;
  margin-right: 10px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #303133;
}

.user-name i {
  font-size: 20px;
  margin-right: 5px;
}

.app-aside {
  background-color: #545c64;
}

.app-main {
  padding: 20px;
  background-color: #f5f7fa;
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
}
</style>
