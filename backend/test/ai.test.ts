import { beforeEach, describe, expect, it } from 'vitest';
import request from 'supertest';
import type { Express } from 'express';
import { createApp } from '../src/app.js';
import type { AiClient, ChatTurn } from '../src/ai/gemini.js';
import { DailyQuota } from '../src/ai/gemini.js';
import { loadCatalogSeed, syncCatalog } from '../src/catalog/service.js';
import { loadEnv } from '../src/config/env.js';
import { createLogger } from '../src/config/logger.js';
import { openDatabase } from '../src/db/client.js';

class FakeAi implements AiClient {
  configured = true;
  lastSystem = '';
  lastTurns: ChatTurn[] = [];
  chunks: string[] = ['Hello', ' traveller!'];
  failMidStream = false;
  json: unknown[] = [];
  jsonCalls = 0;

  async *streamChat(system: string, turns: ChatTurn[]) {
    this.lastSystem = system;
    this.lastTurns = turns;
    for (const c of this.chunks) {
      yield c;
      if (this.failMidStream) throw new Error('boom');
    }
  }

  async generateJson() {
    this.jsonCalls++;
    const next = this.json.shift();
    if (next instanceof Error) throw next;
    return next;
  }
}

let ai: FakeAi;
let app: Express;
let auth: Record<string, string>;

async function setup(limit = 100) {
  const env = loadEnv({ NODE_ENV: 'test', JWT_SECRET: 'x'.repeat(40), AI_DAILY_LIMIT: String(limit) });
  const { db } = await openDatabase({ production: false });
  await syncCatalog(db, await loadCatalogSeed());
  ai = new FakeAi();
  app = createApp(env, createLogger(env), { db, ai });
  const reg = await request(app).post('/api/v1/auth/register').send({ email: 'ai@x.io', password: 'password123', displayName: 'A' });
  auth = { Authorization: `Bearer ${reg.body.tokens.accessToken}` };
}

const chat = (body: object) => request(app).post('/api/v1/ai/chat').set(auth).send(body);
const userSays = (text: string) => ({ messages: [{ role: 'user', text }] });

describe('POST /api/v1/ai/chat', () => {
  beforeEach(() => setup());

  it('streams deltas as server-sent events and ends with done', async () => {
    const res = await chat({ ...userSays('What should I eat in Kyoto?'), context: { destinationId: 'kyoto' } });
    expect(res.status).toBe(200);
    expect(res.headers['content-type']).toContain('text/event-stream');
    expect(res.text).toBe('data: {"delta":"Hello"}\n\ndata: {"delta":" traveller!"}\n\ndata: {"done":true}\n\n');
    // Destination context comes from the curated catalog.
    expect(ai.lastSystem).toContain('Kyoto, Japan');
    expect(ai.lastSystem).toContain('Fushimi Inari Taisha');
    expect(ai.lastTurns).toEqual([{ role: 'user', text: 'What should I eat in Kyoto?' }]);
  });

  it('maps assistant history to model turns and validates input', async () => {
    await chat({ messages: [{ role: 'user', text: 'Hi' }, { role: 'assistant', text: 'Hello' }, { role: 'user', text: 'Tips?' }] });
    expect(ai.lastTurns.map((t) => t.role)).toEqual(['user', 'model', 'user']);
    expect((await chat({ messages: [{ role: 'assistant', text: 'x' }] })).status).toBe(400);
    expect((await chat({ messages: [] })).status).toBe(400);
    expect((await chat(userSays('x'.repeat(4_001)))).status).toBe(400);
  });

  it('requires sign-in and reports missing configuration', async () => {
    expect((await request(app).post('/api/v1/ai/chat').send(userSays('hi'))).status).toBe(401);
    ai.configured = false;
    const res = await chat(userSays('hi'));
    expect(res.status).toBe(503);
    expect(res.body.error.code).toBe('NOT_CONFIGURED');
  });

  it('sends an error event when the model fails mid-stream', async () => {
    ai.failMidStream = true;
    const res = await chat(userSays('hi'));
    expect(res.text).toContain('data: {"delta":"Hello"}');
    expect(res.text).toContain('event: error\ndata: {"code":"AI_UNAVAILABLE"');
  });

  it('enforces the per-user daily quota', async () => {
    await setup(2);
    expect((await chat(userSays('1'))).status).toBe(200);
    expect((await chat(userSays('2'))).status).toBe(200);
    const res = await chat(userSays('3'));
    expect(res.status).toBe(429);
    expect(res.body.error.code).toBe('AI_QUOTA_EXCEEDED');
  });
});

const validDraft = (days: number) => ({
  title: 'Kyoto days',
  summary: 'Temples and food',
  days: Array.from({ length: days }, (_, i) => ({
    day: i + 7, // wrong numbering is normalised
    theme: 'Temples',
    items: [
      { startTime: '14:00', title: 'Nishiki Market', category: 'FOOD', durationMinutes: 90, estimatedCostUsd: 20, attractionId: 'kyoto-5' },
      { startTime: '08:00', title: 'Fushimi Inari', category: 'SIGHTSEEING', durationMinutes: 150, estimatedCostUsd: 0, attractionId: 'made-up' },
    ],
  })),
});

describe('POST /api/v1/ai/itineraries', () => {
  beforeEach(() => setup());
  const generate = (body: object) => request(app).post('/api/v1/ai/itineraries').set(auth).send(body);

  it('returns a validated, normalised AI plan', async () => {
    ai.json = [validDraft(2)];
    const res = await generate({ destinationId: 'kyoto', days: 2, pace: 'RELAXED', interests: ['FOOD'] });
    expect(res.status).toBe(200);
    expect(res.body.source).toBe('ai');
    expect(res.body.days.map((d: { day: number }) => d.day)).toEqual([1, 2]);
    const items = res.body.days[0].items;
    expect(items.map((i: { startTime: string }) => i.startTime)).toEqual(['08:00', '14:00']); // sorted
    expect(items[0].attractionId).toBeNull(); // unknown id dropped
    expect(items[1].attractionId).toBe('kyoto-5');
  });

  it('retries once on invalid output, then falls back to the curated template', async () => {
    ai.json = [{ nonsense: true }, validDraft(1)]; // wrong day count on retry too
    const res = await generate({ destinationId: 'kyoto', days: 3 });
    expect(ai.jsonCalls).toBe(2);
    expect(res.body.source).toBe('template');
    expect(res.body.days).toHaveLength(3);
    for (const day of res.body.days) {
      expect(day.items.length).toBeGreaterThan(0);
      expect(day.items.some((i: { category: string }) => i.category === 'FOOD')).toBe(true);
    }
  });

  it('uses the template without AI and validates the request', async () => {
    ai.configured = false;
    const res = await generate({ destinationId: 'bali', days: 1, pace: 'PACKED' });
    expect(res.body.source).toBe('template');
    expect(res.body.days[0].items).toHaveLength(5);
    expect((await generate({ destinationId: 'bali', days: 0 })).status).toBe(400);
    expect((await generate({ destinationId: 'atlantis', days: 2 })).status).toBe(404);
  });
});

describe('DailyQuota', () => {
  it('resets each UTC day', () => {
    let now = new Date('2026-01-01T23:59:00Z');
    const q = new DailyQuota(1, () => now);
    expect(q.take('u')).toBe(true);
    expect(q.take('u')).toBe(false);
    now = new Date('2026-01-02T00:01:00Z');
    expect(q.take('u')).toBe(true);
  });
});
