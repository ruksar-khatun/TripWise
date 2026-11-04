import { beforeEach, describe, expect, it } from 'vitest';
import request from 'supertest';
import type { Express } from 'express';
import { createApp } from '../src/app.js';
import { loadEnv } from '../src/config/env.js';
import { createLogger } from '../src/config/logger.js';
import { openDatabase } from '../src/db/client.js';

const env = loadEnv({ NODE_ENV: 'test', JWT_SECRET: 'test-secret-'.padEnd(40, 'x') });
let app: Express;
let clock = new Date('2026-03-01T10:00:00Z');

beforeEach(async () => {
  clock = new Date('2026-03-01T10:00:00Z');
  const { db } = await openDatabase({ production: false }); // fresh in-memory Postgres per test
  app = createApp(env, createLogger(env), { db, now: () => clock });
});

const alex = { email: '  Alex@Example.com ', password: 'correct horse battery', displayName: 'Alex' };
const register = () => request(app).post('/api/v1/auth/register').send(alex);

describe('register', () => {
  it('creates an account, normalises email and returns tokens', async () => {
    const res = await register();
    expect(res.status).toBe(201);
    expect(res.body.user).toMatchObject({ email: 'alex@example.com', displayName: 'Alex', homeCurrency: 'INR' });
    expect(res.body.user).not.toHaveProperty('passwordHash');
    expect(res.body.tokens.accessToken).toMatch(/^ey/);
    expect(res.body.tokens.refreshToken.length).toBeGreaterThan(40);
    expect(res.body.tokens.accessTokenExpiresIn).toBe(900);
  });

  it('rejects duplicate email (any case) with a field error', async () => {
    await register();
    const res = await request(app).post('/api/v1/auth/register').send({ ...alex, email: 'ALEX@example.com' });
    expect(res.status).toBe(409);
    expect(res.body.error).toMatchObject({ code: 'VALIDATION_FAILED', details: { 'body.email': expect.any(String) } });
  });

  it('validates fields', async () => {
    const res = await request(app).post('/api/v1/auth/register').send({ email: 'nope', password: 'short', displayName: '' });
    expect(res.status).toBe(400);
    expect(Object.keys(res.body.error.details).sort()).toEqual(['body.displayName', 'body.email', 'body.password']);
  });
});

describe('login', () => {
  it('succeeds with correct password and fails generically otherwise', async () => {
    await register();
    const ok = await request(app).post('/api/v1/auth/login').send({ email: 'alex@example.com', password: alex.password });
    expect(ok.status).toBe(200);

    const wrong = await request(app).post('/api/v1/auth/login').send({ email: 'alex@example.com', password: 'wrong password' });
    const unknown = await request(app).post('/api/v1/auth/login').send({ email: 'who@example.com', password: 'wrong password' });
    for (const res of [wrong, unknown]) {
      expect(res.status).toBe(401);
      expect(res.body.error).toEqual({ code: 'INVALID_CREDENTIALS', message: 'Email or password is incorrect' });
    }
  });
});

describe('access tokens and /me', () => {
  it('requires a valid bearer token', async () => {
    const { body } = await register();
    expect((await request(app).get('/api/v1/me')).status).toBe(401);
    expect((await request(app).get('/api/v1/me').set('Authorization', 'Bearer garbage')).status).toBe(401);
    const me = await request(app).get('/api/v1/me').set('Authorization', `Bearer ${body.tokens.accessToken}`);
    expect(me.status).toBe(200);
    expect(me.body.email).toBe('alex@example.com');
  });

  it('updates profile fields and rejects unknown ones', async () => {
    const { body } = await register();
    const auth = { Authorization: `Bearer ${body.tokens.accessToken}` };
    const ok = await request(app).patch('/api/v1/me').set(auth).send({ displayName: 'Alex T', homeCurrency: 'JPY' });
    expect(ok.body).toMatchObject({ displayName: 'Alex T', homeCurrency: 'JPY' });
    expect((await request(app).patch('/api/v1/me').set(auth).send({ email: 'x@y.z' })).status).toBe(400);
    expect((await request(app).patch('/api/v1/me').set(auth).send({ homeCurrency: 'yen' })).status).toBe(400);
  });
});

describe('refresh rotation', () => {
  it('rotates tokens; reusing an old token revokes the whole session family', async () => {
    const { body } = await register();
    const first = body.tokens.refreshToken;

    const r1 = await request(app).post('/api/v1/auth/refresh').send({ refreshToken: first });
    expect(r1.status).toBe(200);
    const second = r1.body.tokens.refreshToken;
    expect(second).not.toBe(first);

    // Replay of the rotated token → 401 and the newer token is revoked too.
    expect((await request(app).post('/api/v1/auth/refresh').send({ refreshToken: first })).status).toBe(401);
    expect((await request(app).post('/api/v1/auth/refresh').send({ refreshToken: second })).status).toBe(401);
  });

  it('rejects expired refresh tokens', async () => {
    const { body } = await register();
    clock = new Date(clock.getTime() + 31 * 86_400_000);
    const res = await request(app).post('/api/v1/auth/refresh').send({ refreshToken: body.tokens.refreshToken });
    expect(res.status).toBe(401);
    expect(res.body.error.code).toBe('UNAUTHORIZED');
  });

  it('logout revokes the session', async () => {
    const { body } = await register();
    expect((await request(app).post('/api/v1/auth/logout').send({ refreshToken: body.tokens.refreshToken })).status).toBe(204);
    expect((await request(app).post('/api/v1/auth/refresh').send({ refreshToken: body.tokens.refreshToken })).status).toBe(401);
  });
});

describe('account deletion', () => {
  it('requires the password, then removes the account and its sessions', async () => {
    const { body } = await register();
    const auth = { Authorization: `Bearer ${body.tokens.accessToken}` };
    const wrong = await request(app).delete('/api/v1/me').set(auth).send({ password: 'nope nope' });
    expect(wrong.status).toBe(401);
    expect(wrong.body.error.code).toBe('INVALID_CREDENTIALS');

    expect((await request(app).delete('/api/v1/me').set(auth).send({ password: alex.password })).status).toBe(204);
    expect((await request(app).post('/api/v1/auth/refresh').send({ refreshToken: body.tokens.refreshToken })).status).toBe(401);
    const login = await request(app).post('/api/v1/auth/login').send({ email: alex.email, password: alex.password });
    expect(login.status).toBe(401);
  });
});
