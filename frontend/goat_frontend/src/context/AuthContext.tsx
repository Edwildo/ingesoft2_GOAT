import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
} from "react";
import { authService } from "../api/auth.service";
import { authStorage } from "../utils/storage";
import { User, RegisterRequest, AuthState } from "../types/auth.types";
import { ApiResponse } from "../types/api.types";

interface AuthContextType extends AuthState {
  login: (
    email: string,
    password: string
  ) => Promise<{ success: boolean; message: string }>;
  register: (
    email: string,
    password: string,
    roles?: string[]
  ) => Promise<{ success: boolean; message: string; userId?: string }>;
  logout: () => void;
  checkAuth: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const checkAuth = useCallback(() => {
    const storedToken = authStorage.getToken();
    const storedUser = authStorage.getUser();

    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(storedUser);
      setIsAuthenticated(true);
    } else {
      setToken(null);
      setUser(null);
      setIsAuthenticated(false);
    }
    setIsLoading(false);
  }, []);

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  const login = useCallback(
    async (
      email: string,
      password: string
    ): Promise<{ success: boolean; message: string }> => {
      try {
        const response: ApiResponse<{
          token: string;
          email: string;
          roles: string[];
          userId: string;
        }> = await authService.login({ email, password });

        if (response.success && response.data) {
          const {
            token: newToken,
            userId,
            email: userEmail,
            roles,
          } = response.data;

          authStorage.setToken(newToken);

          const userData: User = {
            id: userId,
            email: userEmail,
            emailConfirmed: true, // Si logueó, el email está confirmado
            isActive: true,
            roles,
          };

          authStorage.setUser(userData);
          setToken(newToken);
          setUser(userData);
          setIsAuthenticated(true);

          return { success: true, message: "Login exitoso" };
        } else {
          const errorMessage =
            response.error?.message || "Error al iniciar sesión";
          return { success: false, message: errorMessage };
        }
      } catch (error) {
        console.error("Login error:", error);
        return { success: false, message: "Error de conexión con el servidor" };
      }
    },
    []
  );
  const register = useCallback(
    async (
      email: string,
      password: string,
      roles?: string[]
    ): Promise<{ success: boolean; message: string; userId?: string }> => {
      try {
        const registerData: RegisterRequest = { email, password };
        if (roles && roles.length > 0) {
          registerData.roles = roles;
        }

        const response: ApiResponse<{
          id: string;
          email: string;
          emailConfirmed: boolean;
          isActive: boolean;
        }> = await authService.register(registerData);

        if (response.success && response.data) {
          return {
            success: true,
            message: "Usuario registrado exitosamente",
            userId: response.data.id,
          };
        } else {
          const errorMessage =
            response.error?.message || "Error al registrar usuario";
          return { success: false, message: errorMessage };
        }
      } catch (error) {
        console.error("Register error:", error);
        return { success: false, message: "Error de conexión con el servidor" };
      }
    },
    []
  );

  const logout = useCallback(() => {
    authStorage.clear();
    setToken(null);
    setUser(null);
    setIsAuthenticated(false);
  }, []);

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated,
    isLoading,
    login,
    register,
    logout,
    checkAuth,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth debe ser usado dentro de un AuthProvider");
  }
  return context;
};
