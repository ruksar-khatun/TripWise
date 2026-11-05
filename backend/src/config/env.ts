import 'dotenv/config';
import { z } from 'zod';

const EnvSchema = z.object({
  NODE_ENV: z.enum(['development', 'test', 'production']).default('development'),
  PORT: z.coerce.number().int().positive().default(3000),
  LOG_LEVEL: z.enum(['fatal', 'error', 'warn', 'info', 'debug', 'trace']).default('info'),
  CORS_ORIGINS: z
    .string()
    .default('')
    .transform((s) => s.split(',').map((o) => o.trim()).filter(Boolean)),
  // Optional at boot so the service can start without AI; AI routes will check for it.
  DATABASE_URL: z.string().url().optional(),
  /** Dev only: directory for the embedded PGlite database (omit for in-memory). */
  PGLITE_DIR: z.string().optional(),
  /** HS256 signing key for access tokens. Required in production; at least 32 characters. */
  JWT_SECRET: z.string().min(32, 'JWT_SECRET must be at least 32 characters').optional(),
  ACCESS_TOKEN_TTL_SECONDS: z.coerce.number().int().min(60).max(3600).default(900),
  REFRESH_TOKEN_TTL_DAYS: z.coerce.number().int().min(1).max(180).default(30),
  /** Google Places API (New) server key. Optional: nearby places return NOT_CONFIGURED without it. */
  PLACES_API_KEY: z.string().optional(),
  GEMINI_API_KEY: z.string().optional(),
  /** Primary Gemini model. gemini-2.5-flash is no longer offered to new API keys. */
  GEMINI_MODEL: z.string().default('gemini-3.5-flash'),
  /** Comma-separated models tried in order when the primary is unavailable/overloaded. */
  GEMINI_FALLBACK_MODELS: z.string().default('gemini-3.8-flash,gemini-3.1-flash-lite')
    .transform((s) => s.split(',').map((m) => m.trim()).filter(Boolean)),
  /** Max AI requests (chat messages + itineraries) per user per day. */
  AI_DAILY_LIMIT: z.coerce.number().int().min(1).default(100),
});

export type Env = z.infer<typeof EnvSchema>;

export function loadEnv(source: NodeJS.ProcessEnv = process.env): Env {
  const parsed = EnvSchema.safeParse(source);
  if (!parsed.success) {
    throw new Error(`Invalid environment configuration:\n${z.prettifyError(parsed.error)}`);
  }
  if (parsed.data.NODE_ENV === 'production') {
    if (!parsed.data.JWT_SECRET) throw new Error('Invalid environment configuration: JWT_SECRET is required in production');
    if (!parsed.data.DATABASE_URL) throw new Error('Invalid environment configuration: DATABASE_URL is required in production');
  }
  return parsed.data;
}
