/**
 * Error codes shared with the Android client.
 * Keep in sync with android/core/network/.../dto/ApiErrorDto.kt (ApiErrorCode).
 */
export const ErrorCode = {
  VALIDATION_FAILED: 'VALIDATION_FAILED',
  UNAUTHORIZED: 'UNAUTHORIZED',
  FORBIDDEN: 'FORBIDDEN',
  NOT_FOUND: 'NOT_FOUND',
  CONFLICT: 'CONFLICT',
  RATE_LIMITED: 'RATE_LIMITED',
  AI_UNAVAILABLE: 'AI_UNAVAILABLE',
  AI_QUOTA_EXCEEDED: 'AI_QUOTA_EXCEEDED',
  AI_INVALID_OUTPUT: 'AI_INVALID_OUTPUT',
  INTERNAL: 'INTERNAL',
} as const;
export type ErrorCode = (typeof ErrorCode)[keyof typeof ErrorCode];

/** Response envelope for every error: `{ error: { code, message, details?, serverVersion? } }`. */
export interface ApiErrorBody {
  error: { code: ErrorCode; message: string; details?: Record<string, string>; serverVersion?: number };
}

/** Throw from routes/services; the error middleware turns it into the envelope. */
export class HttpError extends Error {
  constructor(
    readonly status: number,
    readonly code: ErrorCode,
    message: string,
    readonly extra: { details?: Record<string, string>; serverVersion?: number } = {},
  ) {
    super(message);
    this.name = 'HttpError';
  }

  static validation(details: Record<string, string>) {
    return new HttpError(400, ErrorCode.VALIDATION_FAILED, 'Request validation failed', { details });
  }
  static notFound(message = 'Not found') {
    return new HttpError(404, ErrorCode.NOT_FOUND, message);
  }
  static unauthorized(message = 'Authentication required') {
    return new HttpError(401, ErrorCode.UNAUTHORIZED, message);
  }
  static forbidden(message = 'Forbidden') {
    return new HttpError(403, ErrorCode.FORBIDDEN, message);
  }
  static conflict(serverVersion: number) {
    return new HttpError(409, ErrorCode.CONFLICT, 'Version conflict', { serverVersion });
  }

  toBody(): ApiErrorBody {
    return { error: { code: this.code, message: this.message, ...this.extra } };
  }
}
