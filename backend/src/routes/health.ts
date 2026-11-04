import { Router } from 'express';
import pkg from '../../package.json' with { type: 'json' };

export const healthRouter = Router();

healthRouter.get('/', (_req, res) => {
  res.json({ status: 'ok', service: 'tripwise-backend', version: pkg.version });
});
