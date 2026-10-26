<template>
  <div class="employee-detail">
    <el-card class="detail-card">
      <div slot="header" class="card-header">
        <h2>员工详情</h2>
        <el-button @click="goBack">返回列表</el-button>
      </div>

      <div v-if="loading" class="loading-container">
        <el-loading-spinner class="loading-spinner"></el-loading-spinner>
        <p>加载中...</p>
      </div>

      <div v-else class="detail-content">
        <div class="employee-info">
          <div class="basic-info">
            <div class="avatar-section">
              <div class="avatar" :style="{ backgroundColor: avatarColor }">
                {{ employee.name.charAt(0) }}
              </div>
              <div class="name-title">
                <h1>{{ employee.name }}</h1>
                <p class="position">{{ employee.position }}</p>
              </div>
            </div>

            <div class="contact-info">
              <div class="info-item">
                <span class="label">工号:</span>
                <span class="value">{{ employee.employeeId }}</span>
              </div>
              <div class="info-item">
                <span class="label">邮箱:</span>
                <span class="value">{{ employee.email }}</span>
              </div>
              <div class="info-item">
                <span class="label">电话:</span>
                <span class="value">{{ employee.phone }}</span>
              </div>
              <div class="info-item">
                <span class="label">部门:</span>
                <span class="value">{{ employee.departmentName }}</span>
              </div>
            </div>
          </div>

          <div class="employment-details">
            <h3> employment Information</h3>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">入职日期:</span>
                <span class="detail-value">{{ employee.hireDate }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">合同期限:</span>
                <span class="detail-value">{{ employee.contractStart }} - {{ employee.contractEnd }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">薪资:</span>
                <span class="detail-value">{{ formatSalary(employee.salary) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">状态:</span>
                <span class="detail-value"><el-tag :type="statusType(employee.status)">{{ employee.status }}</el-tag></span>
              </div>
              <div class="detail-item">
                <span class="detail-label">直属上级:</span>
                <span class="detail-value">{{ employee.supervisor || '无' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">最后更新:</span>
                <span class="detail-value">{{ employee.lastUpdated }}</span>
              </div>
            </div>
          </div>

          <div class="action-buttons">
            <el-button type="primary" @click="editEmployee">编辑</el-button>
            <el-button type="danger" @click="deleteEmployee">删除</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElLoading } from 'element-plus';
import axios from 'axios';

const route = useRoute();
const router = useRouter();
const employee = ref({});
const loading = ref(true);

onMounted(() => {
  fetchEmployeeDetail();
});

const fetchEmployeeDetail = async () => {
  try {
    const response = await axios.get(`/api/employees/${route.params.id}`);
    employee.value = response.data.data;
  } catch (error) {
    ElMessage.error('获取员工详情失败: ' + (error.response?.data?.message || error.message));
    router.push('/employees');
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  router.go(-1);
};

const editEmployee = () => {
  router.push(`/employee/edit/${route.params.id}`);
};

const deleteEmployee = async () => {
  if (confirm(`确定要删除员工 ${employee.value.name} 吗?`)) {
    try {
      await axios.delete(`/api/employees/${route.params.id}`);
      ElMessage.success('删除成功');
      router.push('/employees');
    } catch (error) {
      ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message));
    }
  }
};

const statusType = (status) => {
  switch (status) {
    case '在职': return 'success';
    case '离职': return 'danger';
    case '休假': return 'warning';
    case '试用期': return 'info';
    default: return 'default';
  }
};

const formatSalary = (salary) => {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(salary);
};

const avatarColor = ref('#409eff');
</script>

<style scoped>
.employee-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container {
  text-align: center;
  padding: 50px 0;
}

.loading-spinner {
  font-size: 48px;
  margin-bottom: 16px;
}

.employee-info {
  padding: 20px;
}

.basic-info {
  display: flex;
  margin-bottom: 30px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 20px;
}

.avatar-section {
  display: flex;
  align-items: center;
  margin-right: 30px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: bold;
  margin-right: 20px;
}

.name-title h1 {
  margin: 0 0 5px 0;
  font-size: 24px;
}

.position {
  color: #606266;
  font-size: 16px;
  margin: 0;
}

.contact-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  flex-grow: 1;
}

.info-item {
  display: flex;
  margin-bottom: 10px;
}

.label {
  color: #909399;
  width: 80px;
}

.value {
  color: #303133;
  font-weight: 500;
}

.employment-details h3 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  color: #303133;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.detail-item {
  margin-bottom: 15px;
}

.detail-label {
  color: #909399;
  display: inline-block;
  width: 100px;
}

.detail-value {
  color: #303133;
}

.action-buttons {
  margin-top: 30px;
  text-align: right;
}

.action-buttons button {
  margin-left: 10px;
}
</style>