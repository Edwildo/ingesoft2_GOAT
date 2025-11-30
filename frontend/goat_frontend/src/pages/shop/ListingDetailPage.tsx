import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { listingService } from '../../api/listing.service';
import { catalogService } from '../../api/catalog.service';
import { Card } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import { Loading } from '../../components/common/Loading';
import { Alert } from '../../components/common/Alert';
import { Listing } from '../../types/listing.types';
import { Sneaker } from '../../types/catalog.types';
import styles from './ListingDetailPage.module.css';

export const ListingDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [listing, setListing] = useState<Listing | null>(null);
  const [sneaker, setSneaker] = useState<Sneaker | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      loadListing();
    }
  }, [id]);

  useEffect(() => {
    if (listing?.sneakerSku) {
      loadSneakerInfo();
    }
  }, [listing?.sneakerSku]);

  const loadListing = async () => {
    if (!id) return;

    setIsLoading(true);
    setError(null);

    try {
      const response = await listingService.getListingById(id);
      
      if (response.success && response.data) {
        setListing(response.data);
      } else {
        setError(response.error?.message || 'Error al cargar el listing');
      }
    } catch (err) {
      setError('Error de conexión con el servidor');
    } finally {
      setIsLoading(false);
    }
  };

  const loadSneakerInfo = async () => {
    if (!listing?.sneakerSku) return;

    try {
      const response = await catalogService.getSneakerBySku(listing.sneakerSku);
      if (response.success && response.data) {
        setSneaker(response.data);
      }
      // No mostramos error si falla, es opcional
    } catch (err) {
      // Silencioso, la info del catalog es opcional
    }
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

  if (isLoading) {
    return (
      <div className={styles.detailPage}>
        <Loading message="Cargando detalles..." />
      </div>
    );
  }

  if (error || !listing) {
    return (
      <div className={styles.detailPage}>
        <div className={styles.container}>
          <Alert variant="error">
            {error || 'Listing no encontrado'}
          </Alert>
          <Button onClick={() => navigate('/shop')} variant="outline">
            Volver al Shop
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.detailPage}>
      <div className={styles.container}>
        <Button
          variant="outline"
          onClick={() => navigate('/shop')}
          className={styles.backButton}
        >
          ← Volver al Shop
        </Button>

        <div className={styles.content}>
          <div className={styles.imageSection}>
            {listing.coverImage ? (
              <img
                src={listing.coverImage}
                alt={`${listing.brand} ${listing.color}`}
                className={styles.mainImage}
                onError={(e) => {
                  (e.target as HTMLImageElement).src = '/placeholder-sneaker.png';
                }}
              />
            ) : (
              <div className={styles.placeholderImage}>
                <span>Sin imagen</span>
              </div>
            )}
            
            {sneaker?.media?.gallery && sneaker.media.gallery.length > 0 && (
              <div className={styles.gallery}>
                {sneaker.media.gallery.map((image, index) => (
                  <img
                    key={index}
                    src={image}
                    alt={`Vista ${index + 1}`}
                    className={styles.galleryImage}
                  />
                ))}
              </div>
            )}
          </div>

          <div className={styles.infoSection}>
            <Card className={styles.infoCard}>
              <h1 className={styles.brand}>{listing.brand}</h1>
              
              {sneaker && (
                <h2 className={styles.model}>{sneaker.model}</h2>
              )}

              <div className={styles.details}>
                <div className={styles.detailItem}>
                  <strong>Talla:</strong> {listing.size}
                </div>
                <div className={styles.detailItem}>
                  <strong>Condición:</strong> {getConditionLabel(listing.condition)}
                </div>
                <div className={styles.detailItem}>
                  <strong>Género:</strong> {listing.gender}
                </div>
                <div className={styles.detailItem}>
                  <strong>Color:</strong> {listing.color}
                </div>
              </div>

              {sneaker?.description && (
                <div className={styles.description}>
                  <h3>Descripción</h3>
                  <p>{sneaker.description}</p>
                </div>
              )}

              <div className={styles.priceSection}>
                <span className={styles.priceLabel}>Precio</span>
                <span className={styles.price}>{formatPrice(listing.price)}</span>
              </div>

              <Button
                variant="primary"
                size="large"
                fullWidth
                className={styles.buyButton}
                disabled
              >
                Comprar (Próximamente)
              </Button>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};

