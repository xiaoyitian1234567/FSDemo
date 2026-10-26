const express = require('express');

const router = express.Router();

router.get('/', (req, res) => {
  res.json({
    module: 'reports',
    status: 'ok',
    message: 'Reports module placeholder is ready for demo.'
  });
});

module.exports = router;
