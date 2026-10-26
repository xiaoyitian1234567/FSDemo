const STORAGE_KEY = 'fsdemo-employees';

const seedEmployees = [
  {
    id: 1,
    employeeId: 'EMP24001',
    name: '陈晨',
    departmentId: 'tech',
    departmentName: '技术部',
    position: '前端工程师',
    email: 'chenchen@example.com',
    phone: '13800000001',
    hireDate: '2024-03-18',
    status: '在职',
    contractStart: '2024-03-18',
    contractEnd: '2027-03-17',
    salary: 18000,
    supervisor: '刘峰',
    performance: 'A',
    location: '上海',
    lastUpdated: '2026-10-20 10:32'
  },
  {
    id: 2,
    employeeId: 'EMP24002',
    name: '李萌',
    departmentId: 'product',
    departmentName: '产品部',
    position: '产品经理',
    email: 'limeng@example.com',
    phone: '13800000002',
    hireDate: '2023-11-08',
    status: '在职',
    contractStart: '2023-11-08',
    contractEnd: '2026-11-07',
    salary: 22000,
    supervisor: '周岩',
    performance: 'A',
    location: '北京',
    lastUpdated: '2026-10-18 14:10'
  },
  {
    id: 3,
    employeeId: 'EMP24003',
    name: '王楠',
    departmentId: 'hr',
    departmentName: '人力资源部',
    position: '招聘主管',
    email: 'wangnan@example.com',
    phone: '13800000003',
    hireDate: '2022-05-15',
    status: '在职',
    contractStart: '2022-05-15',
    contractEnd: '2025-05-14',
    salary: 16000,
    supervisor: '苏敏',
    performance: 'B+',
    location: '深圳',
    lastUpdated: '2026-10-16 09:20'
  },
  {
    id: 4,
    employeeId: 'EMP24004',
    name: '赵一凡',
    departmentId: 'finance',
    departmentName: '财务部',
    position: '财务分析师',
    email: 'zhaoyifan@example.com',
    phone: '13800000004',
    hireDate: '2024-01-10',
    status: '试用期',
    contractStart: '2024-01-10',
    contractEnd: '2027-01-09',
    salary: 14500,
    supervisor: '何静',
    performance: 'B',
    location: '杭州',
    lastUpdated: '2026-10-12 16:48'
  },
  {
    id: 5,
    employeeId: 'EMP24005',
    name: '孙悦',
    departmentId: 'sales',
    departmentName: '销售部',
    position: '客户经理',
    email: 'sunyue@example.com',
    phone: '13800000005',
    hireDate: '2021-09-06',
    status: '休假',
    contractStart: '2021-09-06',
    contractEnd: '2026-09-05',
    salary: 19000,
    supervisor: '唐杰',
    performance: 'A-',
    location: '广州',
    lastUpdated: '2026-10-08 11:05'
  },
  {
    id: 6,
    employeeId: 'EMP24006',
    name: '高宁',
    departmentId: 'ops',
    departmentName: '运营部',
    position: '运营专员',
    email: 'gaoning@example.com',
    phone: '13800000006',
    hireDate: '2024-06-22',
    status: '在职',
    contractStart: '2024-06-22',
    contractEnd: '2027-06-21',
    salary: 12500,
    supervisor: '马琳',
    performance: 'B+',
    location: '成都',
    lastUpdated: '2026-10-11 18:22'
  }
];

const departments = [
  { id: 'all', name: '全部部门' },
  { id: 'tech', name: '技术部' },
  { id: 'product', name: '产品部' },
  { id: 'hr', name: '人力资源部' },
  { id: 'finance', name: '财务部' },
  { id: 'sales', name: '销售部' },
  { id: 'ops', name: '运营部' }
];

function readEmployees() {
  const saved = localStorage.getItem(STORAGE_KEY);
  if (!saved) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(seedEmployees));
    return [...seedEmployees];
  }

  try {
    return JSON.parse(saved);
  } catch {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(seedEmployees));
    return [...seedEmployees];
  }
}

function writeEmployees(employees) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(employees));
}

function withDepartmentName(employee) {
  const department = departments.find((item) => item.id === employee.departmentId);
  return {
    ...employee,
    departmentName: department ? department.name : employee.departmentName || '未分配'
  };
}

export function resetEmployees() {
  writeEmployees(seedEmployees);
}

export function getDepartments() {
  return departments;
}

export function getEmployees({
  keyword = '',
  departmentId = 'all',
  page = 1,
  pageSize = 10
} = {}) {
  const normalizedKeyword = keyword.trim().toLowerCase();

  const filtered = readEmployees()
    .map(withDepartmentName)
    .filter((employee) => {
      const matchesKeyword =
        !normalizedKeyword ||
        employee.name.toLowerCase().includes(normalizedKeyword) ||
        employee.employeeId.toLowerCase().includes(normalizedKeyword) ||
        employee.position.toLowerCase().includes(normalizedKeyword);

      const matchesDepartment =
        departmentId === 'all' || employee.departmentId === departmentId;

      return matchesKeyword && matchesDepartment;
    })
    .sort((a, b) => b.id - a.id);

  const start = (page - 1) * pageSize;

  return {
    records: filtered.slice(start, start + pageSize),
    total: filtered.length
  };
}

export function getEmployeeById(id) {
  const employee = readEmployees().find((item) => String(item.id) === String(id));
  return employee ? withDepartmentName(employee) : null;
}

export function saveEmployee(payload, id) {
  const employees = readEmployees();
  const now = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-');
  const record = {
    ...payload,
    departmentName: departments.find((item) => item.id === payload.departmentId)?.name || '未分配',
    lastUpdated: now
  };

  if (id) {
    const index = employees.findIndex((item) => String(item.id) === String(id));
    if (index >= 0) {
      employees[index] = {
        ...employees[index],
        ...record
      };
    }
  } else {
    const nextId = employees.length ? Math.max(...employees.map((item) => item.id)) + 1 : 1;
    employees.push({
      ...record,
      id: nextId
    });
  }

  writeEmployees(employees);
}

export function deleteEmployeeById(id) {
  const employees = readEmployees().filter((item) => String(item.id) !== String(id));
  writeEmployees(employees);
}

export function getDashboardStats() {
  const employees = readEmployees().map(withDepartmentName);
  const activeCount = employees.filter((item) => item.status === '在职').length;
  const probationCount = employees.filter((item) => item.status === '试用期').length;
  const departmentStats = departments
    .filter((item) => item.id !== 'all')
    .map((department) => {
      const count = employees.filter((item) => item.departmentId === department.id).length;
      return {
        department: department.name,
        count,
        percentage: employees.length ? Number(((count / employees.length) * 100).toFixed(1)) : 0
      };
    })
    .sort((a, b) => b.count - a.count);

  return {
    totalEmployees: employees.length,
    activeCount,
    probationCount,
    totalDepartments: departments.length - 1,
    averageSalary: Math.round(
      employees.reduce((sum, item) => sum + Number(item.salary || 0), 0) / Math.max(employees.length, 1)
    ),
    departmentStats,
    recentActivities: [
      { id: 1, title: '完成演示版前端重构', time: '今天 10:30', type: 'success' },
      { id: 2, title: '新增员工详情页和表单页', time: '今天 09:45', type: 'primary' },
      { id: 3, title: '切换为本地 Mock 数据模式', time: '昨天 18:20', type: 'warning' }
    ]
  };
}
