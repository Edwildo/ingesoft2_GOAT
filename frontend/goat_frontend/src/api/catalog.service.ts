import { apiClient, handleApiError } from './api.config';
import {
  Sneaker,
  SneakersResponse,
  SneakersSearchParams,
  BrandsResponse,
  CategoriesResponse,
  CreateSneakerRequest,
} from '../types/catalog.types';
import { ApiResponse } from '../types/api.types';

export const catalogService = {
  /**
   * Busca sneakers para autocompletado
   */
  searchSneakers: async (params?: SneakersSearchParams): Promise<ApiResponse<SneakersResponse>> => {
    try {
      const queryParams = new URLSearchParams();
      
      if (params?.search) queryParams.append('search', params.search);
      if (params?.brand) queryParams.append('brand', params.brand);
      if (params?.page !== undefined) queryParams.append('page', params.page.toString());
      if (params?.size !== undefined) queryParams.append('size', params.size.toString());

      const queryString = queryParams.toString();
      const url = `/api/catalog/sneakers${queryString ? `?${queryString}` : ''}`;
      
      const response = await apiClient.get<SneakersResponse>(url);
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
   * Obtiene información completa de un sneaker por SKU
   */
  getSneakerBySku: async (sku: string): Promise<ApiResponse<Sneaker>> => {
    try {
      const response = await apiClient.get<Sneaker>(`/api/catalog/sneakers/${sku}`);
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
   * Crea un nuevo sneaker en el catálogo (requiere autenticación JWT)
   */
  createSneaker: async (data: CreateSneakerRequest): Promise<ApiResponse<Sneaker>> => {
    try {
      const response = await apiClient.post<Sneaker>('/api/catalog/sneakers', data);
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
   * Obtiene lista de marcas para filtros
   */
  getBrands: async (): Promise<ApiResponse<BrandsResponse>> => {
    try {
      const response = await apiClient.get<BrandsResponse>('/api/catalog/brands');
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
   * Obtiene lista de categorías para filtros
   */
  getCategories: async (): Promise<ApiResponse<CategoriesResponse>> => {
    try {
      const response = await apiClient.get<CategoriesResponse>('/api/catalog/categories');
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

