import { apiClient, handleApiError } from "./api.config";
import { Cart } from "../types/cart.types";
import { ApiResponse } from "../types/api.types";

export const cartService = {
  getCart: async (): Promise<ApiResponse<Cart>> => {
    try {
      const response = await apiClient.get<Cart>("/api/cart");
      return { success: true, data: response.data };
    } catch (error) {
      return { success: false, error: handleApiError(error) };
    }
  },

  addItem: async (listingId: string): Promise<ApiResponse<Cart>> => {
    try {
      const response = await apiClient.post<Cart>("/api/cart/items", { listingId });
      return { success: true, data: response.data };
    } catch (error) {
      return { success: false, error: handleApiError(error) };
    }
  },

  removeItem: async (itemId: string): Promise<ApiResponse<Cart>> => {
    try {
      const response = await apiClient.delete<Cart>(`/api/cart/items/${itemId}`);
      return { success: true, data: response.data };
    } catch (error) {
      return { success: false, error: handleApiError(error) };
    }
  },
};
