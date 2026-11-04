import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import { rateLimit } from 'express-rate-limit';
import { pinoHttp } from 'pino-http';
import { randomUUID } from 'node:crypto';
import type { Logger } from 'pino';
import type { Env } from './config/env.js';
import type { Database } from './db/client.js';
import { AuthService } from './auth/service.js';
import { requireAuth } from './auth/middleware.js';
import { toSecret, type TokenConfig } from './auth/tokens.js';
import { randomBytes } from 'node:crypto';
import { authRouter } from './routes/auth.js';
import { meRouter } from './routes/me.js';
import { CatalogService } from './catalog/service.js';
import { destinationsRouter } from './routes/destinations.js';
import { PlacesService } from './places/service.js';
import { placesRouter } from './routes/places.js';
import { ErrorCode, type ApiErrorBody } from './http/errors.js';
import { healthRouter } from './routes/health.js';
import { errorHandler, notFound } from './middleware/errors.js';

export interface AppDeps {
  db: Database;
  places?: PlacesService;
  /** Injectable clock for tests. */
  now?: () => Date;
}

export function createApp(env: Env, logger: Logger, deps: AppDeps) {
  const app = express();

  // Dev/test without JWT_SECRET: a random per-process key (tokens stop working on restart).
  const tokenConfig: TokenConfig = {
    secret: toSecret(env.JWT_SECRET ?? randomBytes(32).toString('hex')),
    accessTtlSeconds: env.ACCESS_TOKEN_TTL_SECONDS,
    refreshTtlDays: env.REFRESH_TOKEN_TTL_DAYS,
  };
  const auth = new AuthService(deps.db, tokenConfig, deps.now);

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
  v1.use('/auth', authRouter(auth, { strictLimit: env.NODE_ENV !== 'test' }));
  v1.use('/me', meRouter(auth, requireAuth(tokenConfig)));
  v1.use('/destinations', destinationsRouter(new CatalogService(deps.db)));
  v1.use('/places', placesRouter(deps.places ?? new PlacesService(env.PLACES_API_KEY), requireAuth(tokenConfig)));
  // Next phases mount here (all behind requireAuth): /trips, /saved, /expenses, /sync, /ai, /destinations

  app.use('/api/v1', v1);
  app.use(notFound);
  app.use(errorHandler);
  return app;
}
