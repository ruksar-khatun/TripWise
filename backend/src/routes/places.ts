import { Router, type RequestHandler } from 'express';
import { z } from 'zod';
import type { PlacesService } from '../places/service.js';
import { validate } from '../http/validate.js';

const NearbyQuery = z.object({
  lat: z.coerce.number().min(-90).max(90),
  lng: z.coerce.number().min(-180).max(180),
  radius: z.coerce.number().int().min(200).max(20_000).default(5_000),
});

/** Signed-in users only: every call costs Places quota. */
export function placesRouter(places: PlacesService, requireAuth: RequestHandler) {
  const router = Router();
  router.use(requireAuth);
  router.get('/nearby', validate({ query: NearbyQuery }), async (_req, res) => {
    const { lat, lng, radius } = res.locals.valid.query;
    res.json({ places: await places.nearby(lat, lng, radius) });
  });
  return router;
}
