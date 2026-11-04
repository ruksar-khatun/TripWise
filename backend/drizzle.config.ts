import { defineConfig } from 'drizzle-kit';

// `npm run db:generate` writes SQL migrations to ./drizzle (committed). They are applied at server start.
export default defineConfig({
  dialect: 'postgresql',
  schema: './src/db/schema.ts',
  out: './drizzle',
});
