const express = require('express');
const supertest = require('supertest');
const adapter = require('../src/adapters/backendApiAdapter');

jest.mock('../src/adapters/backendApiAdapter');

const app = express();
app.use(express.json());
app.use('/api/payments', require('../src/routes/payment.routes'));
app.use(require('../src/middleware/errorHandler'));

const valid = {
  orderId: 'order-1', amount: 12.5, currency: 'USD',
  customerEmail: 'buyer@example.com', description: 'Demo purchase', provider: 'stripe',
};

beforeEach(() => jest.clearAllMocks());

describe('Payment BFF HTTP contract', () => {
  test('processes a valid request through the adapter', async () => {
    adapter.processPayment.mockResolvedValue({ status: 'SUCCESS', provider: 'stripe' });
    const res = await supertest(app).post('/api/payments/process').send(valid);
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('SUCCESS');
    expect(adapter.processPayment).toHaveBeenCalledWith(valid);
  });

  test.each([
    { ...valid, amount: 1.005 },
    { ...valid, amount: 0 },
    { ...valid, description: '' },
    { ...valid, provider: 'nonexistent' },
    { ...valid, customerEmail: 'invalid-email' },
  ])('rejects invalid process payload without a backend call: %j', async payload => {
    const res = await supertest(app).post('/api/payments/process').send(payload);
    expect(res.status).toBe(400);
    expect(res.body.errors.length).toBeGreaterThan(0);
    expect(adapter.processPayment).not.toHaveBeenCalled();
  });

  test('rejects a refund amount with more than two decimals', async () => {
    const res = await supertest(app)
      .post('/api/payments/stripe/refund/tx-1?amount=1.005');
    expect(res.status).toBe(400);
    expect(adapter.refundPayment).not.toHaveBeenCalled();
  });

  test('propagates backend validation failures with a stable status code', async () => {
    const err = new Error('Backend rejected request');
    err.response = { status: 400, data: { error: 'Payment amount is invalid' } };
    adapter.processPayment.mockRejectedValue(err);

    const res = await supertest(app).post('/api/payments/process').send(valid);
    expect(res.status).toBe(400);
    expect(res.body).toMatchObject({ error: 'Payment amount is invalid', source: 'spring-boot-backend' });
  });

  test('returns 503 when the backend is unreachable', async () => {
    const err = new Error('Connection refused');
    err.request = {};
    adapter.getProviders.mockRejectedValue(err);

    const res = await supertest(app).get('/api/payments/providers');
    expect(res.status).toBe(503);
    expect(res.body.source).toBe('network');
  });
});
