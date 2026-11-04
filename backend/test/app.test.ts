import { describe, expect, it } from 'vitest';
import request from 'supertest';
import express from 'express';
import { z } from 'zod';
import { createApp } from '../src/app.js';
import { loadEnv } from '../src/config/env.js';
import { createLogger } from '../src/config/logger.js';
import { HttpError } from '../src/http/errors.js';
import { validate } from '../src/http/validate.js';
import { errorHandler } from '../src/middleware/errors.js';

import { openDatabase } from '../src/db/client.js';

const env = loadEnv({ NODE_ENV: 'test' });
const database = await openDatabase({ production: false });
const app = createApp(env, createLogger(env), { db: database.db });

describe('GET /api/v1/health', () => {
  it('returns service status and a request id', async () => {
    const res = await request(app).get('/api/v1/health');
    expect(res.status).toBe(200);
    expect(res.body).toMatchObject({ status: 'ok', service: 'tripwise-backend' });
    expect(typeof res.body.version).toBe('string');
    expect(res.headers['x-request-id']).toBeTruthy();
  });

  it('echoes a client-supplied request id', async () => {
    const res = await request(app).get('/api/v1/health').set('X-Request-Id', 'abc-123');
    expect(res.headers['x-request-id']).toBe('abc-123');
  });

  it('sets security headers and hides x-powered-by', async () => {
    const res = await request(app).get('/api/v1/health');
    expect(res.headers['x-content-type-options']).toBe('nosniff');
    expect(res.headers['x-powered-by']).toBeUndefined();
  });
});

describe('error envelope', () => {
  it('unknown routes → NOT_FOUND', async () => {
    const res = await request(app).get('/api/v1/nope');
    expect(res.status).toBe(404);
    expect(res.body).toEqual({ error: { code: 'NOT_FOUND', message: 'Route not found' } });
  });

  it('malformed JSON → VALIDATION_FAILED (400)', async () => {
    const res = await request(app).post('/api/v1/health').set('Content-Type', 'application/json').send('{bad');
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('VALIDATION_FAILED');
  });

  it('validate() reports per-field messages; HttpError and unexpected errors are enveloped', async () => {
    const t = express();
    t.use(express.json());
    t.post('/items', validate({ body: z.object({ name: z.string().min(1), qty: z.number().int() }) }), (_req, res) =>
      res.json(res.locals.valid.body),
    );
    t.get('/conflict', () => {
      throw HttpError.conflict(7);
    });
    t.get('/boom', () => {
      throw new Error('secret internals');
    });
    t.use(errorHandler);

    const bad = await request(t).post('/items').send({ name: '', qty: 1.5 });
    expect(bad.status).toBe(400);
    expect(bad.body.error.code).toBe('VALIDATION_FAILED');
    expect(Object.keys(bad.body.error.details).sort()).toEqual(['body.name', 'body.qty']);

    const ok = await request(t).post('/items').send({ name: 'Tea', qty: 2, extra: true });
    expect(ok.body).toEqual({ name: 'Tea', qty: 2 });

    const conflict = await request(t).get('/conflict');
    expect(conflict.status).toBe(409);
    expect(conflict.body.error).toMatchObject({ code: 'CONFLICT', serverVersion: 7 });

    const boom = await request(t).get('/boom');
    expect(boom.status).toBe(500);
    expect(boom.body).toEqual({ error: { code: 'INTERNAL', message: 'Internal server error' } });
  });
});

describe('loadEnv', () => {
  it('applies defaults and parses CORS list', () => {
    const e = loadEnv({ CORS_ORIGINS: 'https://a.com, https://b.com' });
    expect(e.PORT).toBe(3000);
    expect(e.CORS_ORIGINS).toEqual(['https://a.com', 'https://b.com']);
  });

  it('rejects an invalid port', () => {
    expect(() => loadEnv({ PORT: 'abc' })).toThrow(/Invalid environment/);
  });

  it('requires JWT_SECRET and DATABASE_URL in production', () => {
    expect(() => loadEnv({ NODE_ENV: 'production' })).toThrow(/JWT_SECRET/);
    expect(() => loadEnv({ NODE_ENV: 'production', JWT_SECRET: 'x'.repeat(32) })).toThrow(/DATABASE_URL/);
    expect(() => loadEnv({ JWT_SECRET: 'short' })).toThrow(/at least 32/);
  });
});
