import { Router } from 'express';
import { rateLimit } from 'express-rate-limit';
import { z } from 'zod';
import type { AuthService } from '../auth/service.js';
import { ErrorCode, type ApiErrorBody } from '../http/errors.js';
import { validate } from '../http/validate.js';

const email = z.string().trim().email('Enter a valid email address').max(254);
// Length-based policy (NIST SP 800-63B): at least 8 characters, no composition rules.
const password = z.string().min(8, 'Use at least 8 characters').max(128, 'Use at most 128 characters');

export const RegisterBody = z.object({
  email,
  password,
  displayName: z.string().trim().min(1, 'Enter your name').max(60),
});
export const LoginBody = z.object({ email, password: z.string().min(1, 'Enter your password').max(128) });
const RefreshBody = z.object({ refreshToken: z.string().min(20).max(200) });

export function authRouter(auth: AuthService, opts: { strictLimit: boolean }) {
  const router = Router();
  // Brute-force protection on credential endpoints (per IP).
  const credentialLimiter = rateLimit({
    windowMs: 15 * 60_000,
    limit: opts.strictLimit ? 20 : 1_000,
    standardHeaders: 'draft-8',
    legacyHeaders: false,
    handler: (_req, res) => {
      const body: ApiErrorBody = { error: { code: ErrorCode.RATE_LIMITED, message: 'Too many attempts. Try again later.' } };
      res.status(429).json(body);
    },
  });

  router.post('/register', credentialLimiter, validate({ body: RegisterBody }), async (_req, res) => {
    res.status(201).json(await auth.register(res.locals.valid.body));
  });

  router.post('/login', credentialLimiter, validate({ body: LoginBody }), async (_req, res) => {
    res.json(await auth.login(res.locals.valid.body));
  });

  router.post('/refresh', validate({ body: RefreshBody }), async (_req, res) => {
    res.json(await auth.refresh(res.locals.valid.body.refreshToken));
  });

  router.post('/logout', validate({ body: RefreshBody }), async (_req, res) => {
    await auth.logout(res.locals.valid.body.refreshToken);
    res.status(204).end();
  });

  return router;
}
