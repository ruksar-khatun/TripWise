import { z } from 'zod';

const LatLng = z.object({ lat: z.number().min(-90).max(90), lng: z.number().min(-180).max(180) });
export const Interest = z.enum(['BEACHES', 'CULTURE', 'FOOD', 'NATURE', 'MOUNTAINS', 'CITY', 'ADVENTURE']);
export const ActivityCategory = z.enum(['SIGHTSEEING', 'FOOD', 'ACTIVITY', 'TRANSPORT', 'LODGING', 'SHOPPING', 'OTHER']);
export const CostCategory = z.enum(['ACCOMMODATION', 'FOOD', 'LOCAL_TRAVEL', 'ACTIVITIES', 'SHOPPING']);

export const CatalogImage = z.object({
  url: z.string().url(),
  author: z.string(),
  sourceUrl: z.string().url(),
  license: z.string(),
});

export const Attraction = z.object({
  id: z.string(),
  name: z.string(),
  category: ActivityCategory,
  location: LatLng,
  durationMinutes: z.number().int().positive(),
  estimatedCostUsd: z.number().nonnegative(),
  description: z.string(),
});

/** Validated shape of every entry in catalog/destinations.json. */
export const CatalogDestination = z.object({
  id: z.string().regex(/^[a-z0-9-]+$/),
  name: z.string(),
  country: z.string(),
  region: z.string(),
  summary: z.string(),
  highlights: z.array(z.string()),
  localFood: z.array(z.string()),
  interests: z.array(Interest).min(1),
  location: LatLng,
  rating: z.number().min(0).max(5),
  ratingCount: z.number().int().nonnegative(),
  bestTimeToVisit: z.string(),
  currency: z.string().regex(/^[A-Z]{3}$/),
  languages: z.array(z.string()),
  featured: z.boolean(),
  popularity: z.number().int(),
  images: z.array(CatalogImage).min(1),
  attractions: z.array(Attraction),
  /** Typical mid-range spend per person per day, USD. Used by expense estimation. */
  dailyCostUsd: z.record(CostCategory, z.number().nonnegative()),
});
export type CatalogDestination = z.infer<typeof CatalogDestination>;
