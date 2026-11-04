import { defineConfig } from 'vitest/config';

// Tests start an embedded Postgres (PGlite) and hash passwords with Argon2, so allow more than 5 s.
export default defineConfig({ test: { testTimeout: 30_000, hookTimeout: 30_000 } });
