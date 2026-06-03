import React from 'react';

/**
 * AdapterDiagram — renders the architecture of all three adapter layers
 * in the app so users can see the pattern in action as they use it.
 */
const AdapterDiagram: React.FC = () => (
  <div className="adapter-diagram">
    <h3>🔌 Adapter Pattern Layers</h3>
    <div className="diagram-flow">

      <div className="diagram-node frontend-node">
        <span className="node-label">React Component</span>
        <span className="node-desc">PaymentForm.tsx</span>
        <span className="node-sub">calls usePayment()</span>
      </div>

      <div className="diagram-arrow">
        <div className="arrow-label">Adapter ①</div>
        <div className="arrow-line">→</div>
        <div className="arrow-sub">PaymentApiAdapter.ts</div>
      </div>

      <div className="diagram-node bff-node">
        <span className="node-label">BFF (Node.js)</span>
        <span className="node-desc">Express + Adapter</span>
        <span className="node-sub">enriches + routes</span>
      </div>

      <div className="diagram-arrow">
        <div className="arrow-label">Adapter ②</div>
        <div className="arrow-line">→</div>
        <div className="arrow-sub">BackendApiAdapter.js</div>
      </div>

      <div className="diagram-node backend-node">
        <span className="node-label">Spring Boot</span>
        <span className="node-desc">PaymentPort (Target)</span>
        <span className="node-sub">unified API</span>
      </div>

      <div className="diagram-arrow">
        <div className="arrow-label">Adapter ③</div>
        <div className="arrow-line">→</div>
        <div className="arrow-sub">StripeAdapter /
PayPalAdapter /
RazorpayAdapter</div>
      </div>

      <div className="diagram-node provider-node">
        <span className="node-label">Payment SDKs</span>
        <span className="node-desc">Stripe / PayPal / Razorpay</span>
        <span className="node-sub">incompatible APIs</span>
      </div>
    </div>
  </div>
);

export default AdapterDiagram;
