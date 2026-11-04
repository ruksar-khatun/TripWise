import { Router } from 'express';
import type { CurrencyService } from '../currency/service.js';

/** Public: exchange rates are not sensitive and are cached server-side. */
export function currencyRouter(currency: CurrencyService) {
  const router = Router();
  router.get('/rates', async (_req, res) => {
    res.set('Cache-Control', 'public, max-age=3600');
    res.json(await currency.usdRates());
  });
  return router;
}
