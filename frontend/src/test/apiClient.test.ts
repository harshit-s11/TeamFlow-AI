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

  it('parses Axios error with top-level message', () => {
    const axiosError = {
      isAxiosError: true,
      response: {
        data: {
          status: 400,
          error: 'Bad Request',
          message: 'Invalid credentials',
        },
      },
    };
    expect(parseApiError(axiosError)).toBe('Invalid credentials');
  });

  it('surfaces field-level validation messages when fieldErrors array is present', () => {
    const axiosError = {
      isAxiosError: true,
      response: {
        data: {
          status: 400,
          error: 'Bad Request',
          message: 'Validation failed for request payload',
          fieldErrors: [
            { field: 'password', message: 'Password must be between 8 and 100 characters' },
          ],
        },
      },
    };
    expect(parseApiError(axiosError)).toBe('Password must be between 8 and 100 characters');
  });
});
