import { describe, expect, it, vi } from 'vitest';
import request from 'supertest';
import { createApp } from '../src/app.js';
import { loadEnv } from '../src/config/env.js';
import { createLogger } from '../src/config/logger.js';
import { CurrencyService } from '../src/currency/service.js';
import { openDatabase } from '../src/db/client.js';

const env = loadEnv({ NODE_ENV: 'test' });

describe('GET /api/v1/currency/rates', () => {
  it('returns live rates and caches them', async () => {
    const fetchMock = vi.fn(async () => new Response(JSON.stringify({ result: 'success', rates: { USD: 1, INR: 84.1 } })));
    const { db } = await openDatabase({ production: false });
    const app = createApp(env, createLogger(env), { db, currency: new CurrencyService(fetchMock as unknown as typeof fetch) });
    const res = await request(app).get('/api/v1/currency/rates');
    expect(res.body).toMatchObject({ base: 'USD', source: 'live', rates: { INR: 84.1 } });
    await request(app).get('/api/v1/currency/rates');
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it('falls back to bundled rates when the provider fails, and retries later', async () => {
    let now = 0;
    const fetchMock = vi.fn(async () => { throw new Error('offline'); });
    const svc = new CurrencyService(fetchMock as unknown as typeof fetch, () => now);
    const first = await svc.usdRates();
    expect(first.source).toBe('fallback');
    expect(first.rates.JPY).toBeGreaterThan(100);
    now += 11 * 60_000;
    await svc.usdRates();
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });
});
