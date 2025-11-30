import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Listing } from '../../../types/listing.types';
import { Card } from '../../common/Card';
import styles from './ListingCard.module.css';

export interface ListingCardProps {
  listing: Listing;
}

export const ListingCard: React.FC<ListingCardProps> = ({ listing }) => {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/shop/${listing.id}`);
  };

  const formatPrice = (price: number): string => {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(price);
  };

  const getConditionLabel = (condition: string): string => {
    const labels: Record<string, string> = {
      NEW: 'Nuevo',
      LIKE_NEW: 'Como nuevo',
      USED: 'Usado',
      FAIR: 'Regular',
    };
    return labels[condition] || condition;
  };

  return (
    <Card className={styles.listingCard} onClick={handleClick}>
      <div className={styles.imageContainer}>
        {listing.coverImage ? (
          <img
            src={listing.coverImage}
            alt={`${listing.brand} ${listing.color}`}
            className={styles.image}
            onError={(e) => {
              (e.target as HTMLImageElement).src = '/placeholder-sneaker.png';
            }}
          />
        ) : (
          <div className={styles.placeholderImage}>
            <span>Sin imagen</span>
          </div>
        )}
      </div>
      
      <div className={styles.content}>
        <h3 className={styles.brand}>{listing.brand}</h3>
        <p className={styles.details}>
          Talla: <strong>{listing.size}</strong> • {getConditionLabel(listing.condition)}
        </p>
        <p className={styles.color}>Color: {listing.color}</p>
        <div className={styles.priceContainer}>
          <span className={styles.price}>{formatPrice(listing.price)}</span>
        </div>
      </div>
    </Card>
  );
};

