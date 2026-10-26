const express = require('express');

const router = express.Router();

router.get('/', (req, res) => {
  res.json({
    module: 'investment',
    status: 'ok',
    message: 'Investment module placeholder is ready for demo.'
  });
});

module.exports = router;
