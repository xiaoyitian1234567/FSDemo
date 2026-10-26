import { createRouter, createWebHashHistory } from 'vue-router';
import EmployeeList from '../components/EmployeeList.vue';
import EmployeeDetail from '../components/EmployeeDetail.vue';
import EmployeeForm from '../components/EmployeeForm.vue';
import Dashboard from '../components/Dashboard.vue';
import NotFound from '../components/NotFound.vue';

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard
  },
  {
    path: '/employees',
    name: 'EmployeeList',
    component: EmployeeList
  },
  {
    path: '/employee/:id',
    name: 'EmployeeDetail',
    component: EmployeeDetail,
    props: true
  },
  {
    path: '/employee/create',
    name: 'CreateEmployee',
    component: EmployeeForm
  },
  {
    path: '/employee/edit/:id',
    name: 'EditEmployee',
    component: EmployeeForm,
    props: true
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

export default router;