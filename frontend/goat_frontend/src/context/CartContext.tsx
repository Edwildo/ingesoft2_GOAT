import React, { createContext, useCallback, useContext, useEffect, useState } from "react";
import { cartService } from "../api/cart.service";
import { Cart } from "../types/cart.types";
import { ApiError } from "../types/api.types";
import { useAuth } from "./AuthContext";

interface CartContextType {
  cart: Cart | null;
  isLoading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
  addItem: (listingId: string) => Promise<{ success: boolean; message?: string }>;
  removeItem: (itemId: string) => Promise<{ success: boolean; message?: string }>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

const mapCartError = (apiError?: ApiError): string => {
  if (!apiError) return "Error de conexión con el servidor";
  
  // Mapear por status HTTP primero
  if (apiError.status === 404) {
    if (apiError.message?.toLowerCase().includes("cart")) {
      return "Carrito no encontrado";
    }
    if (apiError.message?.toLowerCase().includes("item")) {
      return "Artículo no encontrado";
    }
    if (apiError.message?.toLowerCase().includes("listing")) {
      return "El listing no existe";
    }
    return "Recurso no encontrado";
  }
  
  if (apiError.status === 409) {
    return "El artículo ya está en el carrito";
  }
  
  if (apiError.status === 403) {
    const msg = apiError.message?.toLowerCase() || "";
    if (msg.includes("own") || msg.includes("propio")) {
      return "No puedes agregar un listing propio";
    }
    if (msg.includes("available")) {
      return "El listing no está disponible";
    }
    if (msg.includes("published")) {
      return "El listing no ha sido publicado";
    }
    return "No tienes permisos para esta acción";
  }
  
  if (apiError.status === 400) {
    return apiError.message || "El listing no está disponible";
  }
  
  if (apiError.status === 401) {
    return "Debe iniciar sesión para agregar items al carrito";
  }
  
  return apiError.message || "Ocurrió un error al procesar la solicitud";
};

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState<Cart | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadCart = useCallback(async () => {
    if (!isAuthenticated) {
      setCart(null);
      setError(null);
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const response = await cartService.getCart();
      if (response.success && response.data) {
        setCart(response.data);
      } else {
        setError(mapCartError(response.error));
      }
    } catch (err) {
      void err;
      setError("Error de conexión con el servidor");
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    void loadCart();
  }, [loadCart]);

  const addItem = useCallback(
    async (listingId: string): Promise<{ success: boolean; message?: string }> => {
      setIsLoading(true);
      setError(null);
      try {
        const response = await cartService.addItem(listingId);
        if (response.success && response.data) {
          setCart(response.data);
          return { success: true };
        }
        const message = mapCartError(response.error);
        setError(message);
        return { success: false, message };
      } catch (err) {
        void err;
        const message = "Error de conexión con el servidor";
        setError(message);
        return { success: false, message };
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

  const removeItem = useCallback(
    async (itemId: string): Promise<{ success: boolean; message?: string }> => {
      setIsLoading(true);
      setError(null);
      try {
        const response = await cartService.removeItem(itemId);
        if (response.success && response.data) {
          setCart(response.data);
          return { success: true };
        }
        const message = mapCartError(response.error);
        setError(message);
        return { success: false, message };
      } catch (err) {
        void err;
        const message = "Error de conexión con el servidor";
        setError(message);
        return { success: false, message };
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

  const value: CartContextType = {
    cart,
    isLoading,
    error,
    refresh: loadCart,
    addItem,
    removeItem,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

export const useCart = (): CartContextType => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart debe ser usado dentro de un CartProvider");
  }
  return context;
};
