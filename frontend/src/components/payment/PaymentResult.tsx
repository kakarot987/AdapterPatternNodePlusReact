import React, { useState } from 'react';
import type { PaymentResponse } from '../../types/payment.types';

interface Props {
  response: PaymentResponse;
}

/**
 * PaymentResult — displays the unified response.
 * Shows both the clean unified fields AND the raw provider
 * response, making the adapter's job visible for the demo.
 */
const PaymentResult: React.FC<Props> = ({ response }) => {
  const [showRaw, setShowRaw] = useState(false);

  const statusClass =
    response.status === 'SUCCESS'
      ? 'success'
      : response.status === 'FAILED'
      ? 'failed'
      : 'pending';

  return (
    <div className={`payment-result ${statusClass}`}>
      <div className="result-header">
        <span className="result-icon">{response.providerIcon}</span>
        <div>
          <h3>{response.statusLabel}</h3>
          <p className="result-provider" style={{ color: response.providerColor }}>
            via {response.providerLabel}
          </p>
        </div>
      </div>

      <div className="result-grid">
        <div className="result-field">
          <label>Transaction ID</label>
          <code>{response.transactionId}</code>
        </div>
        <div className="result-field">
          <label>Amount</label>
          <strong>{response.formattedAmount}</strong>
        </div>
        <div className="result-field">
          <label>Date</label>
          <span>{response.formattedDate}</span>
        </div>
        <div className="result-field">
          <label>Message</label>
          <span>{response.message}</span>
        </div>
      </div>

      {/* Toggle raw provider response — shows what the adapter translated FROM */}
      <button
        className="toggle-raw-btn"
        onClick={() => setShowRaw((v) => !v)}
      >
        {showRaw ? '▲ Hide' : '▼ Show'} raw {response.providerLabel} response
      </button>

      {showRaw && (
        <pre className="raw-response">
          {JSON.stringify(response.rawProviderResponse, null, 2)}
        </pre>
      )}
    </div>
  );
};

export default PaymentResult;
