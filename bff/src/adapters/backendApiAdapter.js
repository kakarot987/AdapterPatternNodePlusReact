/**
 * ============================================================
 *  ADAPTER PATTERN IN NODE.JS — BackendApiAdapter
 * ============================================================
 * This is another layer of the Adapter Pattern:
 *
 *  Frontend           BFF (Node.js)           Spring Boot
 *  ────────    ────────────────────────    ─────────────────
 *  Speaks    → BackendApiAdapter adapts → Speaks its own API
 *  camelCase   (format, enrich, reshape)   language
 *
 * The BFF (Backend for Frontend) uses the Adapter Pattern to:
 *  ✔  Reshape Spring Boot responses for the UI
 *  ✔  Add UI-specific fields (icons, labels, colors)
 *  ✔  Aggregate multiple backend calls if needed
 *  ✔  Shield the frontend from backend API changes
 * ============================================================
 */
const axios = require('axios');

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

// Provider metadata — frontend display concerns live here, not in Java
const PROVIDER_META = {
  stripe: { label: 'Stripe', icon: '💳', color: '#635bff', currency: 'USD' },
  paypal: { label: 'PayPal', icon: '🅿️', color: '#003087', currency: 'USD' },
  razorpay: { label: 'Razorpay', icon: '🪙', color: '#3395ff', currency: 'INR' },
};

class BackendApiAdapter {
  /**
   * Adapts the unified backend payment call.
   * Enriches the response with UI metadata before returning.
   */
  async processPayment(requestBody) {
    const { data } = await axios.post(`${BACKEND_URL}/api/payments/process`, requestBody);
    return this._enrichResponse(data);
  }

  async refundPayment(provider, transactionId, amount) {
    const { data } = await axios.post(
      `${BACKEND_URL}/api/payments/${provider}/refund/${transactionId}`,
      null,
      { params: { amount } }
    );
    return this._enrichResponse(data);
  }

  async getPaymentStatus(provider, transactionId) {
    const { data } = await axios.get(
      `${BACKEND_URL}/api/payments/${provider}/status/${transactionId}`
    );
    return this._enrichResponse(data);
  }

  async getProviders() {
    const { data } = await axios.get(`${BACKEND_URL}/api/payments/providers`);

    // ADAPTATION: enrich the plain provider list with UI metadata
    return {
      ...data,
      providers: data.providers.map(name => ({
        id: name,
        ...PROVIDER_META[name],
      })),
    };
  }

  /**
   * ADAPTATION: take the raw Spring Boot response and add
   * display-friendly fields the frontend needs.
   */
  _enrichResponse(backendResponse) {
    const meta = PROVIDER_META[backendResponse.provider] || {};
    return {
      ...backendResponse,
      // UI-specific enrichments:
      providerLabel: meta.label || backendResponse.provider,
      providerIcon: meta.icon || '💰',
      providerColor: meta.color || '#888',
      statusLabel: this._formatStatus(backendResponse.status),
      formattedAmount: `${backendResponse.currency} ${Number(backendResponse.amount).toFixed(2)}`,
      formattedDate: new Date(backendResponse.timestamp).toLocaleString(),
    };
  }

  _formatStatus(status) {
    const labels = { SUCCESS: '✅ Success', FAILED: '❌ Failed', PENDING: '⏳ Pending' };
    return labels[status] || status;
  }
}

module.exports = new BackendApiAdapter();
