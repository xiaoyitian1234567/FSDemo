const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');
const mysql = require('mysql2/promise');
const redis = require('redis');
const winston = require('winston');
const path = require('path');
const fs = require('fs');

// 加载环境变量
dotenv.config();

// 创建日志目录
const logDir = path.join(__dirname, 'logs');
if (!fs.existsSync(logDir)) {
  fs.mkdirSync(logDir);
}

// 配置日志
const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.File({ filename: path.join(logDir, 'error.log'), level: 'error' }),
    new winston.transports.File({ filename: path.join(logDir, 'combined.log') })
  ]
});

// 开发环境下输出到控制台
if (process.env.NODE_ENV !== 'production') {
  logger.add(new winston.transports.Console({
    format: winston.format.combine(
      winston.format.colorize(),
      winston.format.simple()
    )
  }));
}

// 创建Express应用
const app = express();
const PORT = process.env.PORT || 3001;

// 中间件
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// 创建数据库连接池
let mysqlPool;
let redisClient;

// 初始化数据库连接
async function initializeDatabaseConnections() {
  try {
    // MySQL连接池
    mysqlPool = mysql.createPool({
      host: process.env.DB_HOST || 'localhost',
      port: process.env.DB_PORT || 3306,
      user: process.env.DB_USER || 'root',
      password: process.env.DB_PASSWORD || 'root',
      database: process.env.DB_NAME || 'feishu_finance',
      waitForConnections: true,
      connectionLimit: 10,
      queueLimit: 0
    });

    // 测试MySQL连接
    const [rows] = await mysqlPool.query('SELECT 1 + 1 AS result');
    logger.info('MySQL connected successfully');

    // Redis客户端
    redisClient = redis.createClient({
      url: `redis://${process.env.REDIS_HOST || 'localhost'}:${process.env.REDIS_PORT || 6379}`
    });

    redisClient.on('error', (err) => logger.error('Redis error:', err));
    await redisClient.connect();
    logger.info('Redis connected successfully');

  } catch (error) {
    logger.error('Database connection error:', error);
    process.exit(1);
  }
}

// 路由
app.use('/api/reimbursement', require('./routes/reimbursement'));
app.use('/api/budget', require('./routes/budget'));
app.use('/api/accounting', require('./routes/accounting'));
app.use('/api/investment', require('./routes/investment'));
app.use('/api/reconciliation', require('./routes/reconciliation'));
app.use('/api/reports', require('./routes/reports'));

// 健康检查接口
app.get('/health', async (req, res) => {
  try {
    // 检查MySQL连接
    const [rows] = await mysqlPool.query('SELECT 1 + 1 AS result');
    // 检查Redis连接
    await redisClient.ping();

    res.status(200).json({
      status: 'ok',
      timestamp: new Date(),
      services: {
        mysql: 'connected',
        redis: 'connected'
      }
    });
  } catch (error) {
    logger.error('Health check failed:', error);
    res.status(500).json({
      status: 'error',
      timestamp: new Date(),
      error: error.message
    });
  }
});

// 404处理
app.use((req, res) => {
  res.status(404).json({
    code: 404,
    message: 'Resource not found'
  });
});

// 错误处理中间件
app.use((err, req, res, next) => {
  logger.error('Server error:', {
    method: req.method,
    url: req.url,
    error: err.message,
    stack: err.stack
  });

  res.status(err.statusCode || 500).json({
    code: err.statusCode || 500,
    message: err.message || 'Internal server error',
    ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
  });
});

// 启动服务器
async function startServer() {
  try {
    await initializeDatabaseConnections();
    app.listen(PORT, () => {
      logger.info(`Finance service running on port ${PORT}`);
    });
  } catch (error) {
    logger.error('Failed to start server:', error);
    process.exit(1);
  }
}

// 优雅关闭
process.on('SIGINT', async () => {
  logger.info('Shutting down server...');
  if (mysqlPool) {
    await mysqlPool.end();
    logger.info('MySQL pool closed');
  }
  if (redisClient) {
    await redisClient.quit();
    logger.info('Redis client closed');
  }
  process.exit(0);
});

// 启动应用
startServer();

// 导出供测试使用
module.exports = app;