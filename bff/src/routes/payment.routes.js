const express = require('express');
const router = express.Router();
const { body, param, query, validationResult } = require('express-validator');
const backendApiAdapter = require('../adapters/backendApiAdapter');

// ─── Validation middleware ────────────────────────────────────────────────────
const validate = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }
  next();
};

/**
 * POST /api/payments/process
 */
router.post(
  '/process',
  [
    body('orderId').isString().trim().notEmpty().withMessage('orderId is required'),
    body('amount').isFloat({ gt: 0 }).withMessage('amount must be a positive number')
      .bail().custom(value => /^\d+(?:\.\d{1,2})?$/.test(String(value)))
      .withMessage('amount must have at most two decimal places'),
    body('currency').isString().trim().notEmpty().withMessage('currency is required'),
    body('description').isString().trim().notEmpty().withMessage('description is required'),
    body('customerEmail').isEmail().withMessage('valid customerEmail is required'),
    body('provider')
      .notEmpty()
      .isIn(['stripe', 'paypal', 'razorpay'])
      .withMessage('provider must be stripe, paypal, or razorpay'),
  ],
  validate,
  async (req, res, next) => {
    try {
      const result = await backendApiAdapter.processPayment(req.body);
      res.json(result);
    } catch (err) {
      next(err);
    }
  }
);

/**
 * POST /api/payments/:provider/refund/:transactionId?amount=X
 */
router.post(
  '/:provider/refund/:transactionId',
  [
    param('provider').isIn(['stripe', 'paypal', 'razorpay']),
    param('transactionId').notEmpty(),
    query('amount').isFloat({ gt: 0 }).withMessage('amount must be a positive number')
      .bail().custom(value => /^\d+(?:\.\d{1,2})?$/.test(String(value)))
      .withMessage('amount must have at most two decimal places'),
  ],
  validate,
  async (req, res, next) => {
    try {
      const { provider, transactionId } = req.params;
      const { amount } = req.query;
      const result = await backendApiAdapter.refundPayment(provider, transactionId, amount);
      res.json(result);
    } catch (err) {
      next(err);
    }
  }
);

/**
 * GET /api/payments/:provider/status/:transactionId
 */
router.get(
  '/:provider/status/:transactionId',
  [
    param('provider').isIn(['stripe', 'paypal', 'razorpay']),
    param('transactionId').notEmpty(),
  ],
  validate,
  async (req, res, next) => {
    try {
      const { provider, transactionId } = req.params;
      const result = await backendApiAdapter.getPaymentStatus(provider, transactionId);
      res.json(result);
    } catch (err) {
      next(err);
    }
  }
);

/**
 * GET /api/payments/providers
 */
router.get('/providers', async (req, res, next) => {
  try {
    const result = await backendApiAdapter.getProviders();
    res.json(result);
  } catch (err) {
    next(err);
  }
});

module.exports = router;
