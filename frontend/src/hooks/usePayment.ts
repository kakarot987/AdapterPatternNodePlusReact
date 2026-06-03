import { useState } from 'react';
import { paymentApiAdapter } from '../adapters/paymentApiAdapter.ts';
import type {
  PaymentRequest,
  PaymentResponse,
  PaymentProvider,
  ProvidersResponse,
} from '../types/payment.types';

interface UsePaymentState {
  response: PaymentResponse | null;
  loading: boolean;
  error: string | null;
}

/**
 * Custom hook — encapsulates payment operations.
 * Components call this hook; they never touch the adapter directly.
 * This is a further layer of abstraction: UI state + adapter calls
 * are co-located here, keeping components purely presentational.
 */
export function usePayment() {
  const [state, setState] = useState<UsePaymentState>({
    response: null,
    loading: false,
    error: null,
  });

  const execute = async <T>(fn: () => Promise<T>): Promise<T | null> => {
    setState((s) => ({ ...s, loading: true, error: null }));
    try {
      const result = await fn();
      setState((s) => ({ ...s, loading: false }));
      return result;
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Something went wrong';
      setState((s) => ({ ...s, loading: false, error: message }));
      return null;
    }
  };

  const processPayment = async (request: PaymentRequest) => {
    const result = await execute(() => paymentApiAdapter.processPayment(request));
    if (result) setState((s) => ({ ...s, response: result }));
    return result;
  };

  const refundPayment = async (
    provider: PaymentProvider,
    transactionId: string,
    amount: number
  ) => {
    const result = await execute(() =>
      paymentApiAdapter.refundPayment(provider, transactionId, amount)
    );
    if (result) setState((s) => ({ ...s, response: result }));
    return result;
  };

  const getStatus = async (provider: PaymentProvider, transactionId: string) => {
    const result = await execute(() =>
      paymentApiAdapter.getPaymentStatus(provider, transactionId)
    );
    if (result) setState((s) => ({ ...s, response: result }));
    return result;
  };

  const reset = () => setState({ response: null, loading: false, error: null });

  return {
    ...state,
    processPayment,
    refundPayment,
    getStatus,
    reset,
  };
}

/**
 * Hook for fetching the list of available providers.
 */
export function useProviders() {
  const [providers, setProviders] = useState<ProvidersResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchProviders = async () => {
    setLoading(true);
    try {
      const data = await paymentApiAdapter.getProviders();
      setProviders(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to fetch providers');
    } finally {
      setLoading(false);
    }
  };

  return { providers, loading, error, fetchProviders };
}
