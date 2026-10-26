<template>
  <div class="dashboard">
    <div class="dashboard-hero">
      <div>
        <p class="eyebrow">演示版后台</p>
        <h2>员工管理总览</h2>
        <p class="hero-text">当前页面完全基于本地 Mock 数据，可直接用于面试演示，不依赖外部系统和后端服务。</p>
      </div>
      <el-button type="primary" @click="router.push('/employee/create')">新增员工</el-button>
    </div>

    <div class="stats-grid">
      <el-card v-for="card in statCards" :key="card.label" shadow="hover" class="stat-card">
        <div class="stat-label">{{ card.label }}</div>
        <div class="stat-value">{{ card.value }}</div>
        <div class="stat-note">{{ card.note }}</div>
      </el-card>
    </div>

    <div class="content-grid">
      <el-card shadow="never">
        <template #header>
          <div class="section-header">
            <span>部门分布</span>
            <el-button link @click="router.push('/employees')">查看员工列表</el-button>
          </div>
        </template>
        <el-table :data="stats.departmentStats" size="large">
          <el-table-column prop="department" label="部门" min-width="160" />
          <el-table-column prop="count" label="人数" width="100" />
          <el-table-column label="占比" min-width="220">
            <template #default="{ row }">
              <div class="progress-cell">
                <el-progress :percentage="row.percentage" :stroke-width="10" />
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="section-header">
            <span>最近活动</span>
            <el-tag type="success">本地演示数据</el-tag>
          </div>
        </template>
        <div class="activity-list">
          <div v-for="item in stats.recentActivities" :key="item.id" class="activity-item">
            <el-tag :type="item.type" effect="light">{{ item.time }}</el-tag>
            <div class="activity-content">
              <div class="activity-title">{{ item.title }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { getDashboardStats } from '../mock/employeeStore';

const router = useRouter();
const stats = getDashboardStats();

const statCards = computed(() => [
  {
    label: '员工总数',
    value: stats.totalEmployees,
    note: '用于演示列表、详情和搜索筛选'
  },
  {
    label: '在职员工',
    value: stats.activeCount,
    note: '可作为业务运营核心指标'
  },
  {
    label: '试用期员工',
    value: stats.probationCount,
    note: '便于展示状态标签与人事流程'
  },
  {
    label: '平均薪资',
    value: `¥${stats.averageSalary.toLocaleString('zh-CN')}`,
    note: '演示统计卡片和格式化能力'
  }
]);
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dashboard-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 28px;
  border-radius: 20px;
  background: linear-gradient(135deg, #1d4ed8, #4f46e5);
  color: #fff;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 13px;
  opacity: 0.85;
}

.dashboard-hero h2 {
  margin: 0 0 8px;
  font-size: 30px;
}

.hero-text {
  margin: 0;
  max-width: 640px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.88);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  border-radius: 16px;
}

.stat-label {
  color: #6b7280;
  font-size: 14px;
}

.stat-value {
  margin-top: 10px;
  font-size: 32px;
  font-weight: 700;
  color: #111827;
}

.stat-note {
  margin-top: 8px;
  color: #6b7280;
  line-height: 1.6;
}

.content-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-cell {
  min-width: 180px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.activity-item {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eef2f7;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-title {
  font-weight: 600;
  color: #111827;
}

@media (max-width: 960px) {
  .stats-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-hero {
    flex-direction: column;
  }
}
</style>
