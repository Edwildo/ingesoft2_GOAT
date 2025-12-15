export interface CartItem {
  id: string;
  listingId: string;
  sneakerSku: string;
  brand: string;
  color: string;
  size: string;
  condition: string;
  price: number;
  coverImage?: string;
  createdAt: string;
}

export interface Cart {
  id: string;
  userId: string;
  items: CartItem[];
  total: number;
  updatedAt?: string;
}
