const express = require('express');

const router = express.Router();

router.get('/', (req, res) => {
  res.json({
    module: 'budget',
    status: 'ok',
    message: 'Budget module placeholder is ready for demo.'
  });
});

module.exports = router;
