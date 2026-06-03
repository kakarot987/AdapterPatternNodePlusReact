import axios from 'axios';
import type {
  PaymentRequest,
  PaymentResponse,
  ProvidersResponse,
  PaymentProvider,
} from '../types/payment.types';

/**
 * ============================================================
 *  ADAPTER PATTERN IN REACT — PaymentApiAdapter
 * ============================================================
 * A third layer of the Adapter Pattern lives here in the
 * frontend. This adapter:
 *
 *  React Components         PaymentApiAdapter        BFF (Node.js)
 *  ─────────────────   ────────────────────────   ────────────────
 *  Call usePayment()  → adapts to HTTP contract → REST API calls
 *  Get typed results  ← normalises responses   ← JSON response
 *
 * Benefits for the frontend:
 *  ✔  Components never import axios directly
 *  ✔  All API shape knowledge is in ONE place
 *  ✔  If BFF URL or shape changes, fix ONE file
 *  ✔  Easy to mock in tests — swap adapter, not every component
 * ============================================================
 */

const api = axios.create({
  baseURL: process.env.REACT_APP_BFF_URL || 'http://localhost:3000',
  timeout: 10_000,
  headers: { 'Content-Type': 'application/json' },
});

// ─── Interceptors ─────────────────────────────────────────────────────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.error ||
      error.response?.data?.message ||
      error.message ||
      'Unknown error';
    return Promise.reject(new Error(message));
  }
);

// ─── Adapter Interface ────────────────────────────────────────────────────────
export interface IPaymentApiAdapter {
  processPayment(request: PaymentRequest): Promise<PaymentResponse>;
  refundPayment(provider: PaymentProvider, transactionId: string, amount: number): Promise<PaymentResponse>;
  getPaymentStatus(provider: PaymentProvider, transactionId: string): Promise<PaymentResponse>;
  getProviders(): Promise<ProvidersResponse>;
}

// ─── Concrete Adapter ─────────────────────────────────────────────────────────
class PaymentApiAdapter implements IPaymentApiAdapter {
  async processPayment(request: PaymentRequest): Promise<PaymentResponse> {
    const { data } = await api.post<PaymentResponse>('/api/payments/process', request);
    return data;
  }

  async refundPayment(
    provider: PaymentProvider,
    transactionId: string,
    amount: number
  ): Promise<PaymentResponse> {
    const { data } = await api.post<PaymentResponse>(
      `/api/payments/${provider}/refund/${transactionId}`,
      null,
      { params: { amount } }
    );
    return data;
  }

  async getPaymentStatus(
    provider: PaymentProvider,
    transactionId: string
  ): Promise<PaymentResponse> {
    const { data } = await api.get<PaymentResponse>(
      `/api/payments/${provider}/status/${transactionId}`
    );
    return data;
  }

  async getProviders(): Promise<ProvidersResponse> {
    const { data } = await api.get<ProvidersResponse>('/api/payments/providers');
    return data;
  }
}

// Export a singleton — swap this for a mock in tests
export const paymentApiAdapter: IPaymentApiAdapter = new PaymentApiAdapter();
