const axios = require('axios');
const backendApiAdapter = require('../src/adapters/backendApiAdapter');

jest.mock('axios');

describe('BackendApiAdapter', () => {
  const mockBackendResponse = {
    transactionId: 'ch_test123',
    status: 'SUCCESS',
    provider: 'stripe',
    amount: 99.99,
    currency: 'USD',
    message: 'Payment processed via Stripe',
    timestamp: 1700000000000,
    rawProviderResponse: {},
  };

  describe('processPayment', () => {
    it('should enrich backend response with UI metadata', async () => {
      axios.post.mockResolvedValue({ data: mockBackendResponse });

      const result = await backendApiAdapter.processPayment({
        orderId: 'o1', amount: 99.99, currency: 'USD',
        customerEmail: 'a@b.com', provider: 'stripe',
      });

      expect(result.providerLabel).toBe('Stripe');
      expect(result.providerIcon).toBe('💳');
      expect(result.providerColor).toBe('#635bff');
      expect(result.statusLabel).toBe('✅ Success');
      expect(result.formattedAmount).toBe('USD 99.99');
    });

    it('should pass the correct payload to the backend', async () => {
      axios.post.mockResolvedValue({ data: mockBackendResponse });

      const payload = {
        orderId: 'ord_001', amount: 50, currency: 'USD',
        customerEmail: 'x@y.com', provider: 'stripe',
      };
      await backendApiAdapter.processPayment(payload);

      expect(axios.post).toHaveBeenCalledWith(
        expect.stringContaining('/api/payments/process'),
        payload
      );
    });
  });

  describe('getProviders', () => {
    it('should enrich provider list with metadata', async () => {
      axios.get.mockResolvedValue({
        data: { providers: ['stripe', 'paypal', 'razorpay'], count: 3 },
      });

      const result = await backendApiAdapter.getProviders();

      expect(result.providers).toHaveLength(3);
      expect(result.providers[0]).toMatchObject({
        id: 'stripe', label: 'Stripe', icon: '💳',
      });
    });
  });
});
