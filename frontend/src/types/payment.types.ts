// ─── Domain Types ────────────────────────────────────────────────────────────

export type PaymentProvider = 'stripe' | 'paypal' | 'razorpay';
export type PaymentStatus = 'SUCCESS' | 'FAILED' | 'PENDING' | 'UNKNOWN';

/**
 * Unified PaymentRequest — what the app sends regardless of provider.
 * This mirrors the Java PaymentRequest model.
 */
export interface PaymentRequest {
  orderId: string;
  amount: number;
  currency: string;
  customerEmail: string;
  description: string;
  provider: PaymentProvider;
}

/**
 * Enriched PaymentResponse — the BFF has added UI-friendly fields
 * on top of the raw Spring Boot response.
 */
export interface PaymentResponse {
  transactionId: string;
  status: PaymentStatus;
  provider: PaymentProvider;
  amount: number;
  currency: string;
  message: string;
  timestamp: number;
  rawProviderResponse: Record<string, unknown>;
  // BFF-added enrichments:
  providerLabel: string;
  providerIcon: string;
  providerColor: string;
  statusLabel: string;
  formattedAmount: string;
  formattedDate: string;
}

export interface ProviderInfo {
  id: PaymentProvider;
  label: string;
  icon: string;
  color: string;
  currency: string;
}

export interface ProvidersResponse {
  providers: ProviderInfo[];
  count: number;
}
