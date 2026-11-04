import { Router, type RequestHandler } from 'express';
import { z } from 'zod';
import type { AuthService } from '../auth/service.js';
import { validate } from '../http/validate.js';

const PatchMeBody = z
  .object({
    displayName: z.string().trim().min(1).max(60).optional(),
    homeCurrency: z.string().regex(/^[A-Z]{3}$/, 'Use a 3-letter ISO currency code').optional(),
  })
  .strict();

const DeleteMeBody = z.object({ password: z.string().min(1, 'Enter your password').max(128) });

/** Current-user endpoints. Mounted behind requireAuth. */
export function meRouter(auth: AuthService, requireAuth: RequestHandler) {
  const router = Router();
  router.use(requireAuth);

  router.get('/', async (_req, res) => {
    res.json(await auth.getUser(res.locals.userId));
  });

  router.patch('/', validate({ body: PatchMeBody }), async (_req, res) => {
    res.json(await auth.updateUser(res.locals.userId, res.locals.valid.body));
  });

  // Account deletion (Google Play requirement). Re-confirms the password.
  router.delete('/', validate({ body: DeleteMeBody }), async (_req, res) => {
    await auth.deleteAccount(res.locals.userId, res.locals.valid.body.password);
    res.status(204).end();
  });

  return router;
}
