import type { RequestHandler } from 'express';
import type { z } from 'zod';
import { HttpError } from './errors.js';

type Schemas = { body?: z.ZodType; query?: z.ZodType; params?: z.ZodType };

/**
 * Validates request parts with zod. Parsed values are stored on `res.locals.valid`
 * (Express 5 makes `req.query` read-only), and failures become VALIDATION_FAILED with per-field messages.
 */
export function validate(schemas: Schemas): RequestHandler {
  return (req, res, next) => {
    const details: Record<string, string> = {};
    const valid: Record<string, unknown> = {};
    for (const part of ['params', 'query', 'body'] as const) {
      const schema = schemas[part];
      if (!schema) continue;
      const result = schema.safeParse(req[part]);
      if (result.success) {
        valid[part] = result.data;
      } else {
        for (const issue of result.error.issues) {
          const key = [part, ...issue.path].join('.');
          details[key] ??= issue.message;
        }
      }
    }
    if (Object.keys(details).length > 0) return next(HttpError.validation(details));
    res.locals.valid = valid;
    next();
  };
}
