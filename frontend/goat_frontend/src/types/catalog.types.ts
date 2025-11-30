export interface Sneaker {
  sku: string;
  brand: string;
  model: string;
  gender: 'MALE' | 'FEMALE' | 'UNISEX';
  description?: string;
  categories?: string[];
  collections?: string[];
  media?: {
    coverImage?: string;
    gallery?: string[];
  };
}

export interface SneakersResponse {
  sneakers: Sneaker[];
  total: number;
  page: number;
  size: number;
}

export interface SneakersSearchParams {
  search?: string;
  brand?: string;
  page?: number;
  size?: number;
}

export interface Brand {
  id: string;
  name: string;
  slug: string;
  logoUrl?: string;
}

export interface BrandsResponse {
  brands: Brand[];
}

export interface Category {
  id: string;
  name: string;
  slug: string;
}

export interface CategoriesResponse {
  categories: Category[];
}

export interface CreateSneakerRequest {
  sku: string;
  brand: string;
  model: string;
  gender: 'MALE' | 'FEMALE' | 'UNISEX';
  description?: string;
  categories?: string[];
  collections?: string[];
  media?: {
    coverImage?: string;
    gallery?: string[];
  };
}

