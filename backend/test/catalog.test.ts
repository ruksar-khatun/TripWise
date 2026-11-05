import { beforeAll, describe, expect, it } from 'vitest';
import request from 'supertest';
import type { Express } from 'express';
import { createApp } from '../src/app.js';
import { loadCatalogSeed, syncCatalog } from '../src/catalog/service.js';
import { loadEnv } from '../src/config/env.js';
import { createLogger } from '../src/config/logger.js';
import { openDatabase } from '../src/db/client.js';

let app: Express;
const env = loadEnv({ NODE_ENV: 'test' });

beforeAll(async () => {
  const { db } = await openDatabase({ production: false });
  const seed = await loadCatalogSeed();
  await syncCatalog(db, seed);
  await syncCatalog(db, seed); // idempotent
  app = createApp(env, createLogger(env), { db });
});

describe('catalog seed', () => {
  it('validates and has unique ids, images and attractions everywhere', async () => {
    const seed = await loadCatalogSeed();
    expect(seed.length).toBe(16);
    expect(new Set(seed.map((d) => d.id)).size).toBe(seed.length);
    for (const d of seed) {
      expect(d.images.length).toBeGreaterThan(0);
      expect(d.attractions.length).toBeGreaterThanOrEqual(5);
      expect(new Set(d.attractions.map((a) => a.id)).size).toBe(d.attractions.length);
      expect(d.country).toBe('India');
      expect(d.currency).toBe('INR');
      expect(d.rating).toBeNull(); // no invented ratings
    }
  });
});

describe('GET /api/v1/destinations', () => {
  it('lists by popularity with total', async () => {
    const res = await request(app).get('/api/v1/destinations');
    expect(res.status).toBe(200);
    expect(res.body.total).toBe(16);
    const pops = res.body.items.map((d: { popularity: number }) => d.popularity);
    expect(pops).toEqual([...pops].sort((a, b) => b - a));
  });

  it('searches name and country case-insensitively, and filters', async () => {
    const raj = await request(app).get('/api/v1/destinations?query=JAIPUR');
    expect(raj.body.items.map((d: { id: string }) => d.id)).toEqual(['jaipur']);
    const india = await request(app).get('/api/v1/destinations?query=india');
    expect(india.body.total).toBe(16); // country search
    const beaches = await request(app).get('/api/v1/destinations?interest=BEACHES');
    expect(beaches.body.items.every((d: { interests: string[] }) => d.interests.includes('BEACHES'))).toBe(true);
    const featured = await request(app).get('/api/v1/destinations?featured=true');
    expect(featured.body.items.length).toBe(5);
    expect((await request(app).get('/api/v1/destinations?query=%25')).body.total).toBe(0); // wildcard escaped
  });

  it('validates query params and paginates', async () => {
    expect((await request(app).get('/api/v1/destinations?interest=SPACE')).status).toBe(400);
    const page = await request(app).get('/api/v1/destinations?limit=5&offset=5');
    expect(page.body.items.length).toBe(5);
  });

  it('returns details with attractions, 404 for unknown', async () => {
    const res = await request(app).get('/api/v1/destinations/agra');
    expect(res.body.attractions[0]).toMatchObject({ id: 'agra-1', name: 'Taj Mahal' });
    expect((await request(app).get('/api/v1/destinations/atlantis')).status).toBe(404);
    expect((await request(app).get('/api/v1/destinations/BAD!')).status).toBe(400);
  });
});
