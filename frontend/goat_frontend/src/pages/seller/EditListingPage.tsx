import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { listingService } from '../../api/listing.service';
import { Card } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import { Alert } from '../../components/common/Alert';
import { Loading } from '../../components/common/Loading';
import { Input } from '../../components/common/Input';
import { Listing, UpdateListingRequest, Condition, Gender } from '../../types/listing.types';
import styles from './EditListingPage.module.css';

export const EditListingPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [listing, setListing] = useState<Listing | null>(null);
  const [formData, setFormData] = useState<UpdateListingRequest>({});
  const [errors, setErrors] = useState<Partial<Record<keyof UpdateListingRequest, string>>>({});
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  useEffect(() => {
    if (id) {
      loadListing();
    }
  }, [id]);

  const loadListing = async () => {
    if (!id) return;

    setIsLoading(true);
    setError(null);

    try {
      const response = await listingService.getListingById(id);
      
      if (response.success && response.data) {
        const listingData = response.data;
        
        if (listingData.status !== 'DRAFT') {
          setError('Solo se pueden editar listings en estado DRAFT');
          return;
        }

        setListing(listingData);
        setFormData({
          size: listingData.size,
          condition: listingData.condition,
          gender: listingData.gender,
          brand: listingData.brand,
          color: listingData.color,
          price: listingData.price,
          coverImage: listingData.coverImage,
        });
      } else {
        setError(response.error?.message || 'Error al cargar el listing');
      }
    } catch (err) {
      setError('Error de conexión con el servidor');
    } finally {
      setIsLoading(false);
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof UpdateListingRequest, string>> = {};

    if (formData.size !== undefined && !formData.size) {
      newErrors.size = 'La talla es requerida';
    }

    if (formData.brand !== undefined && !formData.brand) {
      newErrors.brand = 'La marca es requerida';
    }

    if (formData.color !== undefined && !formData.color) {
      newErrors.color = 'El color es requerido';
    }

    if (formData.price !== undefined && formData.price <= 0) {
      newErrors.price = 'El precio debe ser mayor a 0';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!id || !listing) return;

    if (!validateForm()) {
      return;
    }

    setIsSaving(true);
    setMessage(null);

    try {
      const response = await listingService.updateListing(id, formData);
      
      if (response.success && response.data) {
        setMessage({ type: 'success', text: 'Listing actualizado exitosamente' });
        setTimeout(() => {
          navigate('/seller/listings');
        }, 1500);
      } else {
        setMessage({ type: 'error', text: response.error?.message || 'Error al actualizar listing' });
      }
    } catch (err) {
      setMessage({ type: 'error', text: 'Error de conexión con el servidor' });
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <div className={styles.editListingPage}>
        <div className={styles.container}>
          <Loading message="Cargando listing..." />
        </div>
      </div>
    );
  }

  if (error || !listing) {
    return (
      <div className={styles.editListingPage}>
        <div className={styles.container}>
          <Alert variant="error">
            {error || 'Listing no encontrado'}
          </Alert>
          <Button onClick={() => navigate('/seller/listings')} variant="outline">
            Volver a Mis Listings
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.editListingPage}>
      <div className={styles.container}>
        <Card className={styles.card}>
          <h1 className={styles.title}>Editar Listing</h1>

          {message && (
            <Alert
              variant={message.type === 'error' ? 'error' : 'success'}
              onClose={() => setMessage(null)}
            >
              {message.text}
            </Alert>
          )}

          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.infoBox}>
              <p><strong>SKU:</strong> {listing.sneakerSku}</p>
              <p><strong>Estado:</strong> {listing.status}</p>
            </div>

            <div className={styles.row}>
              <Input
                id="brand"
                label="Marca"
                value={formData.brand || ''}
                onChange={(e) => setFormData((prev) => ({ ...prev, brand: e.target.value }))}
                error={errors.brand}
                required
                fullWidth
              />

              <Input
                id="size"
                label="Talla"
                value={formData.size || ''}
                onChange={(e) => setFormData((prev) => ({ ...prev, size: e.target.value }))}
                error={errors.size}
                required
                fullWidth
              />
            </div>

            <div className={styles.row}>
              <div className={styles.selectGroup}>
                <label htmlFor="condition" className={styles.label}>
                  Condición <span className={styles.required}>*</span>
                </label>
                <select
                  id="condition"
                  value={formData.condition || listing.condition}
                  onChange={(e) => setFormData((prev) => ({ ...prev, condition: e.target.value as Condition }))}
                  className={styles.select}
                  required
                >
                  <option value="NEW">Nuevo</option>
                  <option value="LIKE_NEW">Como nuevo</option>
                  <option value="USED">Usado</option>
                  <option value="FAIR">Regular</option>
                </select>
              </div>

              <div className={styles.selectGroup}>
                <label htmlFor="gender" className={styles.label}>
                  Género <span className={styles.required}>*</span>
                </label>
                <select
                  id="gender"
                  value={formData.gender || listing.gender}
                  onChange={(e) => setFormData((prev) => ({ ...prev, gender: e.target.value as Gender }))}
                  className={styles.select}
                  required
                >
                  <option value="MALE">Masculino</option>
                  <option value="FEMALE">Femenino</option>
                  <option value="UNISEX">Unisex</option>
                </select>
              </div>
            </div>

            <Input
              id="color"
              label="Color"
              value={formData.color || ''}
              onChange={(e) => setFormData((prev) => ({ ...prev, color: e.target.value }))}
              error={errors.color}
              required
              fullWidth
            />

            <Input
              id="price"
              label="Precio (COP)"
              type="number"
              min="0"
              step="0.01"
              value={formData.price || ''}
              onChange={(e) => setFormData((prev) => ({ ...prev, price: parseFloat(e.target.value) || 0 }))}
              error={errors.price}
              required
              fullWidth
            />

            <Input
              id="coverImage"
              label="URL de Imagen"
              type="url"
              value={formData.coverImage || ''}
              onChange={(e) => setFormData((prev) => ({ ...prev, coverImage: e.target.value }))}
              fullWidth
              helperText="URL de la imagen principal del sneaker"
            />

            <div className={styles.actions}>
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate('/seller/listings')}
                disabled={isSaving}
              >
                Cancelar
              </Button>
              <Button
                type="submit"
                variant="primary"
                isLoading={isSaving}
              >
                Guardar Cambios
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
};

