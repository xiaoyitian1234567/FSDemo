<template>
  <div class="dashboard-container">
    <h2 class="dashboard-title">系统概览</h2>

    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <p class="stat-label">员工总数</p>
            <h3 class="stat-value">{{ totalEmployees }}</h3>
            <p class="stat-change"><i class="el-icon-arrow-up"></i> {{ employeeGrowth }}% 本月</p>
          </div>
          <div class="stat-icon employee-icon">
            <i class="el-icon-user"></i>
          </div>
        </div>
        <div class="stat-footer">
          <span>查看全部员工</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <p class="stat-label">部门数量</p>
            <h3 class="stat-value">{{ totalDepartments }}</h3>
            <p class="stat-change"><i class="el-icon-arrow-up"></i> {{ departmentGrowth }}% 本月</p>
          </div>
          <div class="stat-icon department-icon">
            <i class="el-icon-building"></i>
          </div>
        </div>
        <div class="stat-footer">
          <span>查看部门结构</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <p class="stat-label">本月新增</p>
            <h3 class="stat-value">{{ newThisMonth }}</h3>
            <p class="stat-change"><i class="el-icon-arrow-up"></i> {{ newEmployeeGrowth }}% 较上月</p>
          </div>
          <div class="stat-icon new-icon">
            <i class="el-icon-user-plus"></i>
          </div>
        </div>
        <div class="stat-footer">
          <span>查看入职记录</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <p class="stat-label">待处理事项</p>
            <h3 class="stat-value">{{ pendingTasks }}</h3>
            <p class="stat-change"><i class="el-icon-arrow-down"></i> {{ taskReduction }}% 较上周</p>
          </div>
          <div class="stat-icon task-icon">
            <i class="el-icon-bell"></i>
          </div>
        </div>
        <div class="stat-footer">
          <span>查看任务列表</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </el-card>
    </div>

    <div class="dashboard-grid">
      <el-card class="chart-card">
        <div slot="header" class="card-header">
          <h3>员工部门分布</h3>
        </div>
        <div class="chart-container">
          <el-table :data="departmentDistribution" class="distribution-table">
            <el-table-column prop="department" label="部门" width="150"></el-table-column>
            <el-table-column prop="count" label="人数" width="100"></el-table-column>
            <el-table-column prop="percentage" label="占比"></el-table-column>
            <el-table-column label="进度">
              <template #default="scope">
                <el-progress :percentage="scope.row.percentage" :stroke-width="8"></el-progress>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <el-card class="recent-activities">
        <div slot="header" class="card-header">
          <h3>最近活动</h3>
        </div>
        <div class="activities-list">
          <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
            <div class="activity-icon" :class="activity.typeClass"></div>
            <div class="activity-content">
              <p class="activity-text">{{ activity.description }}</p>
              <p class="activity-time">{{ activity.time }}</p>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="upcoming-events">
      <el-card>
        <div slot="header" class="card-header">
          <h3>近期事项</h3>
        </div>
        <el-timeline>
          <el-timeline-item
            v-for="event in upcomingEvents"
            :key="event.id"
            :timestamp="event.date"
            :type="event.type"
          >
            <el-card>
              <h4 class="event-title">{{ event.title }}</h4>
              <p class="event-description">{{ event.description }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();

// 统计数据
const totalEmployees = ref(0);
const totalDepartments = ref(0);
const newThisMonth = ref(0);
const pendingTasks = ref(0);
const employeeGrowth = ref(0);
const departmentGrowth = ref(0);
const newEmployeeGrowth = ref(0);
const taskReduction = ref(0);

// 部门分布
const departmentDistribution = ref([
  { department: '技术部', count: 0, percentage: 0 },
  { department: '人力资源', count: 0, percentage: 0 },
  { department: '财务部', count: 0, percentage: 0 },
  { department: '市场部', count: 0, percentage: 0 },
  { department: '销售部', count: 0, percentage: 0 }
]);

// 最近活动
const recentActivities = ref([
  // 数据将通过API获取
]);

// 近期事项
const upcomingEvents = ref([
  // 数据将通过API获取
]);

onMounted(() => {
  fetchDashboardData();
});

const fetchDashboardData = async () => {
  try {
    // 模拟API调用获取仪表盘数据
    const response = await axios.get('/api/dashboard/stats');
    const stats = response.data.data;

    // 更新统计数据
    totalEmployees.value = stats.employeeStats.total;
    employeeGrowth.value = stats.employeeStats.growthRate;
    totalDepartments.value = stats.departmentStats.total;
    departmentGrowth.value = stats.departmentStats.growthRate;
    newThisMonth.value = stats.employeeStats.newThisMonth;
    newEmployeeGrowth.value = stats.employeeStats.newGrowthRate;
    pendingTasks.value = stats.taskStats.pending;
    taskReduction.value = stats.taskStats.reductionRate;

    // 更新部门分布
    departmentDistribution.value = stats.departmentDistribution;

    // 更新最近活动
    recentActivities.value = stats.recentActivities.map(activity => ({
      ...activity,
      typeClass: getActivityTypeClass(activity.type)
    }));

    // 更新近期事项
    upcomingEvents.value = stats.upcomingEvents;
  } catch (error) {
    console.error('获取仪表盘数据失败:', error);
    // 使用模拟数据
    loadMockData();
  }
};

// 获取活动类型样式
const getActivityTypeClass = (type) => {
  switch (type) {
    case 'create': return 'activity-icon create';
    case 'update': return 'activity-icon update';
    case 'delete': return 'activity-icon delete';
    case 'sync': return 'activity-icon sync';
    default: return 'activity-icon default';
  }
};

// 加载模拟数据（API失败时）
const loadMockData = () => {
  totalEmployees.value = 128;
  employeeGrowth.value = 5.2;
  totalDepartments.value = 8;
  departmentGrowth.value = 0;
  newThisMonth.value = 12;
  newEmployeeGrowth.value = 20;
  pendingTasks.value = 5;
  taskReduction.value = 15;

  departmentDistribution.value = [
    { department: '技术部', count: 45, percentage: 35.16 },
    { department: '人力资源', count: 12, percentage: 9.38 },
    { department: '财务部', count: 8, percentage: 6.25 },
    { department: '市场部', count: 23, percentage: 17.97 },
    { department: '销售部', count: 40, percentage: 31.25 }
  ];

  recentActivities.value = [
    { id: 1, description: '新增员工：张三', time: '今天 09:45', typeClass: 'activity-icon create' },
    { id: 2, description: '更新员工信息：李四', time: '昨天 14:20', typeClass: 'activity-icon update' },
    { id: 3, description: '同步飞书组织架构', time: '昨天 10:15', typeClass: 'activity-icon sync' },
    { id: 4, description: '删除员工：王五', time: '2023-06-10 16:30', typeClass: 'activity-icon delete' },
    { id: 5, description: '新增部门：产品部', time: '2023-06-09 09:10', typeClass: 'activity-icon create' }
  ];

  upcomingEvents.value = [
    { id: 1, title: '员工入职', date: '2023-06-15', description: '新员工入职培训', type: 'primary' },
    { id: 2, title: '部门会议', date: '2023-06-16', description: '技术部周会', type: 'success' },
    { id: 3, title: '合同到期', date: '2023-06-20', description: '员工合同续签提醒', type: 'warning' }
  ];
};
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-title {
  margin-bottom: 20px;
  color: #303133;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  height: 100%;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-info {
  flex-grow: 1;
}

.stat-label {
  color: #909399;
  margin: 0 0 5px 0;
}

.stat-value {
  font-size: 24px;
  margin: 0 0 5px 0;
  color: #303133;
}

.stat-change {
  font-size: 12px;
  margin: 0;
  color: #67c23a;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.employee-icon {
  background-color: #409eff;
}

.department-icon {
  background-color: #67c23a;
}

.new-icon {
  background-color: #e6a23c;
}

.task-icon {
  background-color: #f56c6c;
}

.stat-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px dashed #e8e8e8;
  color: #409eff;
  font-size: 13px;
  cursor: pointer;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.chart-card {
  height: 100%;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.distribution-table {
  width: 100%;
}

.recent-activities {
  height: 100%;
}

.activities-list {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.activity-icon {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 10px;
  margin-top: 4px;
}

.activity-icon.create {
  background-color: #67c23a;
}

.activity-icon.update {
  background-color: #409eff;
}

.activity-icon.delete {
  background-color: #f56c6c;
}

.activity-icon.sync {
  background-color: #e6a23c;
}

.activity-icon.default {
  background-color: #909399;
}

.activity-content {
  flex-grow: 1;
}

.activity-text {
  margin: 0 0 5px 0;
  color: #303133;
}

.activity-time {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.upcoming-events {
  margin-top: 30px;
}

.event-title {
  margin: 0 0 5px 0;
  color: #303133;
}

.event-description {
  margin: 0;
  color: #606266;
  font-size: 13px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>