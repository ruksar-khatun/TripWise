import { Router } from 'express';
import { z } from 'zod';
import type { CatalogService } from '../catalog/service.js';
import { Interest } from '../catalog/types.js';
import { validate } from '../http/validate.js';

const ListQuery = z.object({
  query: z.string().trim().max(80).optional(),
  interest: Interest.optional(),
  featured: z.enum(['true', 'false']).transform((v) => v === 'true').optional(),
  limit: z.coerce.number().int().min(1).max(100).default(50),
  offset: z.coerce.number().int().min(0).default(0),
});

/** Public, read-only catalog (no auth: browsing works before sign-in and is cacheable). */
export function destinationsRouter(catalog: CatalogService) {
  const router = Router();
  router.get('/', validate({ query: ListQuery }), async (_req, res) => {
    res.set('Cache-Control', 'public, max-age=300');
    res.json(await catalog.list(res.locals.valid.query));
  });
  router.get('/:id', validate({ params: z.object({ id: z.string().regex(/^[a-z0-9-]{1,64}$/) }) }), async (_req, res) => {
    res.set('Cache-Control', 'public, max-age=300');
    res.json(await catalog.get(res.locals.valid.params.id));
  });
  return router;
}
