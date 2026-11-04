import { sql } from 'drizzle-orm';
import { boolean, index, integer, jsonb, pgTable, text, timestamp, uniqueIndex, uuid } from 'drizzle-orm/pg-core';
import type { CatalogDestination } from '../catalog/types.js';

/** Accounts. Emails are stored normalised (trimmed, lower-case). */
export const users = pgTable(
  'users',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    email: text('email').notNull(),
    passwordHash: text('password_hash').notNull(),
    displayName: text('display_name').notNull(),
    homeCurrency: text('home_currency').notNull().default('INR'),
    createdAt: timestamp('created_at', { withTimezone: true }).notNull().defaultNow(),
    updatedAt: timestamp('updated_at', { withTimezone: true }).notNull().defaultNow(),
  },
  (t) => [uniqueIndex('users_email_unique').on(t.email)],
);

/**
 * Opaque refresh tokens, stored only as SHA-256 hashes. Tokens rotate on every use; all tokens
 * descending from one sign-in share a family, so presenting an already-used token (theft signal)
 * revokes the whole family.
 */
export const refreshTokens = pgTable(
  'refresh_tokens',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    userId: uuid('user_id')
      .notNull()
      .references(() => users.id, { onDelete: 'cascade' }),
    tokenHash: text('token_hash').notNull(),
    familyId: uuid('family_id').notNull(),
    expiresAt: timestamp('expires_at', { withTimezone: true }).notNull(),
    revokedAt: timestamp('revoked_at', { withTimezone: true }),
    createdAt: timestamp('created_at', { withTimezone: true }).notNull().default(sql`now()`),
  },
  (t) => [
    uniqueIndex('refresh_tokens_hash_unique').on(t.tokenHash),
    index('refresh_tokens_family_idx').on(t.familyId),
    index('refresh_tokens_user_idx').on(t.userId),
  ],
);

/**
 * Curated destination catalog. Searchable columns are denormalised; the full document
 * (images, attractions, cost index...) lives in `data`. Seeded from catalog/destinations.json at start-up.
 */
export const destinations = pgTable(
  'destinations',
  {
    id: text('id').primaryKey(),
    name: text('name').notNull(),
    country: text('country').notNull(),
    interests: text('interests').array().notNull(),
    featured: boolean('featured').notNull().default(false),
    popularity: integer('popularity').notNull().default(0),
    data: jsonb('data').$type<CatalogDestination>().notNull(),
    updatedAt: timestamp('updated_at', { withTimezone: true }).notNull().defaultNow(),
  },
  (t) => [index('destinations_popularity_idx').on(t.popularity)],
);

export const schema = { users, refreshTokens, destinations };
export type User = typeof users.$inferSelect;
