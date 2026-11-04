import { GoogleGenAI, HarmBlockThreshold, HarmCategory } from '@google/genai';

export interface ChatTurn {
  role: 'user' | 'model';
  text: string;
}

/** Narrow interface over Gemini so routes/services are testable with a fake. */
export interface AiClient {
  readonly configured: boolean;
  streamChat(system: string, turns: ChatTurn[], signal?: AbortSignal): AsyncIterable<string>;
  generateJson(system: string, prompt: string, jsonSchema: object): Promise<unknown>;
}

const SAFETY = [
  HarmCategory.HARM_CATEGORY_HARASSMENT,
  HarmCategory.HARM_CATEGORY_HATE_SPEECH,
  HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT,
  HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT,
].map((category) => ({ category, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE }));

export class GeminiClient implements AiClient {
  private readonly ai: GoogleGenAI | null;

  constructor(apiKey: string | undefined, private readonly model: string) {
    this.ai = apiKey ? new GoogleGenAI({ apiKey }) : null;
  }

  get configured() {
    return this.ai !== null;
  }

  async *streamChat(system: string, turns: ChatTurn[], signal?: AbortSignal): AsyncIterable<string> {
    if (!this.ai) throw new Error('not configured');
    const stream = await this.ai.models.generateContentStream({
      model: this.model,
      contents: turns.map((t) => ({ role: t.role, parts: [{ text: t.text }] })),
      config: { systemInstruction: system, temperature: 0.7, maxOutputTokens: 1_024, safetySettings: SAFETY, abortSignal: signal },
    });
    for await (const chunk of stream) {
      const text = chunk.text;
      if (text) yield text;
    }
  }

  async generateJson(system: string, prompt: string, jsonSchema: object): Promise<unknown> {
    if (!this.ai) throw new Error('not configured');
    const res = await this.ai.models.generateContent({
      model: this.model,
      contents: [{ role: 'user', parts: [{ text: prompt }] }],
      config: {
        systemInstruction: system,
        temperature: 0.6,
        maxOutputTokens: 8_192,
        responseMimeType: 'application/json',
        responseJsonSchema: jsonSchema,
        safetySettings: SAFETY,
      },
    });
    return JSON.parse(res.text ?? '');
  }
}

/** Simple per-user daily request counter (in memory; use a shared store when scaling out). */
export class DailyQuota {
  private readonly counts = new Map<string, { day: string; n: number }>();
  constructor(private readonly limit: number, private readonly now: () => Date = () => new Date()) {}

  /** Returns false when the user has used today's allowance. */
  take(userId: string): boolean {
    const day = this.now().toISOString().slice(0, 10);
    const entry = this.counts.get(userId);
    if (!entry || entry.day !== day) {
      this.counts.set(userId, { day, n: 1 });
      return true;
    }
    if (entry.n >= this.limit) return false;
    entry.n++;
    return true;
  }
}
