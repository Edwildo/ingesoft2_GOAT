export type ListingStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type Condition = 'NEW' | 'LIKE_NEW' | 'USED' | 'FAIR';
export type Gender = 'MALE' | 'FEMALE' | 'UNISEX';

export interface Listing {
  id: string;
  sellerId: string;
  sneakerSku: string;
  size: string;
  condition: Condition;
  gender: Gender;
  brand: string;
  color: string;
  price: number;
  status: ListingStatus;
  coverImage: string;
  createdAt: string;
  updatedAt?: string;
}

export interface ListingFilters {
  brand?: string;
  size?: string;
  condition?: Condition;
  gender?: Gender;
  color?: string;
  minPrice?: number;
  maxPrice?: number;
  page?: number;
  pageSize?: number;
}

export interface ListingsResponse {
  listings: Listing[];
  total: number;
  page: number;
  size: number;
}

export interface CreateListingRequest {
  sneakerSku: string;
  size: string;
  condition: Condition;
  gender: Gender;
  brand: string;
  color: string;
  price: number;
  coverImage: string;
}

export interface UpdateListingRequest extends Partial<CreateListingRequest> {}

export interface MyListingsFilters {
  status?: ListingStatus;
  page?: number;
  size?: number;
}

