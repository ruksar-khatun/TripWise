import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import { rateLimit } from 'express-rate-limit';
import { pinoHttp } from 'pino-http';
import { randomUUID } from 'node:crypto';
import type { Logger } from 'pino';
import type { Env } from './config/env.js';
import { ErrorCode, type ApiErrorBody } from './http/errors.js';
import { healthRouter } from './routes/health.js';
import { errorHandler, notFound } from './middleware/errors.js';

export function createApp(env: Env, logger: Logger) {
  const app = express();

  app.disable('x-powered-by');
  app.set('trust proxy', 1); // behind one proxy hop (e.g. Cloud Run) so rate limiting sees the client IP
  app.use(
    pinoHttp({
      logger,
      genReqId: (req, res) => {
        const id = (req.headers['x-request-id'] as string | undefined) ?? randomUUID();
        res.setHeader('X-Request-Id', id);
        return id;
      },
    }),
  );
  app.use(helmet());
  app.use(cors({ origin: env.CORS_ORIGINS.length ? env.CORS_ORIGINS : false }));
  app.use(express.json({ limit: '1mb' }));

  const v1 = express.Router();
  v1.use(
    rateLimit({
      windowMs: 60_000,
      limit: 120,
      standardHeaders: 'draft-8',
      legacyHeaders: false,
      handler: (_req, res) => {
        const body: ApiErrorBody = { error: { code: ErrorCode.RATE_LIMITED, message: 'Too many requests' } };
        res.status(429).json(body);
      },
    }),
  );
  v1.use('/health', healthRouter);
  // Feature routers are mounted on v1 as their phases are approved:
  //   auth middleware → /me, /destinations, /trips, /expenses, /sync, /ai, /currency

  app.use('/api/v1', v1);
  app.use(notFound);
  app.use(errorHandler);
  return app;
}
