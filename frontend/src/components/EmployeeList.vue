<template>
  <div class="employee-container">
    <div class="header-actions">
      <h2>员工管理系统</h2>
      <button @click="syncOrganization" class="sync-btn">
        <i class="sync-icon"></i> 同步飞书组织架构
      </button>
    </div>

    <div class="filter-section">
      <el-input v-model="searchKeyword" placeholder="搜索员工姓名/工号" class="search-input"/>
      <el-select v-model="departmentId" placeholder="选择部门" class="department-select">
        <el-option label="所有部门" value="0"></el-option>
        <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id"></el-option>
      </el-select>
    </div>

    <el-table :data="employees" stripe class="employee-table">
      <el-table-column prop="name" label="姓名" width="120"></el-table-column>
      <el-table-column prop="employeeId" label="工号" width="100"></el-table-column>
      <el-table-column prop="departmentName" label="部门" width="150"></el-table-column>
      <el-table-column prop="position" label="职位" width="150"></el-table-column>
      <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
      <el-table-column prop="phone" label="电话" width="130"></el-table-column>
      <el-table-column prop="hireDate" label="入职日期" width="120"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" @click="viewEmployee(scope.row.id)">查看</el-button>
          <el-button size="small" type="primary" @click="editEmployee(scope.row.id)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteEmployee(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pagination"
      :current-page="currentPage"
      :page-size="pageSize"
      :total="totalEmployees"
      @current-change="handlePageChange"
    ></el-pagination>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import axios from 'axios';

// 状态管理
const employees = ref([]);
const departments = ref([]);
const searchKeyword = ref('');
const departmentId = ref('0');
const currentPage = ref(1);
const pageSize = ref(10);
const totalEmployees = ref(0);
const loading = ref(false);

// 页面加载时获取数据
onMounted(() => {
  fetchDepartments();
  fetchEmployees();
});

// 获取部门列表
const fetchDepartments = async () => {
  try {
    const response = await axios.get('/api/departments');
    departments.value = response.data.data;
  } catch (error) {
    ElMessage.error('获取部门列表失败: ' + (error.response?.data?.message || error.message));
  }
};

// 获取员工列表
const fetchEmployees = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/employees', {
      params: {
        keyword: searchKeyword.value,
        departmentId: departmentId.value,
        page: currentPage.value,
        size: pageSize.value
      }
    });
    employees.value = response.data.data.records;
    totalEmployees.value = response.data.data.total;
  } catch (error) {
    ElMessage.error('获取员工列表失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
};

// 同步飞书组织架构
const syncOrganization = async () => {
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '正在同步组织架构...',
    background: 'rgba(0, 0, 0, 0.7)'
  });

  try {
    const response = await axios.post('/api/employees/sync-organization');
    ElMessage.success(`同步成功: 新增${response.data.data.added}人, 更新${response.data.data.updated}人`);
    fetchEmployees(); // 同步后刷新员工列表
  } catch (error) {
    ElMessage.error('同步失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loadingInstance.close();
  }
};

// 状态标签样式
const statusType = (status) => {
  switch (status) {
    case '在职': return 'success';
    case '离职': return 'danger';
    case '休假': return 'warning';
    case '试用期': return 'info';
    default: return 'default';
  }
};

// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchEmployees();
};

// 查看员工详情
const viewEmployee = (id) => {
  // 路由跳转逻辑
  window.location.href = `#/employee/${id}`;
};

// 编辑员工
const editEmployee = (id) => {
  window.location.href = `#/employee/edit/${id}`;
};

// 删除员工
const deleteEmployee = async (id) => {
  if (confirm('确定要删除该员工吗?')) {
    try {
      await axios.delete(`/api/employees/${id}`);
      ElMessage.success('删除成功');
      fetchEmployees();
    } catch (error) {
      ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message));
    }
  }
};

// 搜索和筛选事件监听
watch([searchKeyword, departmentId], () => {
  currentPage.value = 1; // 重置页码
  fetchEmployees();
});
</script>

<style scoped>
.employee-container {
  padding: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.sync-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.sync-icon {
  margin-right: 6px;
}

.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.department-select {
  width: 200px;
}

.employee-table {
  width: 100%;
  margin-bottom: 20px;
}

.pagination {
  text-align: right;
}
</style>