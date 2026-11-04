type Fetch = typeof fetch;

export interface RatesResult {
  base: 'USD';
  rates: Record<string, number>;
  /** 'live' from the rates provider, or 'fallback' (bundled approximate rates) when it's unreachable. */
  source: 'live' | 'fallback';
  updatedAt: string;
}

/**
 * Approximate USD rates for the catalog's currencies, used only when the live provider fails.
 * Good enough for trip estimates; never for payments.
 */
const FALLBACK: Record<string, number> = {
  USD: 1, EUR: 0.92, GBP: 0.79, INR: 83.5, JPY: 150, KRW: 1_350, THB: 36, SGD: 1.35, AED: 3.67,
  IDR: 15_800, VND: 25_000, MVR: 15.4, CHF: 0.88, TRY: 32, AUD: 1.52, CAD: 1.36,
};

const TTL_MS = 12 * 60 * 60_000;

/** USD-based exchange rates from open.er-api.com (free, no key), cached for 12 hours. */
export class CurrencyService {
  private cached: RatesResult | null = null;
  private cachedAt = 0;

  constructor(
    private readonly fetchImpl: Fetch = fetch,
    private readonly now: () => number = Date.now,
    private readonly url = 'https://open.er-api.com/v6/latest/USD',
  ) {}

  async usdRates(): Promise<RatesResult> {
    if (this.cached && this.now() - this.cachedAt < TTL_MS) return this.cached;
    try {
      const res = await this.fetchImpl(this.url, { signal: AbortSignal.timeout(8_000) });
      if (!res.ok) throw new Error(`rates ${res.status}`);
      const body = (await res.json()) as { result?: string; rates?: Record<string, number>; time_last_update_utc?: string };
      if (body.result !== 'success' || !body.rates?.USD) throw new Error('bad rates payload');
      this.cached = { base: 'USD', rates: body.rates, source: 'live', updatedAt: new Date(this.now()).toISOString() };
      this.cachedAt = this.now();
      return this.cached;
    } catch {
      // Don't cache the fallback for long: retry the provider after 10 minutes.
      this.cached = { base: 'USD', rates: FALLBACK, source: 'fallback', updatedAt: new Date(this.now()).toISOString() };
      this.cachedAt = this.now() - TTL_MS + 10 * 60_000;
      return this.cached;
    }
  }
}
