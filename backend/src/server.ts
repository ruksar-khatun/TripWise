import { loadEnv } from './config/env.js';
import { createLogger } from './config/logger.js';
import { createApp } from './app.js';

const env = loadEnv();
const logger = createLogger(env);
const app = createApp(env, logger);

const server = app.listen(env.PORT, () => {
  logger.info(`tripwise-backend listening on http://localhost:${env.PORT} (${env.NODE_ENV})`);
});

// Graceful shutdown (Cloud Run sends SIGTERM).
for (const signal of ['SIGTERM', 'SIGINT'] as const) {
  process.on(signal, () => {
    logger.info(`${signal} received, shutting down`);
    server.close(() => process.exit(0));
  });
}
