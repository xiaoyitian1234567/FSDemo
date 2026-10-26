# 项目结构梳理

## 一、仓库整体结构

项目根目录包含 3 个业务目录：

- `frontend`：前端项目，技术栈为 Vue 3 + Vite + Element Plus。
- `backend`：主后端项目，技术栈为 Spring Boot + MyBatis + MySQL + Redis，负责飞书集成相关核心能力。
- `finance-service`：独立的 Node.js 财务服务，技术栈为 Express + MySQL + Redis，当前更像补充型服务。

根目录还有：

- `README.md`：项目概述说明。
- `.gitignore`：Git 忽略规则。

## 二、前端结构

前端位于 `frontend`，入口与职责如下：

- `package.json`：前端依赖与启动脚本。
- `src/main.js`：应用入口，配置 Axios 默认请求地址与请求/响应拦截器。
- `src/router/index.js`：前端路由定义。
- `src/App.vue`：根组件。
- `src/components/`：页面级组件。

当前主要页面组件：

- `Dashboard.vue`：首页/仪表盘
- `EmployeeList.vue`：员工列表
- `EmployeeDetail.vue`：员工详情
- `EmployeeForm.vue`：员工新增/编辑
- `NotFound.vue`：兜底 404 页面

前端主要访问后端 `/api` 接口，默认通过 `VITE_APP_API_BASE_URL` 或 `/api` 进行请求转发。

## 三、后端结构

后端位于 `backend`，是主业务服务。

### 1. 启动入口

- `src/main/java/com/feishu/integration/Application.java`

### 2. 核心分层

- `config`：配置类，例如飞书客户端配置、RestTemplate 配置
- `controller`：接口层，目前可见员工相关控制器
- `service`：业务接口定义
- `service/impl`：业务实现
- `mapper`：MyBatis 数据访问层
- `entity`：实体类
- `dto`：数据传输对象
- `client` / `util`：飞书 API 调用封装、工具类
- `exception` / `constant`：异常与常量定义

### 3. 目前可见的核心业务

- `EmployeeController.java`：员工接口入口
- `EmployeeServiceImpl.java`：员工增删改查、飞书组织架构同步、考勤记录获取
- `PayrollServiceImpl.java`：薪资计算与飞书审批集成

### 4. 配置文件

- `src/main/resources/application.yml`
- `src/main/resources/application.properties`
- `src/main/resources/messages.properties`

后端是整个项目的主中台，飞书通讯录、审批、考勤等能力主要集中在这里。

## 四、财务服务结构

`finance-service` 是一个独立的 Node.js 服务。

### 1. 启动入口

- `index.js`

### 2. 已体现出来的职责

`index.js` 中声明了以下能力：

- MySQL 连接池
- Redis 连接
- 日志系统（Winston）
- 财务相关路由挂载：
  - `/api/reimbursement`
  - `/api/budget`
  - `/api/accounting`
  - `/api/investment`
  - `/api/reconciliation`
  - `/api/reports`

### 3. 当前状态判断

虽然目录中预留了 `controllers`、`services`、`models`、`routes` 等结构，但当前目录下未看到实际路由与业务文件，说明这一块可能还在搭骨架，或者项目文件并不完整。

## 五、三者关系

从当前目录看，项目更像下面这套分工：

1. `frontend` 负责页面展示和用户操作。
2. `backend` 负责员工、组织、考勤、审批等飞书核心集成能力，是主后端。
3. `finance-service` 计划承接更细分的财务能力，但当前完成度明显低于 `backend`。

## 六、当前更合理的理解

如果把这个项目按主次关系重新梳理，可以理解为：

- 主前端：`frontend`
- 主后端：`backend`
- 补充服务：`finance-service`

也就是说，这不是一个简单的“前端 + 后端”双项目结构，而是“前端 + 主后端 + 补充型财务服务”的三段式结构。
