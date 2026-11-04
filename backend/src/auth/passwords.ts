import { hash, verify } from '@node-rs/argon2';

// Argon2id with OWASP-recommended minimums (19 MiB memory, 2 iterations, 1 lane).
const OPTIONS = { memoryCost: 19_456, timeCost: 2, parallelism: 1 } as const;

export const hashPassword = (password: string) => hash(password, OPTIONS);

export async function verifyPassword(passwordHash: string, password: string): Promise<boolean> {
  try {
    return await verify(passwordHash, password);
  } catch {
    return false;
  }
}

/**
 * Precomputed hash used when an email is unknown, so login takes the same time whether or not
 * the account exists (prevents account enumeration by timing).
 */
let dummyHash: Promise<string> | undefined;
export const timingSafeDummyVerify = async (password: string) => {
  dummyHash ??= hashPassword('tripwise-dummy-password');
  await verifyPassword(await dummyHash, password);
};
