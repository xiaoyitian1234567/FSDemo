<template>
  <div class="employee-form">
    <el-card class="form-card">
      <div slot="header" class="card-header">
        <h2>{{ isEditMode ? '编辑员工' : '新增员工' }}</h2>
        <el-button @click="goBack">返回</el-button>
      </div>

      <el-form
        ref="employeeForm"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="form-container"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="formData.name" placeholder="请输入员工姓名"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工号" prop="employeeId" v-if="!isEditMode">
              <el-input v-model="formData.employeeId" placeholder="请输入工号"/>
            </el-form-item>
            <el-form-item label="工号" v-else>
              <el-input v-model="formData.employeeId" disabled/>
              <el-form-item__error>工号不可修改</el-form-item__error>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" prop="departmentId">
              <el-select v-model="formData.departmentId" placeholder="请选择部门">
                <el-option
                  v-for="dept in departments"
                  :key="dept.id"
                  :label="dept.name"
                  :value="dept.id"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-input v-model="formData.position" placeholder="请输入职位"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" type="email" placeholder="请输入邮箱"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入电话号码"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入职日期" prop="hireDate">
              <el-date-picker
                v-model="formData.hireDate"
                type="date"
                placeholder="选择入职日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工状态" prop="status">
              <el-select v-model="formData.status" placeholder="请选择员工状态">
                <el-option label="在职" value="在职"></el-option>
                <el-option label="离职" value="离职"></el-option>
                <el-option label="休假" value="休假"></el-option>
                <el-option label="试用期" value="试用期"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合同开始日期" prop="contractStart">
              <el-date-picker
                v-model="formData.contractStart"
                type="date"
                placeholder="选择合同开始日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同结束日期" prop="contractEnd">
              <el-date-picker
                v-model="formData.contractEnd"
                type="date"
                placeholder="选择合同结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="薪资" prop="salary">
              <el-input
                v-model.number="formData.salary"
                type="number"
                placeholder="请输入薪资"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="直属上级" prop="supervisor">
              <el-input v-model="formData.supervisor" placeholder="请输入直属上级姓名"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item class="form-actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="submitForm">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';

const route = useRoute();
const router = useRouter();
const employeeForm = ref(null);
const isEditMode = ref(!!route.params.id);
const departments = ref([]);

const formData = reactive({
  name: '',
  employeeId: '',
  departmentId: '',
  position: '',
  email: '',
  phone: '',
  hireDate: '',
  status: '在职',
  contractStart: '',
  contractEnd: '',
  salary: null,
  supervisor: ''
});

const formRules = reactive({
  name: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  employeeId: [
    { required: true, message: '请输入工号', trigger: 'blur' },
    { min: 4, max: 10, message: '工号长度在 4 到 10 个字符', trigger: 'blur' }
  ],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  position: [{ required: true, message: '请输入职位', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  hireDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择员工状态', trigger: 'change' }],
  contractStart: [{ required: true, message: '请选择合同开始日期', trigger: 'change' }],
  contractEnd: [{ required: true, message: '请选择合同结束日期', trigger: 'change' }],
  salary: [
    { required: true, message: '请输入薪资', trigger: 'blur' },
    { type: 'number', message: '请输入有效的数字', trigger: 'blur' },
    { min: 3000, message: '薪资不能低于3000', trigger: 'blur' }
  ]
});

onMounted(() => {
  fetchDepartments();
  if (isEditMode.value) {
    fetchEmployeeDetail();
  } else {
    generateEmployeeId();
  }
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

// 获取员工详情（编辑模式）
const fetchEmployeeDetail = async () => {
  try {
    const response = await axios.get(`/api/employees/${route.params.id}`);
    const employeeData = response.data.data;
    // 填充表单数据
    for (const key in formData) {
      if (employeeData.hasOwnProperty(key)) {
        formData[key] = employeeData[key];
      }
    }
  } catch (error) {
    ElMessage.error('获取员工详情失败: ' + (error.response?.data?.message || error.message));
    router.push('/employees');
  }
};

// 生成默认工号（新增模式）
const generateEmployeeId = () => {
  const prefix = 'EMP';
  const date = new Date().getFullYear().toString().slice(-2);
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0');
  formData.employeeId = `${prefix}${date}${random}`;
};

// 提交表单
const submitForm = async () => {
  await employeeForm.value.validate();

  try {
    if (isEditMode.value) {
      // 编辑模式
      await axios.put(`/api/employees/${route.params.id}`, formData);
      ElMessage.success('员工信息更新成功');
    } else {
      // 新增模式
      await axios.post('/api/employees', formData);
      ElMessage.success('员工创建成功');
    }
    router.push('/employees');
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message));
  }
};

// 重置表单
const resetForm = () => {
  employeeForm.value.resetFields();
  if (!isEditMode.value) {
    generateEmployeeId();
  }
};

// 返回列表
const goBack = () => {
  router.go(-1);
};
</script>

<style scoped>
.form-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-container {
  padding: 20px 0;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
}
</style>