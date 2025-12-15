import { apiClient, handleApiError } from "./api.config";
import { ApiResponse } from "../types/api.types";

export interface ShippingAddress {
  street: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
}

export interface CreateOrderRequest {
  shippingAddress: ShippingAddress;
  shippingMethod: "STANDARD" | "EXPRESS";
}

export interface OrderItem {
  id: string;
  listingId: string;
  price: number;
  sneakerSku: string;
  size: string;
  brand: string;
  color: string;
  condition: string;
  coverImage?: string;
}

export interface Order {
  id: string;
  buyerId: string;
  cartId: string;
  status: "PENDING" | "PAYMENT_PENDING" | "CONFIRMED" | "SHIPPING" | "DELIVERED" | "CANCELLED";
  totalAmount: number;
  shippingAddress: ShippingAddress;
  shippingMethod: "STANDARD" | "EXPRESS";
  paymentId?: string;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface OrderStatusHistory {
  id: string;
  orderId: string;
  status: string;
  changedAt: string;
  changedBy?: string;
  notes?: string;
}

export const orderService = {
  createOrder: async (request: CreateOrderRequest): Promise<ApiResponse<Order>> => {
    try {
      const response = await apiClient.post<Order>("/api/orders", request);
      return { success: true, data: response.data };
    } catch (error) {
      return { success: false, error: handleApiError(error) };
    }
  },

  getMyOrders: async (): Promise<ApiResponse<Order[]>> => {
    try {
      const response = await apiClient.get<Order[]>("/api/orders");
      return { success: true, data: response.data };
    } catch (error) {
      return { success: false, error: handleApiError(error) };
    }
  },

  getOrder: async (orderId: string): Promise<ApiResponse<Order>> => {
    try {
      const response = await apiClient.get<Order>(`/api/orders/${orderId}`);
      return { success: true, data: response.data };
    } catch (error) {
      return { success: false, error: handleApiError(error) };
    }
  },

  getOrderHistory: async (orderId: string): Promise<ApiResponse<OrderStatusHistory[]>> => {
    try {
      const response = await apiClient.get<OrderStatusHistory[]>(`/api/orders/${orderId}/history`);
      return { success: true, data: response.data };
    } catch (error) {
      return { success: false, error: handleApiError(error) };
    }
  },
};

