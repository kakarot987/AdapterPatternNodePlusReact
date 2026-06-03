/**
 * Global error handler — normalises errors from axios (backend calls)
 * and any unexpected errors into a consistent JSON shape.
 */
module.exports = function errorHandler(err, req, res, next) {
  console.error('[BFF Error]', err.message);

  // Axios error — the Spring Boot backend returned an error response
  if (err.response) {
    return res.status(err.response.status).json({
      error: err.response.data?.error || 'Backend error',
      source: 'spring-boot-backend',
      status: err.response.status,
    });
  }

  // Axios network error — backend is unreachable
  if (err.request) {
    return res.status(503).json({
      error: 'Could not reach the backend service. Is Spring Boot running on port 8080?',
      source: 'network',
    });
  }

  // Generic application error
  res.status(500).json({
    error: err.message || 'Internal BFF error',
    source: 'bff',
  });
};
