import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { listingService } from '../../api/listing.service';
import { Button } from '../../components/common/Button';
import { Loading } from '../../components/common/Loading';
import { Alert } from '../../components/common/Alert';
import { Listing, ListingStatus, MyListingsFilters } from '../../types/listing.types';
import styles from './MyListingsPage.module.css';

export const MyListingsPage: React.FC = () => {
  const navigate = useNavigate();
  
  const [listings, setListings] = useState<Listing[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [filters, setFilters] = useState<MyListingsFilters>({
    page: 0,
    size: 20,
  });
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);

  useEffect(() => {
    loadListings();
  }, [filters]);

  const loadListings = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await listingService.getMyListings(filters);
      
      if (response.success && response.data) {
        setListings(response.data.listings);
        setTotal(response.data.total);
        setCurrentPage(response.data.page);
      } else {
        setError(response.error?.message || 'Error al cargar listings');
      }
    } catch (err) {
      setError('Error de conexión con el servidor');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStatusFilter = (status: ListingStatus | undefined) => {
    setFilters((prev) => ({ ...prev, status, page: 0 }));
  };

  const handlePublish = async (id: string) => {
    try {
      const response = await listingService.publishListing(id);
      if (response.success) {
        setMessage({ type: 'success', text: 'Listing publicado exitosamente' });
        loadListings();
      } else {
        setMessage({ type: 'error', text: response.error?.message || 'Error al publicar listing' });
      }
    } catch (err) {
      setMessage({ type: 'error', text: 'Error de conexión con el servidor' });
    }
  };

  const handleArchive = async (id: string) => {
    try {
      const response = await listingService.archiveListing(id);
      if (response.success) {
        setMessage({ type: 'success', text: 'Listing archivado exitosamente' });
        loadListings();
      } else {
        setMessage({ type: 'error', text: response.error?.message || 'Error al archivar listing' });
      }
    } catch (err) {
      setMessage({ type: 'error', text: 'Error de conexión con el servidor' });
    }
  };

  const formatPrice = (price: number): string => {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(price);
  };

  const getStatusLabel = (status: ListingStatus): string => {
    const labels: Record<ListingStatus, string> = {
      DRAFT: 'Borrador',
      PUBLISHED: 'Publicado',
      ARCHIVED: 'Archivado',
    };
    return labels[status];
  };

  const getStatusClass = (status: ListingStatus): string => {
    return styles[`status${status}`];
  };

  return (
    <div className={styles.myListingsPage}>
      <div className={styles.container}>
        <header className={styles.header}>
          <h1 className={styles.title}>Mis Listings</h1>
          <Button
            variant="primary"
            onClick={() => navigate('/seller/listings/new')}
          >
            + Nuevo Listing
          </Button>
        </header>

        {message && (
          <Alert
            variant={message.type === 'error' ? 'error' : 'success'}
            onClose={() => setMessage(null)}
          >
            {message.text}
          </Alert>
        )}

        {error && (
          <Alert variant="error" onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        <div className={styles.filters}>
          <button
            className={`${styles.filterButton} ${!filters.status ? styles.active : ''}`}
            onClick={() => handleStatusFilter(undefined)}
          >
            Todos
          </button>
          <button
            className={`${styles.filterButton} ${filters.status === 'DRAFT' ? styles.active : ''}`}
            onClick={() => handleStatusFilter('DRAFT')}
          >
            Borradores
          </button>
          <button
            className={`${styles.filterButton} ${filters.status === 'PUBLISHED' ? styles.active : ''}`}
            onClick={() => handleStatusFilter('PUBLISHED')}
          >
            Publicados
          </button>
          <button
            className={`${styles.filterButton} ${filters.status === 'ARCHIVED' ? styles.active : ''}`}
            onClick={() => handleStatusFilter('ARCHIVED')}
          >
            Archivados
          </button>
        </div>

        {isLoading ? (
          <Loading message="Cargando listings..." />
        ) : listings.length === 0 ? (
          <div className={styles.emptyState}>
            <p>No tienes listings {filters.status ? `con estado ${getStatusLabel(filters.status)}` : ''}.</p>
            <Button
              variant="primary"
              onClick={() => navigate('/seller/listings/new')}
            >
              Crear tu primer listing
            </Button>
          </div>
        ) : (
          <>
            <div className={styles.listingsTable}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>Imagen</th>
                    <th>Brand</th>
                    <th>Talla</th>
                    <th>Condición</th>
                    <th>Precio</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {listings.map((listing) => (
                    <tr key={listing.id}>
                      <td>
                        {listing.coverImage ? (
                          <img
                            src={listing.coverImage}
                            alt={listing.brand}
                            className={styles.thumbnail}
                            onError={(e) => {
                              (e.target as HTMLImageElement).src = '/placeholder-sneaker.png';
                            }}
                          />
                        ) : (
                          <div className={styles.thumbnailPlaceholder}>Sin imagen</div>
                        )}
                      </td>
                      <td>{listing.brand}</td>
                      <td>{listing.size}</td>
                      <td>{listing.condition}</td>
                      <td>{formatPrice(listing.price)}</td>
                      <td>
                        <span className={`${styles.statusBadge} ${getStatusClass(listing.status)}`}>
                          {getStatusLabel(listing.status)}
                        </span>
                      </td>
                      <td>
                        <div className={styles.actions}>
                          {listing.status === 'DRAFT' && (
                            <>
                              <Button
                                variant="outline"
                                size="small"
                                onClick={() => navigate(`/seller/listings/${listing.id}/edit`)}
                              >
                                Editar
                              </Button>
                              <Button
                                variant="primary"
                                size="small"
                                onClick={() => handlePublish(listing.id)}
                              >
                                Publicar
                              </Button>
                            </>
                          )}
                          {listing.status === 'PUBLISHED' && (
                            <Button
                              variant="outline"
                              size="small"
                              onClick={() => handleArchive(listing.id)}
                            >
                              Archivar
                            </Button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {Math.ceil(total / (filters.size || 20)) > 1 && (
              <div className={styles.pagination}>
                <button
                  className={styles.paginationButton}
                  onClick={() => setFilters((prev) => ({ ...prev, page: currentPage - 1 }))}
                  disabled={currentPage === 0}
                >
                  Anterior
                </button>
                
                <span className={styles.paginationInfo}>
                  Página {currentPage + 1} de {Math.ceil(total / (filters.size || 20))}
                </span>
                
                <button
                  className={styles.paginationButton}
                  onClick={() => setFilters((prev) => ({ ...prev, page: currentPage + 1 }))}
                  disabled={currentPage >= Math.ceil(total / (filters.size || 20)) - 1}
                >
                  Siguiente
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

