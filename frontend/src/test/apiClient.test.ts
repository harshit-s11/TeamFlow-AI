import { describe, it, expect } from 'vitest';
import { parseApiError } from '../api/apiClient';

describe('apiClient Error Parser', () => {
  it('parses standard Error object', () => {
    const error = new Error('Network error');
    expect(parseApiError(error)).toBe('Network error');
  });

  it('returns default fallback message for unknown errors', () => {
    expect(parseApiError(null)).toBe('An unexpected error occurred. Please try again.');
  });
});
