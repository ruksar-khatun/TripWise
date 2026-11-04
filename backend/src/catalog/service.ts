import { readFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { and, arrayContains, desc, eq, ilike, or, sql } from 'drizzle-orm';
import { z } from 'zod';
import type { Database } from '../db/client.js';
import { destinations } from '../db/schema.js';
import { HttpError } from '../http/errors.js';
import { CatalogDestination } from './types.js';

const seedFile = fileURLToPath(new URL('../../catalog/destinations.json', import.meta.url));

export async function loadCatalogSeed(path = seedFile): Promise<CatalogDestination[]> {
  const raw = JSON.parse(await readFile(path, 'utf8')) as { destinations: unknown };
  return z.array(CatalogDestination).parse(raw.destinations);
}

/** Idempotent: inserts or updates every curated destination (run at start-up). */
export async function syncCatalog(db: Database, items: CatalogDestination[]) {
  for (const d of items) {
    const row = {
      id: d.id,
      name: d.name,
      country: d.country,
      interests: d.interests,
      featured: d.featured,
      popularity: d.popularity,
      data: d,
      updatedAt: new Date(),
    };
    await db.insert(destinations).values(row).onConflictDoUpdate({ target: destinations.id, set: row });
  }
  return items.length;
}

export class CatalogService {
  constructor(private readonly db: Database) {}

  async list(opts: { query?: string; interest?: string; featured?: boolean; limit: number; offset: number }) {
    const conditions = [];
    if (opts.query) {
      const q = `%${opts.query.replace(/[%_\\]/g, (c) => `\\${c}`)}%`;
      conditions.push(or(ilike(destinations.name, q), ilike(destinations.country, q)));
    }
    if (opts.interest) conditions.push(arrayContains(destinations.interests, [opts.interest]));
    if (opts.featured !== undefined) conditions.push(eq(destinations.featured, opts.featured));
    const where = conditions.length ? and(...conditions) : undefined;

    const [rows, total] = await Promise.all([
      this.db.select({ data: destinations.data }).from(destinations).where(where)
        .orderBy(desc(destinations.popularity), destinations.id).limit(opts.limit).offset(opts.offset),
      this.db.select({ n: sql<number>`count(*)::int` }).from(destinations).where(where),
    ]);
    return { items: rows.map((r) => r.data), total: total[0]?.n ?? 0, limit: opts.limit, offset: opts.offset };
  }

  async get(id: string) {
    const row = (await this.db.select({ data: destinations.data }).from(destinations).where(eq(destinations.id, id)).limit(1))[0];
    if (!row) throw HttpError.notFound('Destination not found');
    return row.data;
  }
}
