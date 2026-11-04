import { fileURLToPath } from 'node:url';
import type { PgDatabase, PgQueryResultHKT } from 'drizzle-orm/pg-core';
import type { Logger } from 'pino';
import { schema } from './schema.js';

export type Database = PgDatabase<PgQueryResultHKT, typeof schema>;

export interface DatabaseHandle {
  db: Database;
  close: () => Promise<void>;
}

const migrationsFolder = fileURLToPath(new URL('../../drizzle', import.meta.url));

/**
 * Production: PostgreSQL via DATABASE_URL (e.g. Cloud SQL).
 * Development/tests without DATABASE_URL: embedded PGlite (real Postgres compiled to WASM),
 * in memory or at PGLITE_DIR. Migrations run on start in both cases.
 */
export async function openDatabase(
  opts: { databaseUrl?: string; pgliteDir?: string; production: boolean },
  logger?: Logger,
): Promise<DatabaseHandle> {
  if (opts.databaseUrl) {
    const { Pool } = await import('pg');
    const { drizzle } = await import('drizzle-orm/node-postgres');
    const { migrate } = await import('drizzle-orm/node-postgres/migrator');
    const pool = new Pool({ connectionString: opts.databaseUrl, max: 10 });
    const db = drizzle(pool, { schema });
    await migrate(db, { migrationsFolder });
    logger?.info('Connected to PostgreSQL');
    return { db: db as unknown as Database, close: () => pool.end() };
  }
  if (opts.production) throw new Error('DATABASE_URL is required in production');

  const { PGlite } = await import('@electric-sql/pglite');
  const { drizzle } = await import('drizzle-orm/pglite');
  const { migrate } = await import('drizzle-orm/pglite/migrator');
  const client = new PGlite(opts.pgliteDir);
  const db = drizzle(client, { schema });
  await migrate(db, { migrationsFolder });
  logger?.warn(`Using embedded PGlite database (${opts.pgliteDir ?? 'in-memory'}). Set DATABASE_URL for PostgreSQL.`);
  return { db: db as unknown as Database, close: () => client.close() };
}
