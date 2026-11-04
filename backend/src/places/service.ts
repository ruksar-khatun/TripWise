import { HttpError } from '../http/errors.js';

export interface NearbyPlace {
  id: string;
  name: string;
  location: { lat: number; lng: number };
  rating: number | null;
  ratingCount: number | null;
  category: string | null;
  address: string | null;
  googleMapsUri: string | null;
}

type Fetch = typeof fetch;

const FIELD_MASK = [
  'places.id', 'places.displayName', 'places.location', 'places.rating', 'places.userRatingCount',
  'places.primaryTypeDisplayName', 'places.shortFormattedAddress', 'places.googleMapsUri',
].join(',');

const TTL_MS = 15 * 60_000;
const MAX_ENTRIES = 500;

/**
 * Google Places API (New) "Nearby search" behind our backend: the key stays server-side, the field
 * mask keeps billing to the fields we show, and results are cached per ~100 m cell for 15 minutes.
 */
export class PlacesService {
  private readonly cache = new Map<string, { at: number; places: NearbyPlace[] }>();

  constructor(
    private readonly apiKey: string | undefined,
    private readonly fetchImpl: Fetch = fetch,
    private readonly now: () => number = Date.now,
  ) {}

  get configured() {
    return Boolean(this.apiKey);
  }

  async nearby(lat: number, lng: number, radiusMeters: number): Promise<NearbyPlace[]> {
    if (!this.apiKey) throw HttpError.notConfigured('Google Places');
    const key = `${lat.toFixed(3)},${lng.toFixed(3)},${radiusMeters}`;
    const hit = this.cache.get(key);
    if (hit && this.now() - hit.at < TTL_MS) return hit.places;

    const res = await this.fetchImpl('https://places.googleapis.com/v1/places:searchNearby', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-Goog-Api-Key': this.apiKey, 'X-Goog-FieldMask': FIELD_MASK },
      body: JSON.stringify({
        includedTypes: ['tourist_attraction', 'museum', 'park', 'historical_landmark'],
        maxResultCount: 15,
        rankPreference: 'POPULARITY',
        locationRestriction: { circle: { center: { latitude: lat, longitude: lng }, radius: radiusMeters } },
      }),
      signal: AbortSignal.timeout(10_000),
    });
    if (!res.ok) throw HttpError.upstream(`Places request failed (${res.status})`);
    const body = (await res.json()) as { places?: RawPlace[] };
    const places = (body.places ?? []).map(toNearbyPlace).filter((p): p is NearbyPlace => p !== null);

    if (this.cache.size >= MAX_ENTRIES) this.cache.delete(this.cache.keys().next().value!);
    this.cache.set(key, { at: this.now(), places });
    return places;
  }
}

interface RawPlace {
  id?: string;
  displayName?: { text?: string };
  location?: { latitude?: number; longitude?: number };
  rating?: number;
  userRatingCount?: number;
  primaryTypeDisplayName?: { text?: string };
  shortFormattedAddress?: string;
  googleMapsUri?: string;
}

function toNearbyPlace(p: RawPlace): NearbyPlace | null {
  if (!p.id || !p.displayName?.text || p.location?.latitude == null || p.location.longitude == null) return null;
  return {
    id: p.id,
    name: p.displayName.text,
    location: { lat: p.location.latitude, lng: p.location.longitude },
    rating: p.rating ?? null,
    ratingCount: p.userRatingCount ?? null,
    category: p.primaryTypeDisplayName?.text ?? null,
    address: p.shortFormattedAddress ?? null,
    googleMapsUri: p.googleMapsUri ?? null,
  };
}
