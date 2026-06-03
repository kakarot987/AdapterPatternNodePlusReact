import type { IPaymentApiAdapter } from '../adapters/paymentApiAdapter';
import type {
  PaymentRequest,
  PaymentResponse,
  ProvidersResponse,
  PaymentProvider,
} from '../types/payment.types';

/**
 * Mock adapter for tests — swap this in place of the real adapter
 * to unit test components/hooks without hitting any network.
 *
 * This is a direct benefit of the Adapter Pattern:
 * because all API calls go through an interface, testing
 * is just a matter of swapping the implementation.
 */
export const mockPaymentApiAdapter: IPaymentApiAdapter = {
  async processPayment(request: PaymentRequest): Promise<PaymentResponse> {
    return {
      transactionId: 'test_txn_001',
      status: 'SUCCESS',
      provider: request.provider,
      amount: request.amount,
      currency: request.currency,
      message: `Mock payment via ${request.provider}`,
      timestamp: Date.now(),
      rawProviderResponse: { mock: true },
      providerLabel: request.provider,
      providerIcon: '💳',
      providerColor: '#6366f1',
      statusLabel: '✅ Success',
      formattedAmount: `${request.currency} ${request.amount.toFixed(2)}`,
      formattedDate: new Date().toLocaleString(),
    };
  },

  async refundPayment(
    provider: PaymentProvider,
    transactionId: string,
    amount: number
  ): Promise<PaymentResponse> {
    return {
      transactionId: `refund_${transactionId}`,
      status: 'SUCCESS',
      provider,
      amount,
      currency: 'USD',
      message: `Mock refund via ${provider}`,
      timestamp: Date.now(),
      rawProviderResponse: { mock: true },
      providerLabel: provider,
      providerIcon: '💳',
      providerColor: '#6366f1',
      statusLabel: '✅ Success',
      formattedAmount: `USD ${amount.toFixed(2)}`,
      formattedDate: new Date().toLocaleString(),
    };
  },

  async getPaymentStatus(
    provider: PaymentProvider,
    transactionId: string
  ): Promise<PaymentResponse> {
    return {
      transactionId,
      status: 'SUCCESS',
      provider,
      amount: 0,
      currency: 'USD',
      message: 'Mock status check',
      timestamp: Date.now(),
      rawProviderResponse: {},
      providerLabel: provider,
      providerIcon: '💳',
      providerColor: '#6366f1',
      statusLabel: '✅ Success',
      formattedAmount: 'USD 0.00',
      formattedDate: new Date().toLocaleString(),
    };
  },

  async getProviders(): Promise<ProvidersResponse> {
    return {
      count: 3,
      providers: [
        { id: 'stripe',   label: 'Stripe',   icon: '💳', color: '#635bff', currency: 'USD' },
        { id: 'paypal',   label: 'PayPal',   icon: '🅿️', color: '#003087', currency: 'USD' },
        { id: 'razorpay', label: 'Razorpay', icon: '🪙', color: '#3395ff', currency: 'INR' },
      ],
    };
  },
};
