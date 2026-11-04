import { createHash, randomBytes } from 'node:crypto';
import { SignJWT, jwtVerify } from 'jose';

const ISSUER = 'tripwise-api';
const AUDIENCE = 'tripwise-app';

export interface TokenConfig {
  secret: Uint8Array;
  accessTtlSeconds: number;
  refreshTtlDays: number;
}

export const toSecret = (value: string) => new TextEncoder().encode(value);

/** Short-lived JWT (HS256) carrying only the user id. */
export async function signAccessToken(userId: string, cfg: TokenConfig): Promise<string> {
  return new SignJWT({})
    .setProtectedHeader({ alg: 'HS256', typ: 'JWT' })
    .setSubject(userId)
    .setIssuer(ISSUER)
    .setAudience(AUDIENCE)
    .setIssuedAt()
    .setExpirationTime(`${cfg.accessTtlSeconds}s`)
    .sign(cfg.secret);
}

/** Returns the user id, or null for any invalid/expired token. */
export async function verifyAccessToken(token: string, cfg: TokenConfig): Promise<string | null> {
  try {
    const { payload } = await jwtVerify(token, cfg.secret, { issuer: ISSUER, audience: AUDIENCE, algorithms: ['HS256'] });
    return typeof payload.sub === 'string' ? payload.sub : null;
  } catch {
    return null;
  }
}

/** 256-bit opaque refresh token; only its hash is stored. */
export const newRefreshToken = () => randomBytes(32).toString('base64url');
export const hashToken = (token: string) => createHash('sha256').update(token).digest('hex');
