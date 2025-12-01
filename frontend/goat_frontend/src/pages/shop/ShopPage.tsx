import React, { useState, useEffect } from "react";
import { listingService } from "../../api/listing.service";
import { ListingCard } from "../../components/shop/ListingCard";
import { Loading } from "../../components/common/Loading";
import { Alert } from "../../components/common/Alert";
import { ListingFilters, Listing } from "../../types/listing.types";
import styles from "./ShopPage.module.css";

export const ShopPage: React.FC = () => {
  const [listings, setListings] = useState<Listing[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<ListingFilters>({
    page: 0,
    pageSize: 20,
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
      const response = await listingService.getListings(filters);

      if (response.success && response.data) {
        setListings(response.data.listings);
        setTotal(response.data.total);
        setCurrentPage(response.data.page);
      } else {
        setError(response.error?.message || "Error al cargar listings");
      }
    } catch (err) {
      console.error("Load listings error:", err);
      setError("Error de conexión con el servidor");
    } finally {
      setIsLoading(false);
    }
  };

  const handlePageChange = (newPage: number) => {
    setFilters((prev) => ({ ...prev, page: newPage }));
  };

  const totalPages = Math.ceil(total / (filters.pageSize || 20));

  return (
    <div className={styles.shopPage}>
      <div className={styles.container}>
        <header className={styles.header}>
          <h1 className={styles.title}>Shop</h1>
          <p className={styles.subtitle}>Encuentra los mejores sneakers</p>
        </header>

        {error && (
          <Alert variant="error" onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {isLoading ? (
          <Loading message="Cargando listings..." />
        ) : listings.length === 0 ? (
          <div className={styles.emptyState}>
            <p>No se encontraron listings disponibles.</p>
          </div>
        ) : (
          <>
            <div className={styles.listingsGrid}>
              {listings.map((listing) => (
                <ListingCard key={listing.id} listing={listing} />
              ))}
            </div>

            {totalPages > 1 && (
              <div className={styles.pagination}>
                <button
                  className={styles.paginationButton}
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 0}
                >
                  Anterior
                </button>

                <span className={styles.paginationInfo}>
                  Página {currentPage + 1} de {totalPages} ({total} resultados)
                </span>

                <button
                  className={styles.paginationButton}
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage >= totalPages - 1}
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
