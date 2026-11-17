import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import PaymentResult from './PaymentResult';
import type { PaymentResponse } from '../../types/payment.types';

const response: PaymentResponse = {
  transactionId: 'ch_demo_123',
  provider: 'stripe',
  status: 'SUCCESS',
  amount: 12.5,
  currency: 'USD',
  message: 'Simulated payment',
  timestamp: 123,
  rawProviderResponse: { status: 'succeeded', reference: 'demo' },
  providerLabel: 'Stripe',
  providerIcon: '💳',
  providerColor: '#635bff',
  statusLabel: '✅ Success',
  formattedAmount: 'USD 12.50',
  formattedDate: 'Today',
};

describe('PaymentResult', () => {
  it('shows normalized fields and toggles the raw provider response', async () => {
    const user = userEvent.setup();
    render(<PaymentResult response={response} />);

    expect(screen.getByText('ch_demo_123')).toBeInTheDocument();
    expect(screen.getByText('USD 12.50')).toBeInTheDocument();
    expect(screen.queryByText(/"reference": "demo"/)).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /show raw stripe response/i }));
    expect(screen.getByText(/"reference": "demo"/)).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /hide raw stripe response/i }));
    expect(screen.queryByText(/"reference": "demo"/)).not.toBeInTheDocument();
  });
});
