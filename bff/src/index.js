require('dotenv').config();
const express = require('express');
const morgan = require('morgan');

const paymentRoutes = require('./routes/payment.routes');
const errorHandler = require('./middleware/errorHandler');

const app = express();
const PORT = process.env.PORT || 3000;

// ─── Middleware ────────────────────────────────────────────────────────────────
app.use(express.json());
app.use(morgan('dev'));

// ─── Routes ───────────────────────────────────────────────────────────────────
app.use('/api/payments', paymentRoutes);

app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'adapter-pattern-bff', timestamp: Date.now() });
});

// ─── Error Handler ────────────────────────────────────────────────────────────
app.use(errorHandler);

app.listen(PORT, () => {
  console.log(`\n🚀 BFF running on http://localhost:${PORT}`);
  console.log(`   Proxying to Spring Boot at ${process.env.BACKEND_URL || 'http://localhost:8080'}\n`);
});

module.exports = app;
