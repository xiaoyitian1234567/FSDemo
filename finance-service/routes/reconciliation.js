const express = require('express');

const router = express.Router();

router.get('/', (req, res) => {
  res.json({
    module: 'reconciliation',
    status: 'ok',
    message: 'Reconciliation module placeholder is ready for demo.'
  });
});

module.exports = router;
