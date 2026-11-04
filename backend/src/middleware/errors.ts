import type { ErrorRequestHandler, RequestHandler } from 'express';
import { ErrorCode, HttpError, type ApiErrorBody } from '../http/errors.js';

export const notFound: RequestHandler = (_req, _res, next) => next(HttpError.notFound('Route not found'));

export const errorHandler: ErrorRequestHandler = (err, req, res, _next) => {
  if (err instanceof HttpError) {
    res.status(err.status).json(err.toBody());
    return;
  }
  // body-parser errors (malformed JSON, payload too large) carry a 4xx status.
  const status = typeof err?.status === 'number' && err.status >= 400 && err.status < 500 ? err.status : 500;
  if (status < 500) {
    const body: ApiErrorBody = { error: { code: ErrorCode.VALIDATION_FAILED, message: String(err.message ?? 'Bad request') } };
    res.status(status).json(body);
    return;
  }
  req.log?.error({ err }, 'Unhandled error');
  const body: ApiErrorBody = { error: { code: ErrorCode.INTERNAL, message: 'Internal server error' } };
  res.status(500).json(body);
};
