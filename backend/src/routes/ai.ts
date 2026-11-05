import { Router, type RequestHandler } from 'express';
import { z } from 'zod';
import type { AiClient, DailyQuota } from '../ai/gemini.js';
import { chatSystemPrompt } from '../ai/chat.js';
import type { CatalogService } from '../catalog/service.js';
import { ErrorCode, HttpError } from '../http/errors.js';
import { validate } from '../http/validate.js';
import type { ItineraryService } from '../ai/itinerary.js';
import { ItineraryRequest } from '../ai/itinerary.js';
import { RecommendationRequest, type RecommendationService } from '../ai/recommendations.js';
import { Interest } from '../catalog/types.js';

const ChatBody = z.object({
  messages: z
    .array(z.object({ role: z.enum(['user', 'assistant']), text: z.string().trim().min(1).max(4_000) }))
    .min(1)
    .max(30)
    .refine((m) => m[m.length - 1]?.role === 'user', 'Last message must be from the user'),
  context: z
    .object({
      destinationId: z.string().regex(/^[a-z0-9-]{1,64}$/).optional(),
      trip: z
        .object({
          title: z.string().max(80),
          destinationName: z.string().max(80),
          startDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/),
          endDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/),
          travellers: z.number().int().min(1).max(20),
          budgetInr: z.number().int().min(0).max(100_000_000).optional(),
          itinerary: z.array(z.string().trim().min(1).max(120)).max(40).optional(),
        })
        .optional(),
      preferences: z
        .object({
          budgetLevel: z.enum(['BUDGET', 'MID', 'LUXURY']).optional(),
          days: z.number().int().min(1).max(30).optional(),
          travellers: z.number().int().min(1).max(20).optional(),
          interests: z.array(Interest).max(7).optional(),
          budgetInr: z.number().int().min(0).max(100_000_000).optional(),
        })
        .optional(),
    })
    .optional(),
});

export interface AiDeps {
  ai: AiClient;
  quota: DailyQuota;
  catalog: CatalogService;
  itineraries: ItineraryService;
  recommendations: RecommendationService;
}

const quotaExceeded = () => new HttpError(429, ErrorCode.AI_QUOTA_EXCEEDED, "You've reached today's AI limit");

/** Signed-in only: AI calls cost money. */
export function aiRouter(deps: AiDeps, requireAuth: RequestHandler) {
  const router = Router();
  router.use(requireAuth);

  /**
   * Streams the reply as Server-Sent Events:
   *   data: {"delta":"..."}   (repeated)
   *   data: {"done":true}
   *   event: error / data: {"code":"AI_UNAVAILABLE","message":"..."}  (failure after streaming began)
   */
  router.post('/chat', validate({ body: ChatBody }), async (req, res) => {
    if (!deps.ai.configured) throw HttpError.notConfigured('The AI assistant');
    if (!deps.quota.take(res.locals.userId)) throw quotaExceeded();
    const body = res.locals.valid.body as z.infer<typeof ChatBody>;
    const destination = body.context?.destinationId
      ? await deps.catalog.get(body.context.destinationId).catch(() => undefined)
      : undefined;
    const system = chatSystemPrompt({ destination, trip: body.context?.trip, preferences: body.context?.preferences });
    const turns = body.messages.map((m) => ({ role: m.role === 'user' ? ('user' as const) : ('model' as const), text: m.text }));

    const abort = new AbortController();
    req.on('close', () => abort.abort());
    res.status(200).set({ 'Content-Type': 'text/event-stream', 'Cache-Control': 'no-cache', Connection: 'keep-alive' });
    res.flushHeaders();
    const send = (event: string | null, data: object) =>
      res.write(`${event ? `event: ${event}\n` : ''}data: ${JSON.stringify(data)}\n\n`);
    try {
      let any = false;
      for await (const delta of deps.ai.streamChat(system, turns, abort.signal)) {
        any = true;
        send(null, { delta });
      }
      if (!any) send('error', { code: ErrorCode.AI_INVALID_OUTPUT, message: 'The assistant returned no answer' });
      else send(null, { done: true });
    } catch (err) {
      if (!abort.signal.aborted) {
        req.log?.warn({ err }, 'AI chat failed');
        send('error', { code: ErrorCode.AI_UNAVAILABLE, message: 'The assistant is unavailable right now' });
      }
    } finally {
      res.end();
    }
  });

  router.post('/itineraries', validate({ body: ItineraryRequest }), async (_req, res) => {
    const body = res.locals.valid.body as z.infer<typeof ItineraryRequest>;
    const destination = await deps.catalog.get(body.destinationId);
    // Template fallback doesn't use AI, so only count quota when AI is used.
    if (deps.ai.configured && !deps.quota.take(res.locals.userId)) throw quotaExceeded();
    res.json(await deps.itineraries.generate(destination, body));
  });

  /** Structured stays / restaurants for a destination (cards the app links to Google Maps). */
  router.post('/recommendations', validate({ body: RecommendationRequest }), async (_req, res) => {
    const body = res.locals.valid.body as z.infer<typeof RecommendationRequest>;
    const destination = await deps.catalog.get(body.destinationId);
    if (!deps.ai.configured) throw HttpError.notConfigured('TripMate AI');
    if (!deps.quota.take(res.locals.userId)) throw quotaExceeded();
    res.json(await deps.recommendations.recommend(destination, body));
  });

  return router;
}
