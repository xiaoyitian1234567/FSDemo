<template>
  <div v-if="employee" class="employee-detail">
    <div class="detail-header">
      <div class="profile-card">
        <div class="avatar">{{ employee.name.slice(0, 1) }}</div>
        <div>
          <h2>{{ employee.name }}</h2>
          <p>{{ employee.position }} · {{ employee.departmentName }}</p>
          <el-tag :type="statusType(employee.status)" effect="light">{{ employee.status }}</el-tag>
        </div>
      </div>
      <div class="detail-actions">
        <el-button @click="router.push('/employees')">返回列表</el-button>
        <el-button type="primary" @click="router.push(`/employee/edit/${employee.id}`)">编辑</el-button>
      </div>
    </div>

    <div class="detail-grid">
      <el-card shadow="never">
        <template #header><span>基础信息</span></template>
        <div class="info-list">
          <div class="info-row"><span>工号</span><strong>{{ employee.employeeId }}</strong></div>
          <div class="info-row"><span>邮箱</span><strong>{{ employee.email }}</strong></div>
          <div class="info-row"><span>电话</span><strong>{{ employee.phone }}</strong></div>
          <div class="info-row"><span>办公地</span><strong>{{ employee.location }}</strong></div>
        </div>
      </el-card>

      <el-card shadow="never">
        <template #header><span>雇佣信息</span></template>
        <div class="info-list">
          <div class="info-row"><span>入职日期</span><strong>{{ employee.hireDate }}</strong></div>
          <div class="info-row"><span>合同周期</span><strong>{{ employee.contractStart }} 至 {{ employee.contractEnd }}</strong></div>
          <div class="info-row"><span>直属上级</span><strong>{{ employee.supervisor }}</strong></div>
          <div class="info-row"><span>绩效评级</span><strong>{{ employee.performance }}</strong></div>
        </div>
      </el-card>

      <el-card shadow="never" class="salary-card">
        <template #header><span>薪酬信息</span></template>
        <div class="salary-value">¥{{ Number(employee.salary).toLocaleString('zh-CN') }}</div>
        <p class="salary-note">最近更新时间：{{ employee.lastUpdated }}</p>
      </el-card>
    </div>
  </div>

  <el-empty v-else description="未找到该员工信息">
    <el-button type="primary" @click="router.push('/employees')">返回列表</el-button>
  </el-empty>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getEmployeeById } from '../mock/employeeStore';

const route = useRoute();
const router = useRouter();
const employee = computed(() => getEmployeeById(route.params.id));

function statusType(status) {
  switch (status) {
    case '在职':
      return 'success';
    case '试用期':
      return 'warning';
    case '休假':
      return 'info';
    case '离职':
      return 'danger';
    default:
      return 'default';
  }
}
</script>

<style scoped>
.employee-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 24px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

.profile-card {
  display: flex;
  gap: 16px;
  align-items: center;
}

.profile-card h2 {
  margin: 0 0 6px;
}

.profile-card p {
  margin: 0 0 10px;
  color: #6b7280;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff;
  font-size: 28px;
  font-weight: 700;
}

.detail-actions {
  display: flex;
  gap: 12px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: #4b5563;
}

.info-row strong {
  color: #111827;
  text-align: right;
}

.salary-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.salary-value {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
}

.salary-note {
  color: #6b7280;
  margin-bottom: 0;
}

@media (max-width: 960px) {
  .detail-header,
  .detail-grid {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
