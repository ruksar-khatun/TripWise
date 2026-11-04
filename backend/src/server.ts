import { loadEnv } from './config/env.js';
import { createLogger } from './config/logger.js';
import { openDatabase } from './db/client.js';
import { createApp } from './app.js';

const env = loadEnv();
const logger = createLogger(env);
const database = await openDatabase(
  { databaseUrl: env.DATABASE_URL, pgliteDir: env.PGLITE_DIR, production: env.NODE_ENV === 'production' },
  logger,
);
if (!env.JWT_SECRET) logger.warn('JWT_SECRET not set: using a random key, sessions end when the server restarts.');

const app = createApp(env, logger, { db: database.db });
const server = app.listen(env.PORT, () => {
  logger.info(`tripwise-backend listening on http://localhost:${env.PORT} (${env.NODE_ENV})`);
});

// Graceful shutdown (Cloud Run sends SIGTERM).
for (const signal of ['SIGTERM', 'SIGINT'] as const) {
  process.on(signal, () => {
    logger.info(`${signal} received, shutting down`);
    server.close(() => void database.close().finally(() => process.exit(0)));
  });
}
