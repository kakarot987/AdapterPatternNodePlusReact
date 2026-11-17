import React from 'react';
import PaymentForm from '../components/payment/PaymentForm';
import AdapterDiagram from '../components/ui/AdapterDiagram';

const HomePage: React.FC = () => (
  <main className="app-main">
    <header className="app-header">
      <h1>🔌 Adapter Design Pattern</h1>
      <p>
        One unified API — three different payment providers. Change the provider
        in the form below; the Adapter Pattern handles all the translation.
      </p>
    </header>

    <div className="app-layout">
      <section className="form-section">
        <PaymentForm />
      </section>
      <aside className="diagram-section">
        <AdapterDiagram />
      </aside>
    </div>
  </main>
);

export default HomePage;
