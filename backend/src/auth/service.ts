import { randomUUID } from 'node:crypto';
import { and, eq, isNull } from 'drizzle-orm';
import type { Database } from '../db/client.js';
import { refreshTokens, users, type User } from '../db/schema.js';
import { ErrorCode, HttpError } from '../http/errors.js';
import { hashPassword, timingSafeDummyVerify, verifyPassword } from './passwords.js';
import { hashToken, newRefreshToken, signAccessToken, type TokenConfig } from './tokens.js';

export interface PublicUser {
  id: string;
  email: string;
  displayName: string;
  homeCurrency: string;
  createdAt: string;
}

export interface AuthTokens {
  accessToken: string;
  accessTokenExpiresIn: number;
  refreshToken: string;
  refreshTokenExpiresAt: string;
}

export const toPublicUser = (u: User): PublicUser => ({
  id: u.id,
  email: u.email,
  displayName: u.displayName,
  homeCurrency: u.homeCurrency,
  createdAt: u.createdAt.toISOString(),
});

export const normaliseEmail = (email: string) => email.trim().toLowerCase();

const invalidCredentials = () => new HttpError(401, ErrorCode.INVALID_CREDENTIALS, 'Email or password is incorrect');
const invalidSession = () => HttpError.unauthorized('Session expired. Please sign in again.');

export class AuthService {
  constructor(
    private readonly db: Database,
    private readonly tokens: TokenConfig,
    private readonly now: () => Date = () => new Date(),
  ) {}

  async register(input: { email: string; password: string; displayName: string }) {
    const email = normaliseEmail(input.email);
    const passwordHash = await hashPassword(input.password);
    const inserted = await this.db
      .insert(users)
      .values({ email, passwordHash, displayName: input.displayName.trim() })
      .onConflictDoNothing({ target: users.email })
      .returning();
    const user = inserted[0];
    if (!user) {
      throw new HttpError(409, ErrorCode.VALIDATION_FAILED, 'Email already registered', {
        details: { 'body.email': 'An account with this email already exists' },
      });
    }
    return { user: toPublicUser(user), tokens: await this.issueTokens(user.id, randomUUID()) };
  }

  async login(input: { email: string; password: string }) {
    const user = await this.findByEmail(normaliseEmail(input.email));
    if (!user) {
      await timingSafeDummyVerify(input.password);
      throw invalidCredentials();
    }
    if (!(await verifyPassword(user.passwordHash, input.password))) throw invalidCredentials();
    return { user: toPublicUser(user), tokens: await this.issueTokens(user.id, randomUUID()) };
  }

  /** Rotates the refresh token. Re-use of a rotated token revokes the whole family. */
  async refresh(refreshToken: string) {
    const now = this.now();
    const row = (await this.db.select().from(refreshTokens).where(eq(refreshTokens.tokenHash, hashToken(refreshToken))).limit(1))[0];
    if (!row) throw invalidSession();
    if (row.revokedAt) {
      await this.revokeFamily(row.familyId);
      throw invalidSession();
    }
    if (row.expiresAt <= now) throw invalidSession();

    // Atomic claim: only one concurrent request can rotate a given token.
    const claimed = await this.db
      .update(refreshTokens)
      .set({ revokedAt: now })
      .where(and(eq(refreshTokens.id, row.id), isNull(refreshTokens.revokedAt)))
      .returning({ id: refreshTokens.id });
    if (claimed.length === 0) {
      await this.revokeFamily(row.familyId);
      throw invalidSession();
    }
    const user = (await this.db.select().from(users).where(eq(users.id, row.userId)).limit(1))[0];
    if (!user) throw invalidSession();
    return { user: toPublicUser(user), tokens: await this.issueTokens(user.id, row.familyId) };
  }

  /** Revokes the session the token belongs to. Idempotent; unknown tokens are ignored. */
  async logout(refreshToken: string) {
    const row = (await this.db.select().from(refreshTokens).where(eq(refreshTokens.tokenHash, hashToken(refreshToken))).limit(1))[0];
    if (row) await this.revokeFamily(row.familyId);
  }

  async getUser(userId: string): Promise<PublicUser> {
    const user = (await this.db.select().from(users).where(eq(users.id, userId)).limit(1))[0];
    if (!user) throw invalidSession();
    return toPublicUser(user);
  }

  async updateUser(userId: string, patch: { displayName?: string; homeCurrency?: string }): Promise<PublicUser> {
    const updated = await this.db
      .update(users)
      .set({
        ...(patch.displayName !== undefined ? { displayName: patch.displayName.trim() } : {}),
        ...(patch.homeCurrency !== undefined ? { homeCurrency: patch.homeCurrency } : {}),
        updatedAt: this.now(),
      })
      .where(eq(users.id, userId))
      .returning();
    if (!updated[0]) throw invalidSession();
    return toPublicUser(updated[0]);
  }

  /** Deletes the account and (via ON DELETE CASCADE) all sessions. Requires the current password. */
  async deleteAccount(userId: string, password: string) {
    const user = (await this.db.select().from(users).where(eq(users.id, userId)).limit(1))[0];
    if (!user) throw invalidSession();
    if (!(await verifyPassword(user.passwordHash, password))) throw invalidCredentials();
    await this.db.delete(users).where(eq(users.id, userId));
  }

  private async findByEmail(email: string) {
    return (await this.db.select().from(users).where(eq(users.email, email)).limit(1))[0];
  }

  private async revokeFamily(familyId: string) {
    await this.db
      .update(refreshTokens)
      .set({ revokedAt: this.now() })
      .where(and(eq(refreshTokens.familyId, familyId), isNull(refreshTokens.revokedAt)));
  }

  private async issueTokens(userId: string, familyId: string): Promise<AuthTokens> {
    const refreshToken = newRefreshToken();
    const expiresAt = new Date(this.now().getTime() + this.tokens.refreshTtlDays * 86_400_000);
    await this.db.insert(refreshTokens).values({ userId, familyId, tokenHash: hashToken(refreshToken), expiresAt });
    return {
      accessToken: await signAccessToken(userId, this.tokens),
      accessTokenExpiresIn: this.tokens.accessTtlSeconds,
      refreshToken,
      refreshTokenExpiresAt: expiresAt.toISOString(),
    };
  }
}
