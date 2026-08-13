import { describe, it, expect, beforeEach } from 'vitest';
import { tokenStorage } from '../utils/tokenStorage';
import { UserResponse } from '../types/user.types';

describe('tokenStorage Utility', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('stores and retrieves JWT token', () => {
    expect(tokenStorage.getToken()).toBeNull();
    tokenStorage.setToken('sample-jwt-token');
    expect(tokenStorage.getToken()).toBe('sample-jwt-token');
  });

  it('stores and retrieves user object', () => {
    const user: UserResponse = {
      id: '123e4567-e89b-12d3-a456-426614174000',
      name: 'Alice',
      email: 'alice@teamflow.com',
      role: 'USER',
      createdAt: '2026-08-14T00:00:00Z',
    };

    tokenStorage.setUser(user);
    expect(tokenStorage.getUser()).toEqual(user);
  });

  it('clears session tokens and user data', () => {
    tokenStorage.setToken('token');
    tokenStorage.setUser({
      id: '1',
      name: 'Bob',
      email: 'bob@teamflow.com',
      role: 'ADMIN',
      createdAt: '2026-08-14T00:00:00Z',
    });

    expect(tokenStorage.hasValidSession()).toBe(true);
    tokenStorage.clearToken();
    expect(tokenStorage.getToken()).toBeNull();
    expect(tokenStorage.getUser()).toBeNull();
    expect(tokenStorage.hasValidSession()).toBe(false);
  });
});
