import React, { createContext, useContext, useState, useEffect } from 'react';
import { UserResponse } from '../types/user.types';
import { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth.types';
import { authApi } from '../api/authApi';
import { tokenStorage } from '../utils/tokenStorage';

interface AuthContextType {
  user: UserResponse | null;
  token: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isLoading: boolean;
  login: (request: LoginRequest) => Promise<AuthResponse>;
  register: (request: RegisterRequest) => Promise<AuthResponse>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const storedToken = tokenStorage.getToken();
    const storedUser = tokenStorage.getUser();

    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(storedUser);
    } else {
      tokenStorage.clearToken();
    }
    setIsLoading(false);
  }, []);

  const handleAuthSuccess = (response: AuthResponse): AuthResponse => {
    setToken(response.token);
    setUser(response.user);
    tokenStorage.setToken(response.token);
    tokenStorage.setUser(response.user);
    return response;
  };

  const login = async (request: LoginRequest): Promise<AuthResponse> => {
    const response = await authApi.login(request);
    return handleAuthSuccess(response);
  };

  const register = async (request: RegisterRequest): Promise<AuthResponse> => {
    const response = await authApi.register(request);
    return handleAuthSuccess(response);
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    tokenStorage.clearToken();
  };

  const isAuthenticated = Boolean(token && user);
  const isAdmin = user?.role === 'ADMIN';

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated,
        isAdmin,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
