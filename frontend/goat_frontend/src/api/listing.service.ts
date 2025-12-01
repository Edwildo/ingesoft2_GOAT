import { apiClient, handleApiError } from "./api.config";
import {
  Listing,
  ListingFilters,
  ListingsResponse,
  CreateListingRequest,
  UpdateListingRequest,
  MyListingsFilters,
} from "../types/listing.types";
import { ApiResponse } from "../types/api.types";

export const listingService = {
  /**
   * Obtiene listings públicos para el shop
   */
  getListings: async (
    filters?: ListingFilters
  ): Promise<ApiResponse<ListingsResponse>> => {
    try {
      const params = new URLSearchParams();

      if (filters?.brand) params.append("brand", filters.brand);
      if (filters?.size) params.append("size", filters.size);
      if (filters?.condition) params.append("condition", filters.condition);
      if (filters?.gender) params.append("gender", filters.gender);
      if (filters?.color) params.append("color", filters.color);
      if (filters?.minPrice !== undefined)
        params.append("minPrice", filters.minPrice.toString());
      if (filters?.maxPrice !== undefined)
        params.append("maxPrice", filters.maxPrice.toString());
      if (filters?.page !== undefined)
        params.append("page", filters.page.toString());
      if (filters?.pageSize !== undefined)
        params.append("pageSize", filters.pageSize.toString());

      const queryString = params.toString();
      const url = `/api/listings${queryString ? `?${queryString}` : ""}`;

      const response = await apiClient.get<ListingsResponse>(url);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: handleApiError(error),
      };
    }
  },

  /**
   * Obtiene el detalle de un listing por ID
   */
  getListingById: async (id: string): Promise<ApiResponse<Listing>> => {
    try {
      const response = await apiClient.get<Listing>(`/api/listings/${id}`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: handleApiError(error),
      };
    }
  },

  /**
   * Obtiene los listings del seller autenticado
   */
  getMyListings: async (
    filters?: MyListingsFilters
  ): Promise<ApiResponse<ListingsResponse>> => {
    try {
      const params = new URLSearchParams();

      if (filters?.status) params.append("status", filters.status);
      if (filters?.page !== undefined)
        params.append("page", filters.page.toString());
      if (filters?.pageSize !== undefined)
        params.append("pageSize", filters.pageSize.toString());

      const queryString = params.toString();
      const url = `/api/listings/mine${queryString ? `?${queryString}` : ""}`;

      const response = await apiClient.get<ListingsResponse>(url);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: handleApiError(error),
      };
    }
  },

  /**
   * Crea un nuevo listing (requiere autenticación + rol SELLER)
   */
  createListing: async (
    data: CreateListingRequest
  ): Promise<ApiResponse<Listing>> => {
    try {
      const response = await apiClient.post<Listing>("/api/listings", data);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: handleApiError(error),
      };
    }
  },

  /**
   * Actualiza un listing existente (solo DRAFT, requiere autenticación + rol SELLER + owner)
   */
  updateListing: async (
    id: string,
    data: UpdateListingRequest
  ): Promise<ApiResponse<Listing>> => {
    try {
      const response = await apiClient.put<Listing>(
        `/api/listings/${id}`,
        data
      );
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: handleApiError(error),
      };
    }
  },

  /**
   * Publica un listing (requiere autenticación + rol SELLER + owner)
   */
  publishListing: async (id: string): Promise<ApiResponse<Listing>> => {
    try {
      const response = await apiClient.put<Listing>(
        `/api/listings/${id}/publish`
      );
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: handleApiError(error),
      };
    }
  },

  /**
   * Archiva un listing (requiere autenticación + rol SELLER + owner)
   */
  archiveListing: async (id: string): Promise<ApiResponse<Listing>> => {
    try {
      const response = await apiClient.put<Listing>(
        `/api/listings/${id}/archive`
      );
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: handleApiError(error),
      };
    }
  },
};
