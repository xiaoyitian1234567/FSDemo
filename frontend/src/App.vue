<template>
  <div id="app">
    <el-container class="app-container">
      <el-header class="app-header">
        <div class="logo">
          <el-icon><OfficeBuilding /></el-icon>
          <span class="logo-text">员工管理系统</span>
        </div>
        <div class="user-info">
          <el-dropdown trigger="click">
            <span class="user-name">
              <el-icon><UserFilled /></el-icon>
              管理员
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item><el-icon><Setting /></el-icon>个人设置</el-dropdown-item>
                <el-dropdown-item><el-icon><QuestionFilled /></el-icon>帮助中心</el-dropdown-item>
                <el-dropdown-item divided><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
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
              <el-icon><House /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>

            <el-submenu index="2">
              <template #title>
                <el-icon><User /></el-icon>
                <span>员工管理</span>
              </template>
              <el-menu-item index="2-1" @click="$router.push('/employees')">
                <el-icon><UserFilled /></el-icon>
                员工列表
              </el-menu-item>
              <el-menu-item index="2-2" @click="$router.push('/employee/create')">
                <el-icon><Plus /></el-icon>
                新增员工
              </el-menu-item>
            </el-submenu>

            <el-menu-item index="3">
              <el-icon><OfficeBuilding /></el-icon>
              <span>部门管理</span>
            </el-menu-item>

            <el-menu-item index="4">
              <el-icon><Document /></el-icon>
              <span>薪资管理</span>
            </el-menu-item>

            <el-menu-item index="5">
              <el-icon><PieChart /></el-icon>
              <span>统计报表</span>
            </el-menu-item>

            <el-menu-item index="6">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
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

.logo .el-icon {
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

.user-name .el-icon {
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
