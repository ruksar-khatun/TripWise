import type { RequestHandler } from 'express';
import { HttpError } from '../http/errors.js';
import { verifyAccessToken, type TokenConfig } from './tokens.js';

/** Requires `Authorization: Bearer <access token>`; sets `res.locals.userId`. */
export function requireAuth(tokens: TokenConfig): RequestHandler {
  return async (req, res, next) => {
    const header = req.headers.authorization ?? '';
    const match = /^Bearer (.+)$/i.exec(header);
    const userId = match ? await verifyAccessToken(match[1]!, tokens) : null;
    if (!userId) return next(HttpError.unauthorized());
    res.locals.userId = userId;
    next();
  };
}
