import { pino } from 'pino';
import type { Env } from './env.js';

/** Structured JSON logs in production; pretty logs in development; silent in tests. */
export function createLogger(env: Env) {
  return pino({
    level: env.NODE_ENV === 'test' ? 'silent' : env.LOG_LEVEL,
    // Never log credentials or tokens.
    redact: ['req.headers.authorization', 'req.headers["x-firebase-appcheck"]', 'req.headers.cookie'],
    ...(env.NODE_ENV === 'development' ? { transport: { target: 'pino-pretty' } } : {}),
  });
}
