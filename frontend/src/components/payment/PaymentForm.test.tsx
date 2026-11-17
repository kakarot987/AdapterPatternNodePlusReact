import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import PaymentForm from './PaymentForm';
import type { PaymentResponse, ProvidersResponse } from '../../types/payment.types';

const mocked = vi.hoisted(() => ({
  getProviders: vi.fn(),
  processPayment: vi.fn(),
}));

vi.mock('../../adapters/paymentApiAdapter', () => ({
  paymentApiAdapter: {
    getProviders: mocked.getProviders,
    processPayment: mocked.processPayment,
  },
}));

const providers: ProvidersResponse = {
  count: 2,
  providers: [
    { id: 'stripe', label: 'Stripe', icon: '💳', color: '#635bff', currency: 'USD' },
    { id: 'paypal', label: 'PayPal', icon: '🅿️', color: '#003087', currency: 'USD' },
  ],
};

const response: PaymentResponse = {
  transactionId: 'ch_demo',
  status: 'SUCCESS',
  provider: 'stripe',
  amount: 10.25,
  currency: 'USD',
  message: 'Simulated payment',
  timestamp: 1000,
  rawProviderResponse: { status: 'succeeded' },
  providerLabel: 'Stripe',
  providerIcon: '💳',
  providerColor: '#635bff',
  statusLabel: '✅ Success',
  formattedAmount: 'USD 10.25',
  formattedDate: 'Today',
};

async function completeForm(user: ReturnType<typeof userEvent.setup>) {
  await user.clear(screen.getByLabelText('Order ID'));
  await user.type(screen.getByLabelText('Order ID'), 'demo-order');
  await user.type(screen.getByLabelText('Amount'), '10.25');
  await user.type(screen.getByLabelText('Customer Email'), 'buyer@example.com');
  await user.type(screen.getByLabelText('Description'), 'Demo order');
}

beforeEach(() => {
  mocked.getProviders.mockReset();
  mocked.processPayment.mockReset();
  mocked.getProviders.mockResolvedValue(providers);
  mocked.processPayment.mockResolvedValue(response);
});

describe('PaymentForm', () => {
  it('submits a validated unified request and shows its result', async () => {
    const user = userEvent.setup();
    render(<PaymentForm />);
    await screen.findByRole('button', { name: /pay with stripe/i });
    await completeForm(user);
    await user.click(screen.getByRole('button', { name: /pay with stripe/i }));

    await waitFor(() => expect(mocked.processPayment).toHaveBeenCalledTimes(1));
    expect(mocked.processPayment).toHaveBeenCalledWith({
      orderId: 'demo-order',
      amount: 10.25,
      currency: 'USD',
      customerEmail: 'buyer@example.com',
      description: 'Demo order',
      provider: 'stripe',
    });
    expect(await screen.findByText('ch_demo')).toBeInTheDocument();
  });

  it('rejects invalid fields without calling the payment API', async () => {
    const user = userEvent.setup();
    render(<PaymentForm />);
    await screen.findByRole('button', { name: /pay with stripe/i });
    await user.click(screen.getByRole('button', { name: /pay with stripe/i }));

    expect(await screen.findByText(/amount is required/i)).toBeInTheDocument();
    expect(mocked.processPayment).not.toHaveBeenCalled();
  });

  it('shows payment failures rather than a success receipt', async () => {
    mocked.processPayment.mockRejectedValue(new Error('Backend temporarily unavailable'));
    const user = userEvent.setup();
    render(<PaymentForm />);
    await screen.findByRole('button', { name: /pay with stripe/i });
    await completeForm(user);
    await user.click(screen.getByRole('button', { name: /pay with stripe/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Backend temporarily unavailable');
    expect(screen.queryByText('ch_demo')).not.toBeInTheDocument();
  });

  it('offers a retry when provider discovery fails', async () => {
    mocked.getProviders.mockRejectedValueOnce(new Error('Offline'));
    const user = userEvent.setup();
    render(<PaymentForm />);
    expect(await screen.findByRole('alert')).toHaveTextContent('Offline');
    expect(screen.getByRole('button', { name: /pay with provider/i })).toBeDisabled();

    await user.click(screen.getByRole('button', { name: /retry providers/i }));
    await waitFor(() => expect(mocked.getProviders).toHaveBeenCalledTimes(2));
    expect(await screen.findByRole('button', { name: /pay with stripe/i })).toBeEnabled();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });
});
