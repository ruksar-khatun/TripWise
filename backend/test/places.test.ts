import { describe, expect, it, vi } from 'vitest';
import request from 'supertest';
import { createApp } from '../src/app.js';
import { loadEnv } from '../src/config/env.js';
import { createLogger } from '../src/config/logger.js';
import { openDatabase } from '../src/db/client.js';
import { PlacesService } from '../src/places/service.js';

const env = loadEnv({ NODE_ENV: 'test', JWT_SECRET: 'x'.repeat(40) });

async function appWith(places: PlacesService) {
  const { db } = await openDatabase({ production: false });
  const app = createApp(env, createLogger(env), { db, places });
  const reg = await request(app).post('/api/v1/auth/register').send({ email: 'p@x.io', password: 'password123', displayName: 'P' });
  return { app, auth: { Authorization: `Bearer ${reg.body.tokens.accessToken}` } };
}

const okResponse = {
  places: [
    { id: 'p1', displayName: { text: 'Kinkaku-ji' }, location: { latitude: 35.03, longitude: 135.72 }, rating: 4.6,
      userRatingCount: 50000, primaryTypeDisplayName: { text: 'Buddhist temple' }, shortFormattedAddress: 'Kyoto', googleMapsUri: 'https://maps.google.com/?cid=1' },
    { id: 'broken' }, // incomplete records are dropped
  ],
};

describe('GET /api/v1/places/nearby', () => {
  it('requires auth and a configured key', async () => {
    const { app, auth } = await appWith(new PlacesService(undefined));
    expect((await request(app).get('/api/v1/places/nearby?lat=35&lng=135')).status).toBe(401);
    const res = await request(app).get('/api/v1/places/nearby?lat=35&lng=135').set(auth);
    expect(res.status).toBe(503);
    expect(res.body.error.code).toBe('NOT_CONFIGURED');
  });

  it('calls Places (New) with key + field mask, maps results and caches them', async () => {
    const fetchMock = vi.fn(async () => new Response(JSON.stringify(okResponse), { status: 200 }));
    const { app, auth } = await appWith(new PlacesService('secret-key', fetchMock as unknown as typeof fetch));
    const res = await request(app).get('/api/v1/places/nearby?lat=35.0116&lng=135.7681').set(auth);
    expect(res.status).toBe(200);
    expect(res.body.places).toEqual([
      { id: 'p1', name: 'Kinkaku-ji', location: { lat: 35.03, lng: 135.72 }, rating: 4.6, ratingCount: 50000,
        category: 'Buddhist temple', address: 'Kyoto', googleMapsUri: 'https://maps.google.com/?cid=1' },
    ]);
    const [, init] = fetchMock.mock.calls[0] as unknown as [string, RequestInit];
    const headers = init.headers as Record<string, string>;
    expect(headers['X-Goog-Api-Key']).toBe('secret-key');
    expect(headers['X-Goog-FieldMask']).toContain('places.displayName');

    await request(app).get('/api/v1/places/nearby?lat=35.0116&lng=135.7681').set(auth);
    expect(fetchMock).toHaveBeenCalledTimes(1); // cached
  });

  it('maps upstream failures to 502 and validates coordinates', async () => {
    const fetchMock = vi.fn(async () => new Response('denied', { status: 403 }));
    const { app, auth } = await appWith(new PlacesService('k', fetchMock as unknown as typeof fetch));
    const res = await request(app).get('/api/v1/places/nearby?lat=1&lng=2').set(auth);
    expect(res.status).toBe(502);
    expect(res.body.error.code).toBe('UPSTREAM_FAILED');
    expect((await request(app).get('/api/v1/places/nearby?lat=200&lng=2').set(auth)).status).toBe(400);
  });
});
