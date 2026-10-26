const express = require('express');

const router = express.Router();

router.get('/', (req, res) => {
  res.json({
    module: 'reimbursement',
    status: 'ok',
    message: 'Reimbursement module placeholder is ready for demo.'
  });
});

module.exports = router;
