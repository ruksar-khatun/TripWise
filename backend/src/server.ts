import { loadEnv } from './config/env.js';
import { createApp } from './app.js';

const env = loadEnv();
const app = createApp(env);

app.listen(env.PORT, () => {
  console.log(`tripwise-backend listening on http://localhost:${env.PORT} (${env.NODE_ENV})`);
  if (!env.GEMINI_API_KEY) console.warn('GEMINI_API_KEY not set: AI features will be unavailable.');
});
