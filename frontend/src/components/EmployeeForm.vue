<template>
  <div class="employee-form-page">
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <h2>{{ isEditMode ? '编辑员工' : '新增员工' }}</h2>
            <p>保存后会直接写入本地演示数据，可立即回到列表查看效果。</p>
          </div>
          <el-button @click="router.push('/employees')">返回列表</el-button>
        </div>
      </template>

      <el-form ref="employeeForm" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :md="12" :xs="24">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="formData.name" />
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="工号" prop="employeeId">
              <el-input v-model="formData.employeeId" :disabled="isEditMode" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :md="12" :xs="24">
            <el-form-item label="部门" prop="departmentId">
              <el-select v-model="formData.departmentId" placeholder="请选择部门">
                <el-option
                  v-for="dept in departments.filter((item) => item.id !== 'all')"
                  :key="dept.id"
                  :label="dept.name"
                  :value="dept.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="职位" prop="position">
              <el-input v-model="formData.position" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :md="12" :xs="24">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" />
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :md="12" :xs="24">
            <el-form-item label="办公地" prop="location">
              <el-input v-model="formData.location" />
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="直属上级" prop="supervisor">
              <el-input v-model="formData.supervisor" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :md="12" :xs="24">
            <el-form-item label="入职日期" prop="hireDate">
              <el-date-picker
                v-model="formData.hireDate"
                type="date"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="员工状态" prop="status">
              <el-select v-model="formData.status">
                <el-option label="在职" value="在职" />
                <el-option label="试用期" value="试用期" />
                <el-option label="休假" value="休假" />
                <el-option label="离职" value="离职" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :md="12" :xs="24">
            <el-form-item label="合同开始" prop="contractStart">
              <el-date-picker
                v-model="formData.contractStart"
                type="date"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="合同结束" prop="contractEnd">
              <el-date-picker
                v-model="formData.contractEnd"
                type="date"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :md="12" :xs="24">
            <el-form-item label="月薪" prop="salary">
              <el-input-number v-model="formData.salary" :min="3000" :step="500" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="绩效评级" prop="performance">
              <el-select v-model="formData.performance">
                <el-option label="A" value="A" />
                <el-option label="A-" value="A-" />
                <el-option label="B+" value="B+" />
                <el-option label="B" value="B" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="submitForm">{{ isEditMode ? '保存修改' : '创建员工' }}</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getDepartments, getEmployeeById, saveEmployee } from '../mock/employeeStore';

const route = useRoute();
const router = useRouter();
const employeeForm = ref();
const isEditMode = Boolean(route.params.id);
const departments = getDepartments();

const existingEmployee = isEditMode ? getEmployeeById(route.params.id) : null;

const formData = reactive({
  name: existingEmployee?.name || '',
  employeeId: existingEmployee?.employeeId || generateEmployeeId(),
  departmentId: existingEmployee?.departmentId || 'tech',
  position: existingEmployee?.position || '',
  email: existingEmployee?.email || '',
  phone: existingEmployee?.phone || '',
  hireDate: existingEmployee?.hireDate || '',
  status: existingEmployee?.status || '在职',
  contractStart: existingEmployee?.contractStart || '',
  contractEnd: existingEmployee?.contractEnd || '',
  salary: existingEmployee?.salary || 10000,
  supervisor: existingEmployee?.supervisor || '',
  location: existingEmployee?.location || '',
  performance: existingEmployee?.performance || 'B+'
});

const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  employeeId: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  position: [{ required: true, message: '请输入职位', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  hireDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
  contractStart: [{ required: true, message: '请选择合同开始日期', trigger: 'change' }],
  contractEnd: [{ required: true, message: '请选择合同结束日期', trigger: 'change' }]
};

function generateEmployeeId() {
  const random = Math.floor(Math.random() * 900 + 100);
  return `EMP26${random}`;
}

async function submitForm() {
  await employeeForm.value.validate();
  saveEmployee({ ...formData }, route.params.id);
  ElMessage.success(isEditMode ? '员工信息已更新' : '员工已创建');
  router.push('/employees');
}

function resetForm() {
  if (isEditMode && existingEmployee) {
    Object.assign(formData, existingEmployee);
    return;
  }

  Object.assign(formData, {
    name: '',
    employeeId: generateEmployeeId(),
    departmentId: 'tech',
    position: '',
    email: '',
    phone: '',
    hireDate: '',
    status: '在职',
    contractStart: '',
    contractEnd: '',
    salary: 10000,
    supervisor: '',
    location: '',
    performance: 'B+'
  });
}
</script>

<style scoped>
.employee-form-page {
  padding-bottom: 24px;
}

.form-card {
  max-width: 920px;
  margin: 0 auto;
  border-radius: 18px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.card-header h2 {
  margin: 0 0 8px;
}

.card-header p {
  margin: 0;
  color: #6b7280;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

@media (max-width: 900px) {
  .card-header {
    flex-direction: column;
  }
}
</style>
