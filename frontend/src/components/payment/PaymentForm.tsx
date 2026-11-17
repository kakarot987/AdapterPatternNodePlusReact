import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { usePayment, useProviders } from '../../hooks/usePayment';
import type { PaymentRequest } from '../../types/payment.types';
import PaymentResult from './PaymentResult';

/**
 * PaymentForm — purely presentational component.
 * Has zero knowledge of Stripe, PayPal, or Razorpay.
 * Uses usePayment() hook → adapter → BFF → Spring Boot.
 */
const PaymentForm: React.FC = () => {
  const { response, loading, error, processPayment, reset } = usePayment();
  const { providers, loading: providersLoading, error: providersError, fetchProviders } = useProviders();

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<PaymentRequest>({
    defaultValues: {
      currency: 'USD',
      provider: 'stripe',
      orderId: `order_${Date.now()}`,
    },
  });

  const selectedProvider = watch('provider');

  useEffect(() => {
    fetchProviders();
  }, []);

  const onSubmit = async (data: PaymentRequest) => {
    reset();
    await processPayment(data);
  };

  const selectedMeta = providers?.providers.find((p) => p.id === selectedProvider);

  return (
    <div className="payment-form-wrapper">
      <form onSubmit={handleSubmit(onSubmit)} className="payment-form">
        <h2>Process Payment</h2>

        {providersError && (
          <div className="error-banner" role="alert">
            Unable to load payment providers: {providersError}{' '}
            <button type="button" onClick={() => void fetchProviders()}>
              Retry providers
            </button>
          </div>
        )}

        {/* Provider Selector */}
        <div className="form-group">
          <label>Payment Provider</label>
          <div className="provider-pills">
            {providers?.providers.map((p) => (
              <label
                key={p.id}
                className={`provider-pill ${selectedProvider === p.id ? 'active' : ''}`}
                style={{ '--provider-color': p.color } as React.CSSProperties}
              >
                <input type="radio" value={p.id} {...register('provider')} />
                <span>{p.icon}</span>
                <span>{p.label}</span>
              </label>
            ))}
          </div>
        </div>

        {/* Order ID */}
        <div className="form-group">
          <label htmlFor="order-id">Order ID</label>
          <input
            id="order-id"
            {...register('orderId', { required: 'Order ID is required' })}
            placeholder="order_12345"
          />
          {errors.orderId && <span className="error">{errors.orderId.message}</span>}
        </div>

        {/* Amount + Currency */}
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="payment-amount">Amount</label>
            <input
              id="payment-amount"
              type="number"
              step="0.01"
              {...register('amount', {
                valueAsNumber: true,
                required: 'Amount is required',
                min: { value: 0.01, message: 'Must be > 0' },
              })}
              placeholder="99.99"
            />
            {errors.amount && <span className="error">{errors.amount.message}</span>}
          </div>
          <div className="form-group">
            <label htmlFor="payment-currency">Currency</label>
            <select id="payment-currency" {...register('currency')}>
              <option value="USD">USD</option>
              <option value="EUR">EUR</option>
              <option value="INR">INR</option>
              <option value="GBP">GBP</option>
            </select>
          </div>
        </div>

        {/* Email */}
        <div className="form-group">
          <label htmlFor="customer-email">Customer Email</label>
          <input
            id="customer-email"
            type="email"
            {...register('customerEmail', {
              required: 'Email is required',
              pattern: { value: /^\S+@\S+$/i, message: 'Invalid email' },
            })}
            placeholder="customer@example.com"
          />
          {errors.customerEmail && (
            <span className="error">{errors.customerEmail.message}</span>
          )}
        </div>

        {/* Description */}
        <div className="form-group">
          <label htmlFor="payment-description">Description</label>
          <input
            id="payment-description"
            {...register('description', { required: 'Description is required' })}
            placeholder="Premium subscription"
          />
          {errors.description && <span className="error">{errors.description.message}</span>}
        </div>

        <button
          type="submit"
          disabled={loading || providersLoading || Boolean(providersError)}
          style={{ backgroundColor: selectedMeta?.color }}
        >
          {loading
            ? 'Processing...'
            : `Pay with ${selectedMeta?.label || 'Provider'} ${selectedMeta?.icon || ''}`}
        </button>
      </form>

      {error && (
        <div className="error-banner" role="alert">
          ❌ {error}
        </div>
      )}

      {response && <PaymentResult response={response} />}
    </div>
  );
};

export default PaymentForm;
