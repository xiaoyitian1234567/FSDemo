<template>
  <div class="employee-page">
    <div class="page-header">
      <div>
        <h2>员工列表</h2>
        <p>支持搜索、筛选、分页、查看、编辑和删除，全部本地可演示。</p>
      </div>
      <div class="header-actions">
        <el-button @click="resetDemoData">重置演示数据</el-button>
        <el-button type="primary" @click="router.push('/employee/create')">新增员工</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <div class="filters">
        <el-input v-model="searchKeyword" placeholder="搜索姓名 / 工号 / 职位" clearable />
        <el-select v-model="departmentId">
          <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
        </el-select>
      </div>

      <el-table :data="employees" stripe>
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="employeeId" label="工号" min-width="110" />
        <el-table-column prop="departmentName" label="部门" min-width="140" />
        <el-table-column prop="position" label="职位" min-width="160" />
        <el-table-column prop="location" label="办公地" min-width="120" />
        <el-table-column prop="hireDate" label="入职日期" min-width="120" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/employee/${row.id}`)">查看</el-button>
            <el-button link @click="router.push(`/employee/edit/${row.id}`)">编辑</el-button>
            <el-button link type="danger" @click="removeEmployee(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          layout="total, prev, pager, next"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="totalEmployees"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  deleteEmployeeById,
  getDepartments,
  getEmployees,
  resetEmployees
} from '../mock/employeeStore';

const router = useRouter();
const searchKeyword = ref('');
const departmentId = ref('all');
const currentPage = ref(1);
const pageSize = 10;
const totalEmployees = ref(0);
const employees = ref([]);
const departments = getDepartments();

function loadEmployees() {
  const result = getEmployees({
    keyword: searchKeyword.value,
    departmentId: departmentId.value,
    page: currentPage.value,
    pageSize
  });
  employees.value = result.records;
  totalEmployees.value = result.total;
}

function handlePageChange(page) {
  currentPage.value = page;
  loadEmployees();
}

async function removeEmployee(id) {
  try {
    await ElMessageBox.confirm('确定删除这名员工吗？该操作仅影响本地演示数据。', '删除确认', {
      type: 'warning'
    });
    deleteEmployeeById(id);
    ElMessage.success('删除成功');
    loadEmployees();
  } catch {
    // User cancelled the delete action.
  }
}

function resetDemoData() {
  resetEmployees();
  currentPage.value = 1;
  loadEmployees();
  ElMessage.success('已恢复默认演示数据');
}

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

watch([searchKeyword, departmentId], () => {
  currentPage.value = 1;
  loadEmployees();
}, { immediate: true });
</script>

<style scoped>
.employee-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.page-header h2 {
  margin: 0 0 8px;
}

.page-header p {
  margin: 0;
  color: #6b7280;
}

.header-actions,
.filters {
  display: flex;
  gap: 12px;
}

.filters {
  margin-bottom: 16px;
}

.filters .el-input {
  max-width: 320px;
}

.filters .el-select {
  width: 180px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

@media (max-width: 900px) {
  .page-header,
  .header-actions,
  .filters {
    flex-direction: column;
    align-items: stretch;
  }

  .filters .el-select,
  .filters .el-input {
    max-width: none;
    width: 100%;
  }
}
</style>
