import { describe, expect, it } from 'vitest';
import request from 'supertest';
import { createApp } from '../src/app.js';
import { loadEnv } from '../src/config/env.js';

const app = createApp(loadEnv({ NODE_ENV: 'test' }));

describe('GET /api/health', () => {
  it('returns service status', async () => {
    const res = await request(app).get('/api/health');
    expect(res.status).toBe(200);
    expect(res.body).toMatchObject({ status: 'ok', service: 'tripwise-backend' });
    expect(typeof res.body.version).toBe('string');
  });

  it('returns JSON 404 for unknown routes', async () => {
    const res = await request(app).get('/api/nope');
    expect(res.status).toBe(404);
    expect(res.body.error.code).toBe('NOT_FOUND');
  });
});

describe('loadEnv', () => {
  it('applies defaults and parses CORS list', () => {
    const env = loadEnv({ CORS_ORIGINS: 'https://a.com, https://b.com' });
    expect(env.PORT).toBe(3000);
    expect(env.CORS_ORIGINS).toEqual(['https://a.com', 'https://b.com']);
  });

  it('rejects an invalid port', () => {
    expect(() => loadEnv({ PORT: 'abc' })).toThrow(/Invalid environment/);
  });
});
